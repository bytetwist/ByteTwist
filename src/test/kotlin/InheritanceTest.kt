import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.bytetwist.bytetist.References
import org.bytetwist.bytetist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import java.io.File

@ExperimentalCoroutinesApi
class InheritanceTest {

    private val scanner = DoublePassScanner()

    @BeforeEach
    fun scanResources() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
    }

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