/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
