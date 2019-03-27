
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class MyTestRunner extends BlockJUnit4ClassRunner {

    public MyTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
}
