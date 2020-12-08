/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.generator.endpoints.selfreference;

import com.vaadin.flow.server.connect.generator.endpoints.AbstractEndpointGenerationTest;

import java.util.Arrays;
import org.junit.Test;

public class SelfReferenceTest extends AbstractEndpointGenerationTest {

  public SelfReferenceTest() {
    super(Arrays.asList(SelfReferenceEndpoint.class, SelfReference.class));
  }

  @Test
  public void should_NotImportSelfInTS_When_ReferencingSelfInModel() {
    verifyOpenApiObjectAndGeneratedTs();
  }
}
