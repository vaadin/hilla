package dev.hilla.parser.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Lists {
    public static <T> T getLastElement(List<T> list) {
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    @SafeVarargs
    public static <T> List<T> prepend(List<T> list, T... elements) {
        var newList = new ArrayList<>(Arrays.asList(elements));
        newList.addAll(list);
        return Collections.unmodifiableList(newList);
    }
}
