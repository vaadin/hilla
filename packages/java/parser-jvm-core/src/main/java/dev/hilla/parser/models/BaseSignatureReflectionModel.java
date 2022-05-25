package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;

final class BaseSignatureReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedType>
        implements BaseSignatureModel, ReflectionSignatureModel {
    private final Class<?> inner;

    public BaseSignatureReflectionModel(AnnotatedType origin) {
        super(origin);
        inner = (Class<?>) origin.getType();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BaseSignatureModel)) {
            return false;
        }

        var other = (BaseSignatureModel) obj;

        return inner.equals(other.getType())
                && Objects.equals(getAnnotations(), other.getAnnotations());
    }

    @Override
    public Class<?> getType() {
        return inner;
    }

    @Override
    public int hashCode() {
        return 7 + inner.hashCode();
    }

    @Override
    public boolean isBoolean() {
        return inner == Boolean.TYPE;
    }

    @Override
    public boolean isByte() {
        return inner == Byte.TYPE;
    }

    @Override
    public boolean isCharacter() {
        return inner == Character.TYPE;
    }

    @Override
    public boolean isDouble() {
        return inner == Double.TYPE;
    }

    @Override
    public boolean isFloat() {
        return inner == Float.TYPE;
    }

    @Override
    public boolean isInteger() {
        return inner == Integer.TYPE;
    }

    @Override
    public boolean isJDKClass() {
        return true;
    }

    @Override
    public boolean isLong() {
        return inner == Long.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        return inner != Void.TYPE;
    }

    @Override
    public boolean isShort() {
        return inner == Short.TYPE;
    }

    @Override
    public boolean isVoid() {
        return inner == Void.TYPE;
    }

    static class Bare extends AbstractAnnotatedReflectionModel<Class<?>>
            implements BaseSignatureModel, ReflectionSignatureModel {
        public Bare(Class<?> origin) {
            super(origin);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof BaseSignatureModel)) {
                return false;
            }

            var other = (BaseSignatureModel) obj;

            return origin.equals(other.getType())
                    && getAnnotations().equals(other.getAnnotations());
        }

        @Override
        public Class<?> getType() {
            return origin;
        }

        @Override
        public int hashCode() {
            return 7 + origin.hashCode();
        }

        @Override
        public boolean isBoolean() {
            return origin == Boolean.TYPE;
        }

        @Override
        public boolean isByte() {
            return origin == Byte.TYPE;
        }

        @Override
        public boolean isCharacter() {
            return origin == Character.TYPE;
        }

        @Override
        public boolean isDouble() {
            return origin == Double.TYPE;
        }

        @Override
        public boolean isFloat() {
            return origin == Float.TYPE;
        }

        @Override
        public boolean isInteger() {
            return origin == Integer.TYPE;
        }

        @Override
        public boolean isJDKClass() {
            return true;
        }

        @Override
        public boolean isLong() {
            return origin == Long.TYPE;
        }

        @Override
        public boolean isPrimitive() {
            return origin != Void.TYPE;
        }

        @Override
        public boolean isShort() {
            return origin == Short.TYPE;
        }

        @Override
        public boolean isVoid() {
            return origin == Void.TYPE;
        }
    }
}
