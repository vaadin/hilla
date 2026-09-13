/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.generator.fixtures;

import org.springframework.web.multipart.MultipartFile;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Has a type of its own named after one the browser has, which the generated
 * TypeScript refers to by that name rather than declaring it.
 */
@Endpoint
public class ShadowingProvidedEndpoint {
    public File describe(MultipartFile file) {
        return null;
    }

    /**
     * Named after the type a multipart file is sent as, which the generated
     * file writes as it is.
     */
    public static class File {
        private String name;

        public String getName() {
            return name;
        }
    }
}
