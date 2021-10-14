package org.ekstazi.junit5Extension;

import org.ekstazi.Ekstazi;
import org.ekstazi.monitor.CoverageMonitor;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CoverageExtension implements BeforeAllCallback, AfterAllCallback {
    private String[] mURLs = null;
    private String mClassName = null;
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        mClassName = extensionContext.getRequiredTestClass().getCanonicalName();
        CoverageMonitor.clean();
        mURLs = CoverageMonitor.getURLs();
        Ekstazi.inst().beginClassCoverage(mClassName);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (mURLs != null) CoverageMonitor.addURLs(mURLs);
        Ekstazi.inst().endClassCoverage(mClassName, Junit5FailureFlag.isTestFailed);
    }
}
