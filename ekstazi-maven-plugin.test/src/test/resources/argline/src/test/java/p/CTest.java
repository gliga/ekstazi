package p;

import org.junit.Test;
import org.junit.Assert;

public class CTest {
    @Test
    public void test1() {
        Assert.assertEquals(System.getProperty("p1"), "value1");
        Assert.assertEquals(System.getProperty("p2"), "value2");
    }
}
