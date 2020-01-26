import kotlinx.coroutines.ExperimentalCoroutinesApi
import mu.KotlinLogging
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

private val log = KotlinLogging.logger {}

@ExperimentalCoroutinesApi
class BlockTest {

    private val scanner = DoublePassScanner()

    @BeforeEach
    fun scanResources() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
    }

    @Test
    fun testBlocks() {
        References.methodNames.values.find { m -> m.name == "methodWith5Blocks"}?.run {
            assertEquals(5, this.blocks.size)
        }
    }
}
