import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;

@RunWith(MyTestRunner.class)
public class CTest {

    @Test
    public void test() {
        new C().m(3, 3);
    }
}
