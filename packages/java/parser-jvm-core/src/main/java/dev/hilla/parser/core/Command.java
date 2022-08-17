package dev.hilla.parser.core;

import java.util.Arrays;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.Model;

interface Command {
    default Model[] getContent() {
        return null;
    }

    class Skip implements Command {
        private final Visitor visitor;

        Skip(Visitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            return obj != null && getClass() == obj.getClass();
        }

        public Visitor getVisitor() {
            return visitor;
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }
    }

    class Add implements Command {
        private final Model[] content;

        Add(Model[] content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            return Arrays.equals(content, ((Add) obj).content);
        }

        @Override
        public Model[] getContent() {
            return content;
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode()
                    + 7 * Arrays.hashCode(content);
        }
    }

    class Remove implements Command {
        Remove() {
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            return obj != null && getClass() == obj.getClass();
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }
    }

    class Replace implements Command {
        private final Model[] content;

        Replace(@Nonnull Model[] content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            return Arrays.equals(content, ((Replace) obj).content);
        }

        @Override
        public Model[] getContent() {
            return content;
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode()
                    + 7 * Arrays.hashCode(content);
        }
    }
}
