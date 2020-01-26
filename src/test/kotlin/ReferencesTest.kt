import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class ReferencesTest {
    val scanner = DoublePassScanner()

    @BeforeEach
    fun scan() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
    }

    @Test
    fun findField() {
        assertNotNull(References.findField("testField1"))
    }

    @Test
    fun findMethod() {
        assertNotNull(References.findMethod("testMethod1"))
    }
}