import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import mu.KotlinLogging
import org.bytetwist.bytetwist.*
import org.bytetwist.bytetwist.nodes.*
import org.bytetwist.bytetwist.processors.common.ClassRenamer
import org.bytetwist.bytetwist.processors.oneOff
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

private val log = KotlinLogging.logger {}
@ExperimentalCoroutinesApi
class ScannerTest {

    @InternalCoroutinesApi
    private lateinit var loader: Loader

    @InternalCoroutinesApi
    @BeforeEach
    fun runScan() {
        loader = Loader()
        loader.scan(File(ScannerTest::class::java.javaClass.getResource("JavaTestClass.class").file))
    }

    @InternalCoroutinesApi
    @Test
    fun scanTest() {
        loader.launch()
        val scanner =  loader.processors
        assertEquals(1, scanner.nodes.size)
        assertEquals(2, scanner.nodes.first().fields.size)
        assertEquals(5, scanner.nodes.first().methods.size)
        assertEquals(2, scanner.nodes.first().methods.filterIsInstance(ByteConstructor::class.java).size)
    }

    @InternalCoroutinesApi
    @Test
    fun referencesTest() {
        val scanner =  loader.processors

        val field1 = scanner.nodes.first().fields.first() as ByteField
        val field2 = scanner.nodes.first().fields.last() as ByteField
        val method1 = scanner.nodes.first().constructors.first()
        val method2 = findMethod("testMethod2")
        assertNotNull(method2)
        assertEquals(5, field1.references.size)
        assertEquals(3, field2.references.size)
        assertEquals("<init>", method1.name)
        assertEquals(2, method2.invocations.size)
    }

    @InternalCoroutinesApi
    @Test
    fun m() {

        val clazz = newClass {
            name = "B"
        }
        assertNotNull(clazz)
    }

    @InternalCoroutinesApi
    @Test
    fun loaderTest() {
        assertNotNull(findClass("JavaTestClass"))
        val processingQueue = loader.processors
        assertNotNull(processingQueue)
        loader.addProcessor(oneOff<Block> { assertNotNull(it) } )
        Settings.annotateMethodComplexity = true
        val annotations = mutableSetOf<ByteAnnotation>()
        loader.addProcessor(oneOff<ByteAnnotation> { annotations.add(it) })
        val refs = arrayListOf<FieldReferenceNode>()
        loader.addProcessor(oneOff<FieldReferenceNode> { refs.add(it) })
        loader.addProcessor(oneOff<FieldRead> { log.info { it }; refs.add(it) })
        loader.addProcessor(oneOff<FieldWrite> { refs.add(it) })
        loader.launch()
        assertNotNull(annotations)
        refs.clear()
    }

    @InternalCoroutinesApi
    fun testRefs(
    ) {
        val refs = ArrayList<FieldReferenceNode>()
        val loader = Loader()
        loader.scan("src/test/resources")
        loader.addProcessor(oneOff<FieldWrite> { refs.add(it) })
        loader.launch()
        assertNotEquals(0, refs.size)
    }

    @InternalCoroutinesApi
    @Test
    fun annotationTest() {
        Settings.annotateClassChanges = true
        Settings.annotateMethodComplexity = true
        Settings.annotateTryCatchCount = true
        val classAnnotations = arrayListOf<ClassAnnotationNode>()
        loader.addProcessor(ClassRenamer())
        loader.addProcessor(oneOff<ClassAnnotationNode> { classAnnotations.add(it) })
        loader.launch()
        log.info { classAnnotations.size }
    }

}


