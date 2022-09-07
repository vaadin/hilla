package dev.hilla.parser.core;

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.RootNode;
import dev.hilla.parser.node.NodePath;
import dev.hilla.parser.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginExecutor {
    private static final Logger logger =
        LoggerFactory.getLogger(PluginExecutor.class);

    private final Plugin plugin;
    private final RootNode rootNode;
    private final Set<NodePath<?>> enqueued = new HashSet<>();
    private final Queue<Task> queue = new LinkedList<>();

    public PluginExecutor(@Nonnull Plugin plugin, @Nonnull RootNode rootNode) {
        this.plugin = Objects.requireNonNull(plugin);
        this.rootNode = Objects.requireNonNull(rootNode);
    }

    public void execute() {
        var rootPath = dev.hilla.parser.node.NodePath.of(rootNode);
        enqueueEnter(rootPath);
        queue.iterator().forEachRemaining(Task::execute);
        while (!queue.isEmpty()) {
            queue.remove().execute();
        }
    }

    private abstract class Task {
        private final NodePath<?> path;

        public Task(@Nonnull NodePath<?> path) {
            this.path = Objects.requireNonNull(path);
        }

        protected NodePath<?> getPath() {
            return path;
        }

        abstract void execute();
    }

    private void enqueueEnter(NodePath<?> path) {
        queue.add(new EnterTask(path));
        enqueued.add(path);
    }

    private void enqueueExit(NodePath<?> path) {
        queue.add(new ExitTask(path));
    }

    private class EnterTask extends Task {
        public EnterTask(@Nonnull NodePath<?> path) {
            super(path);
        }

        @Override
        void execute() {
            var dependencies = plugin.scan(
                new NodeDependencies(getPath().getNode(), Stream.empty()));
            plugin.enter(getPath());
            var paths = dependencies.getDependencies()
                .filter(Predicate.not(enqueued::contains)).distinct();
            paths.filter(this::isChildPath).forEachOrdered(PluginExecutor.this::enqueueEnter);
            PluginExecutor.this.enqueueExit(getPath());
            paths.filter(Predicate.not(this::isChildPath)).forEachOrdered(PluginExecutor.this::enqueueEnter);
        }

        private boolean isChildPath(NodePath<?> path) {
            return path.getParentPath().equals(getPath());
        }
    }

    private class ExitTask extends Task {
        public ExitTask(@Nonnull NodePath<?> path) {
            super(path);
        }

        void execute() {
            plugin.exit(getPath());
        }
    }
}
