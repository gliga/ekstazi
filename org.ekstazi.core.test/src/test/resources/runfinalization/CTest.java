import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.lang.ref.WeakReference;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class CTest {

    @Test
    public void testRunFinalization() throws Exception {
        cleanWeakReferences(true);
    }

    @Test
    public void testGc() throws Exception {
        cleanWeakReferences(false);
    }

    // INTERNAL

    private void cleanWeakReferences(boolean runFinalization) throws Exception {
        ClassLoader cl = getClassLoader();
        Class<?> clz = cl.loadClass("C");
        Assert.assertEquals(1, clz.getDeclaredMethods().length);

        WeakReference<Class<?>> ref = new WeakReference<Class<?>>(clz);
        Assert.assertNotNull(ref.get());
        cl = null;
        clz = null;
        if (runFinalization) {
            System.runFinalization();
        }
        System.gc();
        Thread.sleep(3000);
        Assert.assertNull(ref.get());
    }

    private ClassLoader getClassLoader() throws Exception {
        URL url = getClass().getClassLoader().getResource("C.class");
        File dir = new File(url.getFile()).getParentFile();
        return new URLClassLoader(new URL[] { dir.toURI().toURL() }, null);
    }
}
