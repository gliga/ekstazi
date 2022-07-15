/*
 * Copyright 2022-present Milos Gligoric
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

package org.ekstazi.junit;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;

public class JUnit5Monitor {
    /**
     * Register CoverageExtension to instrument all test classes.
     * @param parentRegistry
     * @param extensionTypes
     * @return
     */
    public static MutableExtensionRegistry createRegistryFrom(MutableExtensionRegistry parentRegistry,
            Stream<Class<? extends Extension>> extensionTypes) {
        Stream<Class<? extends Extension>> newExtensionTypes = Stream.concat(extensionTypes,
                Stream.of(CoverageExtension.class));
        return MutableExtensionRegistry.createRegistryFrom(parentRegistry, newExtensionTypes);
    }
}
