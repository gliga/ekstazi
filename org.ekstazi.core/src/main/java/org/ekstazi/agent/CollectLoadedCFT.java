/*
 * Copyright 2014-present Milos Gligoric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ekstazi.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;

import org.ekstazi.Names;
import org.ekstazi.monitor.CoverageMonitor;
import org.ekstazi.util.Types;

/**
 * {@link ClassFileTransformer} that notifies {@link CoverageMonitor} about all
 * loaded classes. These transformer should be used when dependencies are
 * collected for the entire JVM run.
 */
public class CollectLoadedCFT implements ClassFileTransformer {

    /** Strings used to identify URL for JUnit */
    private static final String JUNIT_FRAMEWORK_URL_PART = "!/" + Names.JUNIT_FRAMEWORK_PACKAGE_VM;
    private static final String ORG_JUNIT_URL_PART = "!/" + Names.ORG_JUNIT_PACKAGE_VM;
    private static final String ORG_HAMCREST_URL_PART = "!/" + Names.ORG_HAMCREST_VM;
    private static final String ORG_APACHE_MAVEN_URL_PART = "!/" + Names.ORG_APACHE_MAVEN_VM;

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (loader != null && !Types.isIgnorableInternalName(className)) {
            try {
                URL url = loader.getResource(className + ".class");
                if (url != null) {
                    String externalForm = url.toExternalForm();
                    if (!isWellKnownUrl(externalForm)) {
                        // Include class in set of dependencies.
                        CoverageMonitor.addUncleanableURLs(externalForm);
                    }
                }
            } catch (Exception ex) {
                // Nothing.
            }
        }
        return null;
    }

    private static boolean isWellKnownUrl(String externalForm) {
        return externalForm.contains(ORG_JUNIT_URL_PART) || externalForm.contains(JUNIT_FRAMEWORK_URL_PART)
                || externalForm.contains(ORG_HAMCREST_URL_PART) || externalForm.contains(ORG_APACHE_MAVEN_URL_PART);
    }
}
