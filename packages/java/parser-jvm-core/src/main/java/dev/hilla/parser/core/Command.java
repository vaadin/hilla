package dev.hilla.parser.core;

import dev.hilla.parser.models.Model;

interface Command {
    default Model[] getContent() {
        return null;
    }

    class Add implements Command {
        private final Model[] content;

        Add(Model[] content) {
            this.content = content;
        }

        @Override
        public Model[] getContent() {
            return content;
        }
    }

    class Remove implements Command {
        Remove() {
        }
    }

    class Replace implements Command {
        private final Model[] content;

        Replace(Model[] content) {
            this.content = content;
        }

        @Override
        public Model[] getContent() {
            return content;
        }
    }
}
