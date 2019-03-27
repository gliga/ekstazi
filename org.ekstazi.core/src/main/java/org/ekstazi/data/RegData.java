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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Regression data loaded for each file/url.
 */
public final class RegData implements Serializable {

    /** Serial version id */
    private static final long serialVersionUID = 1L;

    /** String representation of url */
    private final String mURLExternalForm;

    /** Hash of the resource at the url */
    private final String mHash;

    /**
     * Constructor.
     */
    public RegData(String urlExternalForm, String hash) {
        this.mURLExternalForm = urlExternalForm;
        this.mHash = hash;
    }

    public String getURLExternalForm() {
        return mURLExternalForm;
    }

    public String getHash() {
        return mHash;
    }

    /**
     * Simple comparator for regression data. This comparator
     * orders data based on url external form.
     */
    public static class RegComparator implements Comparator<RegData>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(RegData o1, RegData o2) {
            return o1.getURLExternalForm().compareTo(o2.getURLExternalForm());
        }
    }

    @Override
    public String toString() {
        return mURLExternalForm + ":" + mHash;
    }
}
