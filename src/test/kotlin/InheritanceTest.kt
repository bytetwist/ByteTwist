import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import java.io.File

@ExperimentalCoroutinesApi
class InheritanceTest {

    @InternalCoroutinesApi
    private val scanner = DoublePassScanner()

    @InternalCoroutinesApi
    @BeforeEach
    fun scanResources() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
    }

    @InternalCoroutinesApi
    @Test
    fun parentTest() {
        val testClass = scanner.nodes.find { compiledClass -> compiledClass.name == "JavaTestClass" }
        assertNotNull(testClass)
        assertEquals(testClass?.subClasses?.size, 2)
        testClass?.subClasses?.forEach {
            assertEquals(it.superClass(), testClass)
        }
    }

    @Test
    fun childTest() {
        val testClass = References.classNames["Child"]
        assertNotNull(testClass)
        assertEquals(testClass?.subClasses?.size, 1)
    }

}