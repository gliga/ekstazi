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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.ekstazi.log.Log;
import org.ekstazi.util.FileUtil;
import org.ekstazi.util.LRUMap;

/**
 * Hashing helper. This class helps with hashing (class) files. The class can be
 * initialized with several hashing algorithms.
 */
public class Hasher {

    /** Hash value in case of an error/exception */
    protected static final String ERR_HASH = "-1";
    
    /** Cache: file->hash (note that we limit the size) */
    protected final Map<String, String> path2Hash;

    /** Instance of CRC32; not null if used */
    private final CRC32 mCRC32;

    /** If not CRC then we use MessageDigest */
    private final MessageDigest mHashAlgorithm;

    /** Flag to indicate that semantic hashing is on */
    protected final boolean mIsSemanticHashing;

    /**
     * Constructor.
     */
    public Hasher(Algorithm algorithm, int cacheSizes, boolean isSemanticHashing) {
        this.path2Hash = new LRUMap<String, String>(cacheSizes);
        this.mCRC32 = algorithm.equals(Algorithm.CRC32) ? new CRC32() : null;
        this.mIsSemanticHashing = isSemanticHashing;

        MessageDigest md = null;
        if (mCRC32 == null) {
            try {
                md = MessageDigest.getInstance(algorithm.name());
            } catch (NoSuchAlgorithmException e) {
                Log.e(algorithm + " not supported.  Will use CRC64.");
            }
        }
        this.mHashAlgorithm = md;
    }

    /**
     * Hashes all resources at the given URLs and returns mapping from file path
     * (for each url) to hash.
     */
    public synchronized Map<String, String> hashURLs(URL[] urls) {
        Map<String, String> hashes = new HashMap<String, String>();
        for (URL url : urls) {
            if (url != null) {
                String externalForm = url.toExternalForm();
                String hash = hashURL(url, externalForm);
                hashes.put(externalForm, hash);
            }
        }
        return hashes;
    }

    public synchronized Map<String, String> hashURLs(Set<URL> urls) {
        Map<String, String> hashes = new HashMap<String, String>();
        for (URL url : urls) {
            if (url != null) {
                String externalForm = url.toExternalForm();
                String hash = hashURL(url, externalForm);
                hashes.put(externalForm, hash);
            }
        }
        return hashes;
    }

    public synchronized Map<String, String> hashExternalForms(String[] externalForms) {
        Map<String, String> hashes = new HashMap<String, String>();
        for (String externalForm : externalForms) {
            if (externalForm != null) {
                try {
                    String hash = hashURL(new URL(externalForm), externalForm);
                    hashes.put(externalForm, hash);
                } catch (MalformedURLException ex) {
                    // Should not happen.
                }
            }
        }
        return hashes;
    }
    
    /**
     * Hashes system environment.
     * 
     * http://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
     */
    public String hashSystemEnv() {
        List<String> system = new ArrayList<String>();
        for (Entry<Object, Object> el : System.getProperties().entrySet()) {
            system.add(el.getKey() + " " + el.getValue());
        }

        Map<String, String> env = System.getenv();
        for (Entry<String, String> el : env.entrySet()) {
            system.add(el.getKey() + " " + el.getValue());
        }
        Collections.sort(system);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DataOutputStream dos = new DataOutputStream(baos);
            for (String s : system) {
                dos.write(s.getBytes());
            }
        } catch (IOException ex) {
            // never
        }
        return hashByteArray(baos.toByteArray());
    }

    /**
     * Hashes string. This method is minor modification of String.hashCode().
     * 
     * @param string
     *            String to hash.
     * @return Hash of the string.
     */
    public static long hashString(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }
    
    /**
     * Hashes file content. Note that the name must be spec (as defined in URL).
     */
    public synchronized String hashURL(String externalForm) {
        try {
            return hashURL(new URL(externalForm), externalForm);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ERR_HASH;
    }
    
    /**
     * Set of supported algorithms.
     */
    public enum Algorithm {
        CRC32, MD5;

        public static Algorithm fromString(String text) {
            if (text != null) {
                for (Algorithm b : Algorithm.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            // Return default value.
            return CRC32;
        }
    }

    // INTERNAL
    
    /**
     * Hash resource at the given URL.
     */
    protected String hashURL(URL url, String externalForm) {
        if (url == null) return ERR_HASH;

        String hash = path2Hash.get(externalForm);
        if (hash != null) {
            return hash;
        }

        if (mIsSemanticHashing) {
            byte[] bytes = FileUtil.loadBytes(url);
            if (bytes == null) return ERR_HASH;
            // Remove debug info from classfiles.
            bytes = BytecodeCleaner.removeDebugInfo(bytes);
            hash = hashByteArray(bytes);
        } else {
            // http://www.oracle.com/technetwork/articles/java/compress-1565076.html
            Checksum cksum = new Adler32();
            byte[] bytes = FileUtil.loadBytes(url, cksum);
            if (bytes == null) return ERR_HASH;
            hash = Long.toString(cksum.getValue());
        }
        path2Hash.put(externalForm, hash);
        return hash;
    }

    /**
     * Hashes content of all files. Note that each file name must be spec (as
     * defined in URL).
     */
    @SuppressWarnings("unused")
    private Map<String, String> hashFiles(String... fileNames) {
        Map<String, String> hashes = new HashMap<String, String>();
        for (int i = 0; i < fileNames.length; i++) {
            String hash = hashURL(fileNames[i]);
            hashes.put(fileNames[i], hash);
        }
        return hashes;
    }

    /**
     * Hashes byte array.
     * 
     * @return Hash of the given byte array.
     */
    protected String hashByteArray(byte[] data) {
        if (mCRC32 != null) {
            return hashCRC32(data);
        } else if (mHashAlgorithm != null) {
            return messageDigest(data);
        } else {
            // Default.
            return hashCRC32(data);
        }
    }

    private String hashCRC32(byte[] data) {
        mCRC32.reset();
        mCRC32.update(data);
        return Long.toString(mCRC32.getValue());
    }

    private String messageDigest(byte[] data) {
        mHashAlgorithm.reset();
        mHashAlgorithm.update(data);
        byte[] thedigest = mHashAlgorithm.digest(data);
        return Arrays.toString(thedigest).replaceAll("\\s+", "");
    }
    
    // MAIN

    public static void main(String[] args) {
        Hasher hashUtil = new Hasher(Algorithm.CRC32, 1000, true);
        String systemEnvHash = hashUtil.hashSystemEnv();
        System.out.println(systemEnvHash);
    }
}
