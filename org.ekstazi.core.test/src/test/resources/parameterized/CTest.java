import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class CTest {

    private C mC;

    private String one;
    private String two;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object [][]{
                {"A", "a"},
                {"B", "b"},});
    }

    public CTest(String one, String two) {
        this.one = one;
        this.two = two;
    }

    @Before
    public void setUp() {
        mC = new C();
    }

    @Test
    public void test() {
        int result = mC.sum(5, 4);
        Assert.assertTrue(result > 0);
    }
}
