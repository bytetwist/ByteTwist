import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import mu.KLogger
import mu.KLogging
import mu.KotlinLogging
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assert
import kotlin.test.assertNotNull

private val log = KotlinLogging.logger {}
@ExperimentalCoroutinesApi
class RenameNodeTest {

    private val scanner = DoublePassScanner()


    @InternalCoroutinesApi
    @BeforeEach
    fun scanResources() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
        scanner.addProcessor(RenameMethod())
        scanner.addProcessor(oneOff(CompiledField::class) {
                node -> if (node.name == "s") node.rename("stringValue")
        })
        scanner.run()
    }

    @Test
    fun renameMethod() {
        val m = References.findMethod("fine")
        assertNotNull(m)
        assertEquals(1, m.invocations.size)
    }

    @Test
    fun renameField() {
        val m = References.findField("stringValue")
        assertNotNull(m)
        assertEquals(2, m.references.size)
    }

    @Test fun renameClass() {
        val c = References.classNames["NodeForRenaming"]
        assertNotNull(c)
        c.rename("NewClassName")
        assert(References.classNames["NewClassName"] == c)
        assertEquals(c, References.findMethod("fine")?.parent)
        assert(References.findMethod("paramsTest")?.desc?.contains("NewClassName")!!)
    }

    class RenameMethod(override val type: KClass<CompiledMethod> = CompiledMethod::class) :
        AbstractNodeProcessor<CompiledMethod>() {

        override fun process(node: CompiledMethod) {
            if (node.name == "renameMe") {
                node.rename("fine")
            }
        }
    }
}