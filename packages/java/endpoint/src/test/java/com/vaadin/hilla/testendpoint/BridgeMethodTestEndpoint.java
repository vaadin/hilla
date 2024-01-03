/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.hilla.testendpoint;

import com.vaadin.hilla.Endpoint;

/**
 * Test case for https://github.com/vaadin/vaadin-connect/issues/162
 */
public class BridgeMethodTestEndpoint {

    public interface TestInterface<T extends TestInterface2> {
        default T testMethodFromInterface(T a) {
            return null;
        }

        int testNormalMethod(int value);
    }

    public interface TestInterface2 {
        String getId();
    }

    public static class TestInterface2Impl implements TestInterface2 {
        public String id;

        @Override
        public String getId() {
            return id;
        }
    }

    public static class MySecondClass<E> {
        public int testMethodFromClass(E value) {
            return 0;
        }
    }

    @Endpoint
    public static class InheritedClass extends MySecondClass<Integer>
            implements TestInterface<TestInterface2Impl> {
        @Override
        public TestInterface2Impl testMethodFromInterface(
                TestInterface2Impl testInterface2) {
            return testInterface2;
        }

        @Override
        public int testMethodFromClass(Integer value) {
            return value;
        }

        @Override
        public int testNormalMethod(int value) {
            return value;
        }
    }
}
