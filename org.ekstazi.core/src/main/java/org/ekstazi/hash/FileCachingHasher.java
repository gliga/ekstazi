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

package org.ekstazi.hash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.ekstazi.util.FileUtil;

/**
 * {@link Hasher} that saves cache in file at shutdown. This hasher can be used
 * with projects that spawn VM for each test. NOTE: If project runs VMs in
 * parallel this class is not safe.
 */
public class FileCachingHasher extends Hasher {

    /**
     * Constructor.
     */
    public FileCachingHasher(Algorithm algorithm, int cacheSizes, boolean isSemanticHashing, File cacheFile) {
        super(algorithm, cacheSizes, isSemanticHashing);
        setUpCache(path2Hash, cacheSizes, cacheFile);
    }

    private void setUpCache(final Map<String, String> path2Hash, int cacheSizes, final File cacheFile) {
        if (cacheFile.exists()) {
            loadCache(path2Hash, cacheFile, cacheSizes);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                storeCache(path2Hash, cacheFile);
            }
        });
    }

    // TODO: Move to LRU
    public static void storeCache(Map<String, String> map, File file) {
        BufferedWriter bw = null;
        try {
            // Ensure that we the directory exists (maybe this should be done at
            // another place).
            file.getParentFile().mkdirs();
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (Entry<String, String> en : map.entrySet()) {
                bw.write(en.getValue());
                bw.write(' ');
                bw.write(en.getKey());
                bw.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.closeAndIgnoreExceptions(bw);
        }
    }

    // TODO: Move to LRU
    public static void loadCache(Map<String, String> cache, File file, int maxSize) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while ((line = br.readLine()) != null) {
                int sepIndex = line.indexOf(" ");
                String hash = line.substring(0, sepIndex);
                String urlExternalForm = line.substring(sepIndex + 1);
                cache.put(urlExternalForm, hash);
            }
        } catch (FileNotFoundException e) {
            // Nothing: New cache will be used.
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.closeAndIgnoreExceptions(br);
        }
    }
}
