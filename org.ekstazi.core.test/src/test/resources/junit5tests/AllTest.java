import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

@Suite
@SelectClasses({ATest.class, BTest.class})
public class AllTest {
}
