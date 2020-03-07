import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.*
import org.bytetwist.bytetwist.nodes.*
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.Type
import java.io.File
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AnnotationGenerationTestification {

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private lateinit var scanner: DoublePassScanner
    private lateinit var loader: Loader



    @BeforeEach
    fun scanResources() {
        loader = Loader()
        loader.scan(File("src/test/resources"))
    }


    @Test
    fun methodAnnotations() {
        val methods = ArrayList<ByteMethod>()
        loader.addProcessor(oneOff<ByteClass> { clazz ->
            if (clazz.methods.isNotEmpty()) {
                clazz.methods.forEach {
                    (it as ByteMethod).annotate("ThisIsAnAnnotation", "omg" to "wow")
                    methods.add(it)
                }
            }
        })
        loader.launch()
        val m = findMethod("testModMethod")
        assertNotNull(m)
        assert(m.visibleAnnotations != null)
        assert(m.visibleAnnotations.size > 0)
        val a = m.visibleAnnotations as MutableList<ByteAnnotation>
        assertNotNull(a)
        val annotation = a.find { it.desc.contains("ThisIsAnAnnotation") }
        assertNotNull(annotation)
        assertNotNull(annotation.desc)
        assert(annotation.annotates == m)
        assert(annotation.values.isNotEmpty())
        assert(Type.getType(annotation.desc).internalName == "ThisIsAnAnnotation")
        assert(annotation.values.first() == "omg")
    }

    @Test
    fun testClassAnnotation() {
        loader.addProcessor(oneOff<ByteClass>{ it.annotate("classAnnotation", "className" to it.name) })
        loader.launch()
        val clazz = findClass("JavaTestClass")
        assertNotNull(clazz)
        val annotations = clazz.visibleAnnotations as List<ClassAnnotationNode>
        assert(!annotations.isNullOrEmpty())
        val newAnnotation = annotations.find { it.desc.contains("classAnnotation") }
        assertNotNull(newAnnotation)
        assertNotNull(newAnnotation.values)
        assert(newAnnotation.values.contains("className"))
    }

    @Test
    fun fieldAnnotationTest() {
        loader.addProcessor(oneOff<ByteField> {
            it.annotate("field", "references" to it.references.size)
        })
        loader.launch()
        val field = findField("testField2")
        assertNotNull(field)
        val annotation = field.visibleAnnotations.find { it.desc.contains("field")  }
        assertNotNull(annotation)
        assert(annotation.values.contains("references"))
    }

}