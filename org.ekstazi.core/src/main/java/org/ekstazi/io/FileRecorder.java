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

package org.ekstazi.io;

import java.io.File;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.regex.Pattern;

import org.ekstazi.monitor.CoverageMonitor;
import org.ekstazi.research.Research;

/*
 * Considering to use for monitoring used files. This class is Linux specific.
 */
@Research
public class FileRecorder extends SecurityManager {

    /** Name of this class */
    private static final String MY_NAME = FileRecorder.class.getCanonicalName();

    /** Pattern that describes files to exclude */
    private final Pattern mExcludes;

    /** Pattern that describes files to include */
    private final Pattern mIncludes;

    /** Current working directory */
    private final String mCwd;

    /**
     * Constructor.
     */
    public FileRecorder() {
        this(null, null);
    }

    /**
     * Constructor.
     * 
     * @param includes
     *            Pattern that describes files to include
     * @param excludes
     *            Pattern that describes files to exclude
     */
    public FileRecorder(Pattern includes, Pattern excludes) {
        this.mIncludes = includes;
        this.mExcludes = excludes;
        this.mCwd = System.getProperty("user.dir");
    }

    @Override
    public void checkRead(String file) {
        recordFile(file);
    }

    @Override
    public void checkRead(String file, Object context) {
        recordFile(file);
    }

    private void recordFile(String file) {
        String aFile = isAbsolutePath(file) ? file : mCwd + "/" + file;
        if (isRelevant(aFile)) {
            if (!isOnStack(3, MY_NAME)) {
                CoverageMonitor.addFileURL(new File(aFile));
            }
        }
    }

    @Override
    public void checkWrite(String file) {
        // Ignore for now. By ignoring files that are written, we need not worry
        // about closing streams and shutdown hooks.
    }

    // In the following methods we intentionally do nothing to save time for
    // creating Permission objects.

    @Override
    public void checkPermission(Permission perm) {
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }

    @Override
    public void checkCreateClassLoader() {
    }

    @Override
    public void checkAccess(Thread t) {
    }

    @Override
    public void checkAccess(ThreadGroup g) {
    }

    @Override
    public void checkExit(int status) {
    }

    @Override
    public void checkExec(String cmd) {
    }

    @Override
    public void checkLink(String lib) {
    }

    @Override
    public void checkRead(FileDescriptor fd) {
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
    }

    @Override
    public void checkDelete(String file) {
    }

    @Override
    public void checkConnect(String host, int port) {
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
    }

    @Override
    public void checkListen(int port) {
    }

    @Override
    public void checkAccept(String host, int port) {
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
    }

    @Override
    public void checkPropertiesAccess() {
    }

    @Override
    public void checkPropertyAccess(String key) {
    }

    @Override
    public void checkPrintJobAccess() {
    }

    @Override
    public void checkSystemClipboardAccess() {
    }

    @Override
    public void checkAwtEventQueueAccess() {
    }

    @Override
    public void checkPackageAccess(String pkg) {
    }

    @Override
    public void checkPackageDefinition(String pkg) {
    }

    @Override
    public void checkSetFactory() {
    }

    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
    }

    @Override
    public void checkSecurityAccess(String target) {
    }

    // INTERNAL

    /**
     * Checks if the given file/path is absolute path.
     * 
     * @param file
     *            File name to check
     * @return True if file name is absolute path, false otherwise
     */
    protected boolean isAbsolutePath(String file) {
        // Linux specific
        return (file.length() > 0 && file.charAt(0) == '/');
    }

    /**
     * Determines if the file should be recorded based on the given
     * configuration. Note that additional filtering may happen in monitor(s),
     * e.g., temp files.
     * 
     * @param file
     *            File name to check
     * @return True if file should be recorded, false otherwise
     */
    protected boolean isRelevant(String file) {
        if (mExcludes != null && mExcludes.matcher(file).find()) {
            return false;
        }
        if (mIncludes != null && !mIncludes.matcher(file).find()) {
            return false;
        }
        return true;
    }

    private static boolean isOnStack(int moreThan, String canonicalName) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int count = 0;
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith(canonicalName)) {
                count++;
            }
        }
        return count > moreThan;
    }
}
