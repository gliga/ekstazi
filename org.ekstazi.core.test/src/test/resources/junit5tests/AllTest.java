import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.IncludeClassNamePatterns;

@Suite
@IncludeClassNamePatterns({
    "ATest", "BTest"
})
public class AllTest {
}
