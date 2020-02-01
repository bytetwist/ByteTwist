import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import mu.KotlinLogging
import org.bytetwist.bytetwist.References
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.nodes.ConstructorNode
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

private val log = KotlinLogging.logger {}
@ExperimentalCoroutinesApi
class ScannerTest {

    @InternalCoroutinesApi
    private val scanner = DoublePassScanner()

    @InternalCoroutinesApi
    @BeforeEach
    fun runScan() {
        scanner.inputDir = File(ScannerTest::class::java.javaClass.getResource("JavaTestClass.class").file)
        scanner.scan()
    }

    @InternalCoroutinesApi
    @Test
    fun scanTest() {
        assertEquals(1, scanner.processors.nodes.size)
        assertEquals(2, scanner.processors.nodes.first().fields.size)
        assertEquals(5, scanner.processors.nodes.first().methods.size)
        assertEquals(2, scanner.processors.nodes.first().methods.filterIsInstance(ConstructorNode::class.java).size)
    }

    @InternalCoroutinesApi
    @Test
    fun referencesTest() {
        val field1 = scanner.processors.nodes.first().fields.first() as CompiledField
        val field2 = scanner.processors.nodes.first().fields.last() as CompiledField
        val method1 = scanner.processors.nodes.first().constructors.first() as ConstructorNode
        val method2 = References.findMethod("testMethod2")!!
        assertEquals(5, field1.references.size)
        assertEquals(3, field2.references.size)
        assertEquals("<init>", method1.name)

        assertEquals(2, method2.invocations.size)
    }

}


