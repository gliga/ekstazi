import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

class C {
    public static int m() {
        return 42;
    }
}

@RunWith(Enclosed.class)
public class AllTest {

    public static class TestB {
        @Test
        public void test42() {
            System.out.println("Darko printing: Actually running TestB");
            assertEquals(42, C.m());
        }
    }
    
    public static class TestA {
        @Test
        public void test42() {
            System.out.println("Darko printing: Actually running TestA");
            assertEquals(42, C.m());
        }
    }
}
