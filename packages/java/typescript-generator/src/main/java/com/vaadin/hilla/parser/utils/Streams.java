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
package com.vaadin.hilla.parser.utils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * <b>Internal API.</b> Not part of the public Hilla API: used only by Hilla
 * itself, and may change or be removed in any release without notice or
 * deprecation. Do not use it in application code or libraries.
 */
public final class Streams {
    private Streams() {
    }

    @SafeVarargs
    public static <T> Stream<T> combine(Stream<T>... streams) {
        return Stream.of(streams).flatMap(Function.identity());
    }

    @SafeVarargs
    public static <T> Stream<T> combine(T[]... arrays) {
        return Stream.of(arrays).flatMap(Arrays::stream);
    }
}
