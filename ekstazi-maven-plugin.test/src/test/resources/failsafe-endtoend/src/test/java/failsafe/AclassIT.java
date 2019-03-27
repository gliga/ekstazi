package failsafe;

import org.junit.Test;

public class AclassIT {
    
    @Test
    public void testM() {
        new A().m();
    }

    @Test
    public void testN() {
        new A().n();
    }
}
