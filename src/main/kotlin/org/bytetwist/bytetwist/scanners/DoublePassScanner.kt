package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.nodes.*
import org.bytetwist.bytetwist.processors.ProcessingQueue
import org.bytetwist.bytetwist.processors.common.*
import org.bytetwist.bytetwist.processors.log
import org.bytetwist.bytetwist.processors.oneOff
import org.objectweb.asm.ClassReader
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

fun CoroutineScope.methodReferences(it: CompiledMethod): Flow<MethodReferenceNode> =
    flow {
        it.methodCalls().asFlow().onEach {
            launch {
                it.addToMethod()
                emit(it)
            }
        }
    }

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class DoublePassScanner(override val coroutineContext: CoroutineContext) : Scanner(), CoroutineScope {

    private val classFiles = CopyOnWriteArrayList<ByteArray>()
    init {
        processors = ProcessingQueue(coroutineContext)
        processors.scanner = this
    }
    val nodes = processors.nodes



    /**
     * Scans the user submitted input location for any compiled class files
     */
    override fun scan() {
        if (inputDir == null) {
            throw NoInputDir()
        }
        log.info {
            "Scanning finished in " +
                    measureTimeMillis {

                    } / 1000.0 + " s"
        }
    }


    fun secondPass() =
        flow {

            emitAll(firstPass().onEach { value: CompiledClass ->
                launch {
                    value.buildHierarchy()
                }

                value.methods.asFlow()
                    .map { it as CompiledMethod }.onEach {
                        awaitAll(async {
                            it.analyzeBlocks()
                        },
                        async {
                            emitAll(methodReferences(it))
                        },
                        async {
                            emitAll(typeReferences(it))
                        },
                        async {
                            emitAll(fieldReferences(it))
                        })
                        emit(it)
                    }.onEach { emit(it) }
                value.fields.asFlow().filter { it is CompiledField }.onEach {
                    it as CompiledField
                    it.references.onEach {
                        it.addToField()
                    }
                    emit(it as CompiledField)
                }
            })
            }



    fun CoroutineScope.fieldReferences(it: CompiledMethod): Flow<FieldReferenceNode> =
        flow {
            it.fieldReferences().asFlow().onEach {
                launch {
                    it.addToField()
                }
                emit(it)
            }
        }

    fun CoroutineScope.typeReferences(it: CompiledMethod): Flow<ClassReferenceNode> =
        flow {

            it.typeReferences().asFlow().onEach {
                launch {
                    it.addToClass()
                    emit(it)
                }
            }//.flowOn(this@typeReferences.newCoroutineContext(this@typeReferences.coroutineContext))
        }

    private fun firstPass() =
        flow {
            inputDir!!.walkTopDown().forEach {
                loadBytesFromFile(it)
            }
            classFiles.forEach {
                val classNode = CompiledClass()
                ClassReader(it).apply {
                    accept(classNode, ClassReader.SKIP_DEBUG)
                }
                emit(classNode)
            }
        }


    /**
     * Takes either a .class file or a .jar file and loads the ByteArray into the List of class Files
     * @param it - The Jar or class file to load
     */
    private fun loadBytesFromFile(it: File) {
        if (it.extension.toLowerCase() == "jar") {
            scanJar(JarFile(it))
        }
        if (it.extension.toLowerCase() == "class") {
            classFiles.add(it.readBytes())
        }
    }

    private fun scanJar(f: JarFile) {
        for (jarEntry in f.entries()) {
            if (jarEntry.name.endsWith("class")) {
                classFiles.add(bytes(f, jarEntry))
            }
        }
    }

    /**
     * Returns a ByteArray from a JarEntry of a JarFile
     * @param f - The JarFile containing the entry
     * @param jarEntry - the JarEntry to get bytes from
     * @return a ByteArray of the class file
     */
    private fun bytes(f: JarFile, jarEntry: JarEntry?) =
        f.getInputStream(jarEntry).readBytes()

    companion object {

    }


}

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
fun main() {
    val classProcessor =
        BasicClassProcessor()// as AbstractProcessor<*>
    val scanner = DoublePassScanner(GlobalScope.newCoroutineContext(Dispatchers.IO))
    scanner.inputDir = File("186.jar")
    scanner.processors.scanner = scanner
    scanner.addProcessor(AbstractMethodProcessor())
    scanner.addProcessor(UnusedMethodProcessor())
    scanner.addProcessor(UnusedFieldProcessor())
    scanner.addProcessor(FieldRenamer())
    scanner.addProcessor(MethodRenamer())
    scanner.addProcessor(ClassRenamer())
    var i = 0
    scanner.addProcessor(oneOff(CompiledField::class) {
        val grouped = it.references.groupBy { fr -> fr.field()?.parent }
        val map = grouped.mapValues { entry -> entry.value.size }
        val refLimit = 1
        if (it.isStatic() && grouped.isNotEmpty() && grouped.size == refLimit) {
            val first = grouped.keys.first()
            if (first != null) {
                it.annotate("Moved", "from" to it.parent.name, "to" to first.name)
                it.move(first)
                i++
            }
        }
    })
    scanner.addProcessor(oneOff(CompiledMethod::class) {
        if (it.fieldWrites().size == 1 && it.fieldReads().isEmpty()) {
            // log.info { "method ${it.name} is probably a setter for ${it.fieldWrites().first().name}" }
            //log.info { it.fieldWrites().first().previous }
        }
    })
    scanner.addProcessor(oneOff(CompiledMethod::class) {
        if (it.fieldReads().size == 1 && it.fieldWrites().isEmpty()) {
            //log.info { "method ${it.name} is probably a getter for ${it.fieldReads().first().name}" }
        }
    })
    scanner.addProcessor(JarOutputProcessor())
    scanner.run()
    println(i)

}