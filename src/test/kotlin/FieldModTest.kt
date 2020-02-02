import kotlinx.coroutines.InternalCoroutinesApi
import org.bytetwist.bytetwist.findField
import org.bytetwist.bytetwist.findMethod
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@InternalCoroutinesApi
class FieldModTest {

    private val s = DoublePassScanner()

    @BeforeEach
    fun setup() {
        s.inputDir = File("src/test/resources")
        s.scan()
    }

    @Test
    fun default() {
        with (findField("privateStaticField")) {
            assertNotNull(this)
            assertTrue { isPrivate() }
            assertTrue { isStatic() }
            setPublic()
            assertTrue { isPublic() }
            setStatic()
            assertTrue { !isStatic() }
        }
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


}