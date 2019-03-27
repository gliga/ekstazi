import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class CTest {
    private C mC;

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
