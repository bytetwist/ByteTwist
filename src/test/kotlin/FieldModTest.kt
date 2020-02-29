import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.Loader
import org.bytetwist.bytetwist.findField
import org.bytetwist.bytetwist.findMethod
import org.bytetwist.bytetwist.nodes.newClass
import org.bytetwist.bytetwist.nodes.newField
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.Type
import java.io.File
import java.lang.reflect.Modifier
import kotlin.test.*

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
        assert(field.references.size == 1)
        val ref = field.references[0]
        assertNotNull(ref)
        field.delete()
        assertNull(findField("privateStaticField"))
        assertFalse(parent.fields.map { f -> f.name }.contains("privateStaticField"))
        assert(!ref.method.instructions.contains(ref))
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

    @Test
    fun testNewField() {
        val clazz = newClass {
            name = "NewClazz"
        }
        assertNotNull(clazz)
        val field = newField {
            name = "field1"
            descriptor = Type.INT_TYPE
            value = 0
            parent = clazz
        }
        assertNotNull(field)
        assertEquals(clazz, field.parent)
        assert(field.name == "field1")
        assert(field.access.and(Modifier.PUBLIC) == 1)
        assertNotNull(findField("field1"))
        assert(clazz.fields.contains(field))
        assertNotNull(field.value)
        assertFalse { field.isAbstract() }
        field.setAbstract()
        assert(field.isAbstract())
        field.setPrivate()
        assert(field.isPrivate())
    }


}