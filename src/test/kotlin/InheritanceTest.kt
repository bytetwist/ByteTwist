import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.Loader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.findClass
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class InheritanceTest {

    @InternalCoroutinesApi
    private val scanner = Loader()

    @InternalCoroutinesApi
    @BeforeEach
    fun scanResources() {
        scanner.scan(File("src/test/resources"))
    }

    @InternalCoroutinesApi
    @Test
    fun parentTest() {
        val testClass = scanner.processors.nodes.find { compiledClass -> compiledClass.name == "JavaTestClass" }
        assertNotNull(testClass)
        assertEquals(2, testClass.subClasses.size)
        testClass.subClasses.forEach {
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