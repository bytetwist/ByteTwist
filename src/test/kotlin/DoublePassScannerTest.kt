import kotlinx.coroutines.*
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.concurrent.thread

class DoublePassScannerTest {

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun noInputTest() {
        assertThrows<NoInputDir> {
            Loader().scan("q23wre    wefwqerf")
        }
    }
}

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Test
fun jarInputTest() {
    Loader().scan(File("src/test/resources/ByteTwist-0.2.jar"))
}
