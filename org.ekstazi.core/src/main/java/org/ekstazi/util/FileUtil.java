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

package org.ekstazi.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.ekstazi.log.Log;

/**
 * Utility methods for manipulating files.
 */
public class FileUtil {

    /**
     * Loads bytes of the given file.
     * 
     * @return Bytes of the given file.
     */
    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) {
                throw new IOException("File size >= 2 GB");
            }
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            // Close file
            f.close();
        }
    }

    /**
     * Write bytes to the given file.
     */
    public static void writeFile(File file, byte[] bytes) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    /**
     * Checks if tool jar is newer than JUnit jar. If tool jar is newer, return
     * true; false otherwise.
     */
    public static boolean isSecondNewerThanFirst(File first, File second) {
        boolean isSecondNewerThanFirst = false;
        // If file does not exist, it cannot be newer.
        if (second.exists()) {
            long firstLastModified = first.lastModified();
            long secondLastModified = second.lastModified();
            // 0L is returned if there was any error, so we check if both last
            // modification stamps are != 0L.
            if (firstLastModified != 0L && secondLastModified != 0L) {
                isSecondNewerThanFirst = secondLastModified > firstLastModified;
            }
        }
        return isSecondNewerThanFirst;
    }

    /**
     * Closes reader (if not null) and ignores any exception that happened
     * during invocation of close method. This method should only be used in
     * finally blocks when streams are closed and there is nothing to be done if
     * exception is thrown at that point.
     */
    public static void closeAndIgnoreExceptions(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            // Nothing.
        }
    }

    public static void closeAndIgnoreExceptions(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            // Nothing.
        }
    }
    
    /**
     * Closes input stream and ignores any exception that happened while closing
     * the stream. This method should only be used in finally blocks when
     * streams are closed and there is nothing to be done if exception is thrown
     * at that point.
     */
    public static void closeAndIgnoreExceptions(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // Nothing.
        }
    }

    public static void closeAndIgnoreExceptions(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            // Nothing.
        }
    }
    
    /**
     * Returns them when the resources (described with the given external form)
     * is last modified. If any error happens, the resulting value is 0.
     */
    public static long getURLLastModified(String externalForm) {
        long lastModified = 0L;
        InputStream is = null;
        try {
            URL url = new URL(externalForm);
            URLConnection connection = url.openConnection();
            lastModified = connection.getLastModified();
            is = connection.getInputStream();
        } catch (MalformedURLException e) {
            Log.e("Incorrect URL", e);
        } catch (IOException e) {
            // This can happen also if file does not exist.
        } finally {
            closeAndIgnoreExceptions(is);
        }
        return lastModified;
    }
    
    /**
     * Load bytes from the given url.
     * 
     * @param url
     *            Local of the file to load.
     * @return Bytes loaded from the given url.
     */
    public static byte[] loadBytes(URL url) {
        byte[] bytes = null;
        try {
            bytes = loadBytes(url.openStream());
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
        return bytes;
    }
    
    public static byte[] loadBytes(URL url, Checksum cksum) {
        byte[] bytes = null;
        try {
            bytes = loadBytes(new CheckedInputStream(url.openStream(), cksum));
        } catch (IOException ex) {
            // Nothing.
        }
        return bytes;
    }

    public static byte[] loadBytes(InputStream stream) throws IOException {
        return loadBytes(stream, true);
    }
    
    public static byte[] loadBytes(InputStream stream, boolean closeStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        int n;
        while ((n = stream.read(b)) != -1) {
            out.write(b, 0, n);
        }
        if (closeStream) {
            stream.close();
        }
        out.close();
        return out.toByteArray();
    }

    public static void copyBytes(File input, File output) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(output);
            copyBytes(fis, fos);
        } finally {
            closeAndIgnoreExceptions(fis);
            closeAndIgnoreExceptions(fos);
        }
    }

    public static void copyBytes(InputStream is, OutputStream os) throws IOException {
        byte[] b = new byte[1000];
        int n;
        while ((n = is.read(b)) != -1) {
            os.write(b, 0, n);
        }
    }

    public static String[] readLines(File f) throws IOException {
        if (f.exists()) {
            return readLines(new FileReader(f));
        } else {
            return new String[0];
        }
    }

    public static String[] readLines(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        List<String> lines = new ArrayList<String>();
        try {
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                lines.add(line);
            }
        } finally {
            closeAndIgnoreExceptions(br);
        }
        return lines.toArray(new String[0]);
    }

    public static void writeLines(File results, String[] lines) throws FileNotFoundException {
        writeLines(results, Arrays.asList(lines));
    }

    public static void writeLines(File results, List<String> lines) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(results);
        for (String line : lines) {
            pw.println(line);
        }
        pw.close();
    }

    /**
     * Deletes given directory (and does not reason about symlinks). If the
     * given directory does not exist, this method does nothing.
     * 
     * @param dir
     *            Directory to delete
     */
    public static void deleteDirectory(File dir) {
        if (!dir.exists()) {
            return;
        }

        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectory(child);
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

}
