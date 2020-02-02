import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.findClass
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        assertEquals(testClass.subClasses.size, 1)
    }

    @InternalCoroutinesApi
    @Test
    fun abstractTest() {
        with (findClass("AbstractClass")) {
            assertNotNull(this)
            assertTrue { isAbstract() }
            assertFalse { isInterface() }
            assertFalse { isEnum() }
        }
    }

}