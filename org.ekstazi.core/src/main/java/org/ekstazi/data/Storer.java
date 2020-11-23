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

package org.ekstazi.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Set;

import org.ekstazi.Config;
import org.ekstazi.hash.Hasher;
import org.ekstazi.log.Log;

/**
 * API for storing/reading dependencies. Currently there is no
 * enforcement, but it is expected that subclasses write/read
 * magic/version sequence as the first several bytes/characters.
 */
public abstract class Storer {

    /**
     * Storing mode.
     */
    public enum Mode {
        TXT("# 1"), PREFIX_TXT("# 4");

        /** Magic/version sequence */
        private final String mMagicSequence;

        /**
         * Constructor.
         * 
         * @param magicSequence
         *            Magic/version sequence associated with mode.
         */
        private Mode(String magicSequence) {
            this.mMagicSequence = magicSequence;
        }

        /**
         * Returns magic/version sequence for this mode.
         * 
         * @return Magic/version sequence.
         */
        public String getMagicSequence() {
            return mMagicSequence;
        }

        /**
         * This method guarantees to return a correct mode if one is not cannot
         * be parsed.
         * 
         * @param text
         *            Mode as provides in configuration.
         * 
         * @return Storing mode.
         */
        public static Mode fromString(String text) {
            if (text != null) {
                for (Mode b : Mode.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            return TXT;
        }
    }

    /** Mode of this storer */
    protected final Mode mMode;

    /**
     * Constructor.
     */
    protected Storer(Mode mode) {
        this.mMode = mode;
    }

    /**
     * Loads regression data.
     */
    public final Set<RegData> load(String dirName, String fullName) {
        return load(openFileRead(dirName, fullName, fullName, null));
    }

    /**
     * Loads regression data.
     */
    public final Set<RegData> load(String dirName, String className, String methodName) {
        String fullName = className + '.' + methodName;
        return load(openFileRead(dirName, fullName, className, methodName));
    }

    /**
     * Saves regression data.
     */
    public final void save(String dirName, String fullName, Set<RegData> hashes) {
        // @Research(if statement).
        if (!Config.X_DEPENDENCIES_SAVE_V) {
            return;
        }
        // Ensure that the directory for coverage exists.
        new File(dirName).mkdirs();
        save(openFileWrite(dirName, fullName, fullName, null), hashes);
    }

    /**
     * Saves regression data.
     */
    public final void save(String dirName, String className, String methodName, Set<RegData> regData) {
        // @Research(if statement).
        if (!Config.X_DEPENDENCIES_SAVE_V) {
            return;
        }
        // Ensure that the directory for coverage exists.
        new File(dirName).mkdir();
        String fullName = className + '.' + methodName;
        save(openFileWrite(dirName, fullName, className, methodName), regData);
    }

    /**
     * Loading actual data from the given stream. Implementation in subclasses
     * should have matching load and save methods.
     * 
     * @param fis
     *            Stream that contains regression information.
     * @return Regression data.
     */
    protected abstract Set<RegData> extendedLoad(FileInputStream fis);

    /**
     * Saving regression data to the given stream. Implementation in subclasses
     * should have matching load and save methods.
     * 
     * @param fos
     *            Stream that stores regression information.
     * @param hashes
     *            Regression info as mapping URL(ExternalForm)->hash.
     */
    protected abstract void extendedSave(FileOutputStream fos, Set<RegData> hashes);

    // INTERNAL

    private final Set<RegData> load(FileInputStream fis) {
        if (fis != null) {
            return extendedLoad(fis);
        } else {
            return Collections.emptySet();
        }
    }

    private final void save(FileOutputStream fos, Set<RegData> hashes) {
        if (fos != null) {
            extendedSave(fos, hashes);
        }
    }

    /**
     * Opens {@link FileInputStream}. This method checks if file name is too
     * long and hashes the file name. If there are some other problems, the
     * method gives up and returns null.
     * 
     * @param dirName
     *            Destination directory.
     * @param fullName
     *            Name of the file.
     * @return Opened {@link FileInputStream} or null if operation was not
     *         successful.
     */
    private static FileInputStream openFileRead(String dirName, String fullName, String firstPart, String secondPart) {
        try {
            return new FileInputStream(new File(dirName, fullName));
        } catch (FileNotFoundException ex1) {
            // If file name is too long hash it and try again.
            String message = ex1.getMessage();
            if (message != null && message.contains("File name too long")) {
                if (secondPart != null) {
                    long hashedSecondPart = Hasher.hashString(secondPart);
                    fullName = firstPart + "." + Long.toString(hashedSecondPart);
                    // Note that we pass fullName as the second argument too.
                    return openFileRead(dirName, fullName, fullName, null);
                } else if (firstPart != null) {
                    long hashedFirstPart = Hasher.hashString(firstPart);
                    fullName = Long.toString(hashedFirstPart);
                    return openFileRead(dirName, fullName, null, null);
                } else {
                    // No hope.
                    Log.w("Could not open file for reading (name too long) " + fullName);
                }
            }
            return null;
        }
    }

    /**
     * Opens {@link FileOutputStream}. This method checks if file name is too
     * long and hashes the name of the file. If there are other problems, this
     * method gives up and returns null.
     * 
     * @param dirName
     *            Destination directory.
     * @param fullName
     *            File name.
     * @return {@link FileOutputStream} or null if operation was not successful.
     */
    private static FileOutputStream openFileWrite(String dirName, String fullName, String firstPart, String secondPart) {
        try {
            return new FileOutputStream(new File(dirName, fullName));
        } catch (FileNotFoundException ex1) {
            // If file name is too long hash it and try again.
            String message = ex1.getMessage();
            if (message != null && message.contains("File name too long")) {
                if (secondPart != null) {
                    long hashedSecondPart = Hasher.hashString(secondPart);
                    fullName = firstPart + "." + Long.toString(hashedSecondPart);
                    // Invoke again with firstPart.HASH(secondPart).
                    return openFileWrite(dirName, fullName, fullName, null);
                } else if (firstPart != null) {
                    long hashedFirstPart = Hasher.hashString(firstPart);
                    fullName = Long.toString(hashedFirstPart);
                    return openFileWrite(dirName, fullName, null, null);
                } else {
                    // No hope.
                    Log.w("Could not open file for writing (name too long) " + fullName);
                }
            } else {
                Log.w("Could not open file for writing " + fullName + " " + ex1.getMessage());
            }
            return null;
        }
    }
}
