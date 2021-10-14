package org.ekstazi.junit5Extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

public class ResultExtension implements TestWatcher {
    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
        Junit5FailureFlag.isTestFailed = true;
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> optional) {
        return;
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
        return;
    }

    @Override
    public void testAborted(ExtensionContext extensionContext, Throwable throwable) {
        return;
    }
}
