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

package org.ekstazi.it.util;

import java.io.File;

import java.net.URL;

import org.ekstazi.Names;
import org.ekstazi.util.FileUtil;

public class EkstaziPaths {

    public static  String getEkstaziCoreJarPath() {
        return getEkstaziJarPath("org.ekstazi.core-");
    }

    public static String getEkstaziAntJarPath() {
        return getEkstaziJarPath("org.ekstazi.ant-");
    }

    public static String getEkstaziJarPath(String prefix) {
        String classpath = System.getProperty("java.class.path");
        String[] parts = classpath.split(System.getProperty("path.separator"));
        for (String part : parts) {
            if (part.contains(prefix + Names.TOOL_VERSION + ".jar")) {
                return part;
            }
        }
        return null;
    }

    public static void removeEkstaziDirectories(Class<?> clz, String dir) throws Exception {
        URL url = clz.getResource("/" + dir + "/README.txt");
        File readme = new File(url.toURI());
        File testdir = readme.getParentFile();
        removeEkstaziDirectory(testdir);
    }

    private static void removeEkstaziDirectory(File dir) {
        if (dir.getName().equals(Names.EKSTAZI_ROOT_DIR_NAME)) {
            FileUtil.deleteDirectory(dir);
        } else {
            for (File child : dir.listFiles()) {
                if (child.isDirectory()) {
                    removeEkstaziDirectory(child);
                }
            }
        }
    }
}
