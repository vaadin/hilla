package dev.hilla.parser.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Lists {
    @SafeVarargs
    public static <T> List<T> append(List<T> list, T... elements) {
        var newList = new ArrayList<>(list);
        newList.addAll(Arrays.asList(elements));
        return Collections.unmodifiableList(newList);
    }

    public static <T> T getLastElement(List<T> list) {
        var size = list.size();
        return size == 0 ? null : list.get(size - 1);
    }
}
