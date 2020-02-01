import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class DoublePassScannerTest {

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun noInputTest() {
        assertThrows<NoInputDir> { DoublePassScanner().scan() }
    }

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun jarInputTest() {
        val scanner = DoublePassScanner()
        scanner.inputDir = File("src/test/resources/ByteTwist-0.2.jar")
        scanner.scan()
    }
}