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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU (map) cache.
 */
public final class LRUMap<X, Y> extends LinkedHashMap<X, Y> {
    /** Serial version id */
    private static final long serialVersionUID = 1L;

    /** Maximum number of elements */
    private final int mMaxEntries;

    /**
     * Constructor.
     */
    public LRUMap(final int maxEntries) {
        this.mMaxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<X, Y> eldest) {
        return super.size() > mMaxEntries;
    }
}
