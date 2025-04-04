package com.vaadin.hilla.runtime.transfertypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import com.vaadin.hilla.transfertypes.annotations.ForType;
import com.vaadin.hilla.transfertypes.annotations.FromModule;

public final class TransferTypes {
    public static Optional<Class<?>> intoTransferClass(Class<?> cls) {
        return Arrays.stream(TransferTypes.class.getDeclaredClasses())
            .filter(c -> c.getDeclaredAnnotation(ForType.class).value().equals(cls.getName()))
            .findFirst();
    }

    public static Optional<Class<?>> fromTransferClass(Class<?> cls) {
        return Arrays.stream(TransferTypes.class.getDeclaredClasses())
            .filter(cls::equals)
            .findFirst()
            .map(c -> c.getDeclaredAnnotation(ForType.class).value())
            .map(name -> {
                try {
                    return TransferTypes.class.getClassLoader().loadClass(name);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            });
    }

    @ForType("org.springframework.data.domain.Sort$Order")
    @FromModule(module = "@vaadin/hilla-frontend", namedSpecifier = "SortOrder")
    public class $9c2aa17f {}

    @ForType("reactor.core.publisher.Flux")
    @FromModule(module = "@vaadin/hilla-frontend", namedSpecifier = "Flux")
    public static final class $812d3f2a<T> extends ArrayList<T> {
    }

    @ForType("org.springframework.web.multipart.MultipartFile")
    @FromModule(module = "web:std", namedSpecifier = "File")
    public static final class $8a8482dd {
    }

    @ForType("com.vaadin.hilla.signals.ListSignal")
    @FromModule(module = "@vaadin/hilla-react-signal", namedSpecifier = "ListSignal")
    public static final class $b6fbd871 {
    }

    @ForType("com.vaadin.hilla.signals.NumberSignal")
    @FromModule(module = "@vaadin/hilla-react-signal", namedSpecifier = "NumberSignal")
    public static final class $6301ce92 {
    }

    @ForType("com.vaadin.hilla.signals.ValueSignal")
    @FromModule(module = "@vaadin/hilla-react-signal", namedSpecifier = "ValueSignal")
    public static final class $56b66f91 {
    }

    @ForType("com.vaadin.hilla.signals.Signal")
    @FromModule(module = "@vaadin/hilla-react-signal", namedSpecifier = "Signal")
    public static final class $1e7b1b84 {
    }
}
