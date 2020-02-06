import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import mu.KotlinLogging
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assertNotNull

private val log = KotlinLogging.logger {}
@ExperimentalCoroutinesApi
class RenameNodeTest {

    @InternalCoroutinesApi
    private val scanner = DoublePassScanner()


    @InternalCoroutinesApi
    @BeforeEach
    fun scanResources() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
        scanner.addProcessor(RenameMethod())
        scanner.addProcessor(oneOff(ByteField::class) {
                node -> if (node.name == "s") node.rename("stringValue")
        })
        scanner.run()
    }

    @Test
    fun renameMethod() {
        val m = References.findMethod("fine")
        assertNotNull(m)
        log.info { m.invocations.size }
        assertEquals(2, m.invocations.size)
    }

    @Test
    fun renameField() {
        val m = References.findField("stringValue")
        assertNotNull(m)
        assertEquals(3, m.references.size)
    }

    @Test fun renameClass() {
        val c = References.classNames["NodeForRenaming"]
        assertNotNull(c)
        c.rename("NewClassName")
        assert(References.classNames["NewClassName"] == c)
        assertEquals(c, References.findMethod("fine")!!.parent)
        assert(References.findMethod("paramsTest")?.desc?.contains("NewClassName")!!)
    }

    class RenameMethod(override val type: KClass<ByteMethod> = ByteMethod::class) :
        AbstractNodeProcessor<ByteMethod>() {

        override fun process(node: ByteMethod) {
            if (node.name == "renameMe") {
                node.rename("fine")
            }
        }
    }
}