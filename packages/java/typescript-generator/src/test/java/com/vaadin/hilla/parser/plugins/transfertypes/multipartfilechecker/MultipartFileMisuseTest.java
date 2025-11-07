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
package com.vaadin.hilla.parser.plugins.transfertypes.multipartfilechecker;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileCheckerPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileUsageException;
import com.vaadin.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;

public class MultipartFileMisuseTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ThrowWhenMultipartFileIsUsedInEntity() {
        assertThrows(MultipartFileUsageException.class,
                () -> new Parser()
                        .classPath(Set.of(helper.getTargetDir().toString()))
                        .endpointAnnotations(List.of(Endpoint.class))
                        .addPlugin(new BackbonePlugin())
                        .addPlugin(new MultipartFileCheckerPlugin())
                        .execute(List.of(MultipartFileInEntityEndpoint.class)));
    }

    @Test
    public void should_ThrowWhenMultipartFileIsUsedAsReturnType() {
        assertThrows(MultipartFileUsageException.class, () -> new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new MultipartFileCheckerPlugin())
                .execute(List.of(MultipartFileAsReturnTypeEndpoint.class)));
    }
}
