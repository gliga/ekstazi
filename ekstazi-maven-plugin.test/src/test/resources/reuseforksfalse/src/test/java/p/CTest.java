package p;

import org.junit.Test;
import org.junit.Assert;

import org.ekstazi.Config;

public class CTest {
    @Test
    public void test1() {
        Assert.assertEquals(Config.AgentMode.JUNIT, Config.MODE_V);
    }
}
