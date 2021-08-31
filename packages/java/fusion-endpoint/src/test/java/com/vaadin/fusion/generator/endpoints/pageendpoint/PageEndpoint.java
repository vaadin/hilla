/*
 * Copyright 2000-2021 Vaadin Ltd.
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

package com.vaadin.fusion.generator.endpoints.pageendpoint;

import java.util.Arrays;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Endpoint
@AnonymousAllowed
public class PageEndpoint {

    public static class Foo {
        private String value;

        public Foo(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Page<Foo> getPageOfObjects() {
        return new PageImpl<Foo>(Arrays.asList(new Foo("A"), new Foo("B")));
    }

    public Page<String> getPageOfStrings() {
        return new PageImpl<String>(Arrays.asList("A", "B"));
    }
}
