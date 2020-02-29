import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.findClass
import org.bytetwist.bytetwist.findMethod
import org.bytetwist.bytetwist.getMethodByName
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.nodes.ByteTryCatch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.Type
import java.io.File
import kotlin.test.*

@InternalCoroutinesApi
class MethodModTest {

    @ExperimentalCoroutinesApi
    private val s = Loader()
    private lateinit var methodModClass: ByteClass

    @ExperimentalCoroutinesApi
    @BeforeEach
    fun setup() {
        s.scan(File("src/test/resources"))
        methodModClass = findClass("MethodModClass")!!
    }

    @Test
    fun testFieldReadsWrites() {
        assertNotNull(methodModClass)
        val method = methodModClass.getMethodByName("testModMethod")
        assertNotNull(method)
        assert(method.fieldReads().size == 1)
        assert(method.fieldWrites().size == 1)
    }

    @Test
    fun testMethodRef() {
        val m = findMethod("tryCatch")
        assertNotNull(m)
        assertEquals(2, m.methodCalls().size)
        m.methodCalls().forEach {
            assertEquals(m, it.calledFrom)
            it.addToMethod()
            if (it.getMethod() != null) {
                assertNotNull(it.getMethod())
                assert(it.getMethod()!!.invocations.contains(it))
            }
        }
        assertEquals(1, m.tryCatchBlocks.size)
        val tryCatchBlock = m.tryCatchBlocks.first() as ByteTryCatch
        assertNotNull(tryCatchBlock.tryBlock())
        assertNotNull(tryCatchBlock.catchBlock())
        assertEquals(m, tryCatchBlock.method)
        assert(m.blocks.contains(tryCatchBlock.tryBlock()))
        assert(m.blocks.contains(tryCatchBlock.catchBlock()))
    }

    @Test
    fun testTryCatch() {
        assertNotNull(methodModClass)
        val method = methodModClass.getMethodByName("tryCatch")
        assertNotNull(method)
        method.tryCatchBlocks.size == 1
        val tryCatch = method.tryCatchBlocks.first() as ByteTryCatch
        assertNotNull(tryCatch)
        assertEquals(tryCatch.getType(), Type.getObjectType("java/lang/Exception"))
    }

    @Test
    fun testPublic() {
        assertNotNull(methodModClass)
        val method = methodModClass.getMethodByName("tryCatch")
        assertNotNull(method)
        method.setPrivate()
        assert(!method.isPublic())
        method.setPublic()
        assert(method.isPublic())
        assertFalse(method.isPrivate())
    }

    @Test
    fun deleteTest() {
        assertNotNull(methodModClass)
        val method = methodModClass.getMethodByName("tryCatch")
        assertNotNull(method)
        val invocation = method.invocations.first()
        assertNotNull(invocation)
        assert(invocation.calledFrom.instructions.contains(invocation))
        method.delete()
        assertFalse(methodModClass.methods.contains(method))
        assertNull(methodModClass.getMethodByName("tryCatch"))
        assertNull(findMethod("tryCatch"))
        assert(!invocation.calledFrom.instructions.contains(invocation))
        assert(method.invocations.isEmpty())
    }

    @Test
    fun cfgDotTest() {
        assertNotNull(methodModClass)
        val method = methodModClass.getMethodByName("tryCatch")
        assertNotNull(method)
        assertNotNull(method.getCfgDot())
    }
}