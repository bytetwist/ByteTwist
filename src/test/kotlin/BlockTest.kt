import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import mu.KotlinLogging
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.findMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import kotlin.test.assertNotNull

private val log = KotlinLogging.logger {}

@ExperimentalCoroutinesApi
class BlockTest {

    @InternalCoroutinesApi
    private val loader = Loader()

    @InternalCoroutinesApi
    @BeforeEach
    fun scanResources() {
        loader.scan(File("src/test/resources"))
    }

    @Test
    fun testBlocks() {
        val m = findMethod("methodWith5Blocks")
        m?.drawFlowGraph()

        m?.run {
            assertEquals(5, this.blocks.size)
        }
    }

    @Test
    fun flowGraphTest() {
        val mm = References.methodNames.values.random()
        assertNotNull(mm.cfg)
        log.info { findMethod("testModMethod")?.drawFlowGraph()?.javaClass?.name }
        assert(findMethod("testModMethod")?.drawFlowGraph() is BufferedImage)
    }
}
