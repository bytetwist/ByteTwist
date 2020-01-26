import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.nodes.ConstructorNode
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class MemberMoveTest {

    @Test
    fun moveField() {
        val clazz = References.classNames["DestinationClass"]
        assertNotNull(clazz)
        val oldClass = References.classNames["MoveMemberTest"]
        assertNotNull(oldClass)
        assertEquals(0, oldClass.fields.size)
        assertEquals(1, clazz.fields.size)
    }

    @Test
    fun moveMethod() {
        val clazz = References.classNames["DestinationClass"]
        assertNotNull(clazz)
        val oldClass = References.classNames["MoveMemberTest"]
        assertNotNull(oldClass)
        assertEquals(0, oldClass.methods.filterIsInstance(CompiledMethod::class.java).size)
        assertEquals(1, clazz.methods.filter { mn -> mn !is ConstructorNode }.size)
    }

    companion object {
        private val scanner = DoublePassScanner()

        @InternalCoroutinesApi
        @BeforeAll
        @JvmStatic
        fun scanResources() {
            scanner.inputDir = File("src/test/resources")
            scanner.scan()
            scanner.addProcessor(oneOff(CompiledField::class) {
                if (it.isStatic() && it.name == "moveMe") {
                    References.classNames["DestinationClass"]?.let { it1 -> it.move(it1) }
                }
            })
            scanner.addProcessor(oneOff(CompiledMethod::class) {
                if (it.isStatic() && it.name == "moveMe") {
                    it.move(References.classNames["DestinationClass"]!!)
                }
            })
//            scanner.addProcessor(oneOff(CompiledClass::class) {
//                Files.write(Paths.get(scanner.inputDir.toString(), "/out/", it.name + ".class"), it.toBytes())
//            })
            scanner.run()
        }
    }
}