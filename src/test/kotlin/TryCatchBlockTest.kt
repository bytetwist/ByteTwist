import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.findMethod
import org.bytetwist.bytetwist.nodes.ByteBlockNode
import org.bytetwist.bytetwist.nodes.ByteTryCatch
import org.bytetwist.bytetwist.nodes.TryCatchBlock
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull

class TryCatchBlockTest {


    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    val scanner = Loader()

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @BeforeEach
    fun scan() {
        scanner.scan(File("src/test/resources"))
    }

    @Test
    fun tryBlock() {
        val method = findMethod("tryCatchBlock")
        assertNotNull(method)
        assertNotNull(method.tryCatchBlocks)
        assert(method.tryCatchBlocks.size == 1)
        val tryCatchBlock = method.tryCatchBlocks.first() as ByteTryCatch
        assertNotNull(tryCatchBlock)
        val tryBlock = tryCatchBlock.tryBlock()
        assertNotNull(tryBlock)
        assert(method.blocks.contains(tryBlock))
        tryCatchBlock.catchBlock()
        print(method.blocks.map { ByteBlockNode::edges })
    }
}