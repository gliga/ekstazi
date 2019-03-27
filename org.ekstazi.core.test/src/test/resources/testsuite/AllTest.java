import org.junit.Test;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ATest.class,
    BTest.class,
})
public class AllTest {

    // Tests inside this class are ignored.

    @Test
    public void test() {
    }
}
