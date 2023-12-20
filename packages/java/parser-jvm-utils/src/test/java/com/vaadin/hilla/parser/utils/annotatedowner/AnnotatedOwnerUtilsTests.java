package com.vaadin.hilla.parser.utils.annotatedowner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.vaadin.hilla.parser.utils.AnnotatedOwnerUtils;

public class AnnotatedOwnerUtilsTests {

    @DisplayName("It should get annotation for the base part as expected for JDK17")
    @ParameterizedTest(name = Provider.testNamePattern)
    @ArgumentsSource(Provider.class)
    public void should_GetAnnotationsForBase(AnnotatedType type, String name,
            boolean hasTwoAnnotations) {
        var typeIndex = 0;
        var ownerIndex = 1;

        var list = AnnotatedOwnerUtils.getAllOwnersAnnotations(type);

        assertEquals(Foo.class, list.get(typeIndex)[0].annotationType());

        if (hasTwoAnnotations) {
            var ownerAnnotations = list.get(ownerIndex);
            assertEquals(1, ownerAnnotations.length);
            assertEquals(Bar.class, ownerAnnotations[0].annotationType());
        } else {
            assertEquals(0, list.get(ownerIndex).length);
        }
    }

    @DisplayName("It should not get annotation for the grand owner part as expected for JDK17")
    @ParameterizedTest(name = Provider.testNamePattern)
    @ArgumentsSource(Provider.ThreeParts.class)
    public void should_NotGetAnnotationsForGrandOwner(AnnotatedType type,
            String name, boolean hasTwoAnnotations) {
        var grandOwnerIndex = 2;

        var list = AnnotatedOwnerUtils.getAllOwnersAnnotations(type);

        assertEquals(0, list.get(grandOwnerIndex).length);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Bar {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Foo {
    }

    class DYN_Param<P> {
        class DYN_CParam<C> {
        }

        class DYN_CSimple {
        }
    }

    class DYN_Simple {
        class DYN_CParam<C> {
        }

        class DYN_CSimple {
        }
    }

    static class Provider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        private static final List<String> fieldsWithTwoAnnotations = List.of(
                "ss_ds", "ss_dp", "sp_ds", "sp_dp", "gs_ds", "gs_dp", "gp_ds",
                "gp_dp");

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext extensionContext) {
            return Arrays.stream(Sample.class.getDeclaredFields())
                    .map(field -> Arguments.of(field.getAnnotatedType(),
                            field.getName(), fieldsWithTwoAnnotations
                                    .contains(field.getName())));
        }

        static final class ThreeParts extends Provider
                implements ArgumentsProvider {
            private static final List<String> fields = List.of("gs_ds", "gs_dp",
                    "gp_ds", "gp_dp", "gs_ss", "gs_sp", "gp_ss", "gp_sp");

            @Override
            public Stream<? extends Arguments> provideArguments(
                    ExtensionContext context) {
                return super.provideArguments(context).filter(
                        args -> !fields.contains((String) args.get()[1]));
            }
        }
    }

    static class ST_Param<P> {
        class DYN_CParam<C> {
        }

        class DYN_CSimple {
        }

        static class ST_CParam<C> {
        }

        static class ST_CSimple {
        }
    }

    static class ST_Simple {
        class DYN_CParam<C> {
        }

        class DYN_CSimple {
        }

        static class ST_CParam<C> {
        }

        static class ST_CSimple {
        }
    }

    static class Sample {
        DYN_Param<Integer>.@Foo DYN_CParam<Integer> dp_dp; // me
        DYN_Param<Integer>.@Foo DYN_CSimple dp_ds; // me
        DYN_Simple.@Foo DYN_CParam<Integer> ds_dp; // me
        DYN_Simple.@Foo DYN_CSimple ds_ds; // me
        @Bar
        GLOB_Param<Integer>.@Foo DYN_CParam<Integer> gp_dp; // me me
        @Bar
        GLOB_Param<Integer>.@Foo DYN_CSimple gp_ds; // me me
        GLOB_Param.@Foo ST_CParam<Integer> gp_sp; // parent
        GLOB_Param.@Foo ST_CSimple gp_ss; // both
        @Bar
        GLOB_Simple.@Foo DYN_CParam<Integer> gs_dp; // me me
        @Bar
        GLOB_Simple.@Foo DYN_CSimple gs_ds; // me me
        GLOB_Simple.@Foo ST_CParam<Integer> gs_sp; // parent
        GLOB_Simple.@Foo ST_CSimple gs_ss; // both
        @Bar
        ST_Param<Integer>.@Foo DYN_CParam<Integer> sp_dp; // parent
        @Bar
        ST_Param<Integer>.@Foo DYN_CSimple sp_ds; // parent
        ST_Param.@Foo ST_CParam<Integer> sp_sp; // parent
        ST_Param.@Foo ST_CSimple sp_ss; // both
        @Bar
        ST_Simple.@Foo DYN_CParam<Integer> ss_dp; // me
        @Bar
        ST_Simple.@Foo DYN_CSimple ss_ds; // me
        ST_Simple.@Foo ST_CParam<Integer> ss_sp; // parent
        ST_Simple.@Foo ST_CSimple ss_ss; // both
    }
}
