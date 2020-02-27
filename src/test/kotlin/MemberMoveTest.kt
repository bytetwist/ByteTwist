import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.findClass
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class MemberMoveTest {

    @InternalCoroutinesApi
    private val loader = Loader()

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @BeforeEach
    fun scanResources() {
        val scanner = Loader()
        scanner.scan(File("src/test/resources"))
        scanner.addProcessor(oneOff<ByteMethod> {
            if (it.isStatic() && it.name == "moveMe") {
                it.move(findClass("DestinationClass")!!)
            }
        })
//            scanner.addProcessor(oneOff(ByteClass::class) {
//                Files.write(Paths.get(scanner.inputDir.toString(), "/out/", it.name + ".class"), it.toBytes())
//            })
        scanner.launch()
    }
    @Test
    fun moveField() {
        val clazz = findClass("DestinationClass")
        assertNotNull(clazz)
        val oldClass = findClass("MoveMemberTest")
        assertNotNull(oldClass)
        (oldClass.fields.first() as ByteField).move(clazz)
        assertEquals(0, oldClass.fields.size)
        assertEquals(1, clazz.fields.size)
    }

    @Test
    fun moveMethod() {
        val clazz = References.classNames["DestinationClass"]
        assertNotNull(clazz)
        val oldClass = References.classNames["MoveMemberTest"]
        assertNotNull(oldClass)
        assertEquals(2, oldClass.methods.filterIsInstance(ByteMethod::class.java).size)
        assertEquals(2, clazz.methods.size)
    }

    companion object {
        @InternalCoroutinesApi
        private val scanner = Loader()

    }

}

