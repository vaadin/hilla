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
package com.vaadin.hilla.generator.typescript;

/**
 * What the writers agree on about the shape of the files rather than their
 * content.
 */
final class Layout {
    /**
     * The width beyond which what would be one line goes on lines of its own,
     * keeping a long signature or a long import readable. Same as the width the
     * sources of the project are formatted to.
     */
    static final int MAX_WIDTH = 120;

    private Layout() {
    }
}
