import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.findField
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class ReferencesTest {
    @InternalCoroutinesApi
    val scanner = DoublePassScanner()

    @InternalCoroutinesApi
    @BeforeEach
    fun scan() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
    }

    @Test
    fun findField() {
        assertNotNull(findField("testField1"))
    }

    @Test
    fun findMethod() {
        assertNotNull(References.findMethod("testMethod1"))
    }
}