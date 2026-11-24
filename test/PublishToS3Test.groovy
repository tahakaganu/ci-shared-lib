import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class PublishToS3Test extends BasePipelineTest {
    @BeforeEach
    void init() { super.setUp() }

    @Test
    void testPublishToS3() {
        def step = loadScript('vars/publishToS3.groovy')
        step.call(bucket: 'b', file: 'f.tgz', prefix: 'demo')
        assertTrue(helper.callStack.any {
            it.methodName == 'sh' && it.argsToString().contains('s3://b/demo/')
        })
    }

    @Test
    void failsWithoutBucket() {
        def step = loadScript('vars/publishToS3.groovy')
        assertThrows(Exception) { step.call(file: 'f.tgz') }
    }
}