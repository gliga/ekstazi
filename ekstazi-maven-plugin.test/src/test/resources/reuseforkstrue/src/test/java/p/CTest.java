package p;

import org.junit.Test;
import org.junit.Assert;

import org.ekstazi.Config;

public class CTest {
    @Test
    public void test1() {
        Assert.assertEquals(Config.AgentMode.JUNITFORK, Config.MODE_V);
    }
}
