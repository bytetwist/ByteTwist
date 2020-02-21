import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.findClass
import org.bytetwist.bytetwist.findMethod
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.Type
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assert
import kotlin.test.assertNotNull

private val log = KotlinLogging.logger {}
@ExperimentalCoroutinesApi
class RenameNodeTest {

    @InternalCoroutinesApi
    private val scanner = Loader()


    @InternalCoroutinesApi
    @BeforeEach
    fun scanResources() {
        scanner.scan(File("src/test/resources"))
        scanner.addProcessor(RenameMethod())
        scanner.addProcessor(oneOff<ByteField> {
                node -> if (node.name == "s") node.rename("stringValue")
        })
        scanner.launch()
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
        val byteMethod = findMethod("paramsTest")
        log.info { byteMethod?.desc }
        assert(byteMethod?.desc?.contains("NewClassName")!!)
        assertEquals(findClass("NewClassName")!!.subClasses.first().superName, "NewClassName")
            assertEquals(
                Type.getReturnType(byteMethod?.desc).className,
                "NewClassName"
            )
    }

    class RenameMethod(override val type: KClass<ByteMethod> = ByteMethod::class) :
        AbstractNodeProcessor<ByteMethod>() {

        override fun process(node: ByteMethod) {
            if (node.name == "renameMe") {
                runBlocking {
                    node.rename("fine")
                }
            }
        }
    }
}