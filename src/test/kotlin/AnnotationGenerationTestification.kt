import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.nodes.ByteAnnotation
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.Type
import java.io.File
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AnnotationGenerationTestification {

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private val scanner = DoublePassScanner()


    @BeforeEach
    fun scanResources() {
        scanner.inputDir = File("src/test/resources")
        scanner.scan()
    }


    @Test
    fun testAnnotations() {
        val methods = ArrayList<ByteMethod>()
        scanner.addProcessor(oneOff(ByteClass::class) { clazz ->
            if (clazz.methods.isNotEmpty()) {
                clazz.methods.forEach {
                    (it as ByteMethod).annotate("ThisIsAnAnnotation", "omg" to "wow")
                    methods.add(it)
                }
            }
        })
        scanner.run()
        val m = methods.first()
        assert(m.visibleAnnotations != null)
        assert(m.visibleAnnotations.size > 0)
        val a = m.visibleAnnotations.first() as ByteAnnotation
        assertNotNull(a)
        assertNotNull(a.descriptor)
        assert(a.annotates == m)
        assert(a.values.isNotEmpty())
        assert(Type.getType(a.descriptor).internalName == "ThisIsAnAnnotation")
        assert(a.values.first() == "omg")
    }

}