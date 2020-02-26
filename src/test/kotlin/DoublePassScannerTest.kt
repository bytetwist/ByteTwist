import kotlinx.coroutines.*
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.exceptions.UninitializedScanner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class DoublePassScannerTest {

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun noInputTest() {
        assertThrows<NoInputDir> {
            Loader().scan("q23wre    wefwqerf")
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @Test
    fun uninitializedScannerTest() {
        assertThrows<UninitializedScanner> {  runBlocking { Loader().runScanner() } }
    }

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun jarInputTest() {
        Loader().scan(File("src/test/resources/ByteTwist-0.2.jar"))
    }
}
