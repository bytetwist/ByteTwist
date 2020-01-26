import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class DoublePassScannerTest {

    @ExperimentalCoroutinesApi
    @Test
    fun noInputTest() {
        assertThrows<NoInputDir> { DoublePassScanner().scan() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun jarInputTest() {
        val scanner = DoublePassScanner()
        scanner.inputDir = File("build/libs/")
        scanner.scan()
    }
}