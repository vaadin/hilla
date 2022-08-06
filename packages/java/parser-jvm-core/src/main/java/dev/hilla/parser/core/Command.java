package dev.hilla.parser.core;

import dev.hilla.parser.models.Model;

public abstract class Command {
    private static final Model[] EMPTY = new Model[0];

    public static Command ADD(Model... models) {
        return new Add(models);
    }

    public static Command DO_NOTHING() {
        return new DoNothing();
    }

    public static Command REMOVE() {
        return new Remove();
    }

    public static Command REPLACE(Model... with) {
        return new Replace(with);
    }

    static boolean isRemovingCommand(Command command) {
        return command instanceof Command.Remove
                || command instanceof Command.Replace;
    }

    Model[] getContent() {
        return EMPTY;
    }

    static class Add extends Command {
        private final Model[] content;

        private Add(Model[] content) {
            this.content = content;
        }

        @Override
        Model[] getContent() {
            return content;
        }
    }

    static class DoNothing extends Command {
        private DoNothing() {
        }
    }

    static class Remove extends Command {
        private Remove() {
        }
    }

    static class Replace extends Command {
        private final Model[] content;

        private Replace(Model[] content) {
            this.content = content;
        }

        @Override
        Model[] getContent() {
            return content;
        }
    }
}
