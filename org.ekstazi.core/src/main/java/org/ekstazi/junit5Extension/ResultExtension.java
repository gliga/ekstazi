package org.ekstazi.junit5Extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public abstract class ResultExtension implements TestWatcher {
    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
        Junit5FailureFlag.isTestFailed = true;
    }
}
