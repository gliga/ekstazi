import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.*;

public class CTest {

    @Test
    public void test() throws Exception {
        File f = new File("README.txt");
        BufferedReader br = new BufferedReader(new FileReader(f));
        br.readLine();
    }
}
