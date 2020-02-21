import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.findField
import org.bytetwist.bytetwist.findMethod
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@InternalCoroutinesApi
class FieldModTest {

    @ExperimentalCoroutinesApi
    private val s = Loader()

    @ExperimentalCoroutinesApi
    @BeforeEach
    fun setup() {
        s.scan(File("src/test/resources"))

    }

    @Test
    fun default() {
        with (findField("privateStaticField")) {
            assertNotNull(this)
            assertTrue { isPrivate() }
            assertTrue { isStatic() }
            setPublic()
            assertTrue { isPublic() }
        }
    }

    @Test
    fun fieldParent() {
        val parent = findField("privateStaticField")?.parent
        assertNotNull(parent)
    }

    @Test
    fun deleteTest() {
        val field = findField("privateStaticField")
        assertNotNull(field)
        val parent = field.parent
        assertNotNull(parent)
        field.delete()
        assertNull(findField("privateStaticField"))
        assertFalse(parent.fields.map { f -> f.name }.contains("privateStaticField"))
    }

    @Test
    fun methodModTest() {
        with (findMethod("testMethod1")) {
            assertNotNull(this)
            assertTrue { isPublic() }
            assertTrue { !isAbstract() }
            assertTrue { !isStatic() }
            this.setPrivate()
            assertTrue { isPrivate() }
            this.setStatic()
            assertTrue { isStatic() }
            setStatic()
            setAbstract()
            assertTrue { isAbstract() }
        }
    }

    @Test
    fun annotateTest() {
        val field = findField("privateStaticField")
        assertNotNull(field)
        field.annotate("test")
        assert(field.visibleAnnotations.size == 1)
    }


}