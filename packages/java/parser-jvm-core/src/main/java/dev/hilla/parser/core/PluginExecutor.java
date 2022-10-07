package dev.hilla.parser.core;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PluginExecutor {
    private static final Logger logger = LoggerFactory
            .getLogger(PluginExecutor.class);
    private final Set<NodePath<?>> enqueued = new HashSet<>();
    private final Plugin plugin;
    private final Deque<Task> queue = new LinkedList<>();
    private final RootNode rootNode;
    private final Map<Node<?, ?>, NodeScanResult> scanResults = new HashMap<>();

    public PluginExecutor(@Nonnull Plugin plugin, @Nonnull RootNode rootNode) {
        this.plugin = Objects.requireNonNull(plugin);
        this.rootNode = Objects.requireNonNull(rootNode);
    }

    public void execute() {
        var rootPath = NodePath.forRoot(rootNode);
        enqueueEnterFirst(rootPath);
        while (!queue.isEmpty()) {
            queue.removeFirst().execute();
        }
    }

    private void enqueueEnterFirst(NodePath<?> path) {
        if (enqueued.contains(path)) {
            return;
        }

        queue.addFirst(new EnterTask(path));
        enqueued.add(path);
    }

    private void enqueueEnterLast(NodePath<?> path) {
        if (enqueued.contains(path)) {
            return;
        }

        var lastTask = queue.removeLast();
        queue.addLast(new EnterTask(path));
        queue.addLast(lastTask);
        enqueued.add(path);
    }

    private void enqueueExitFirst(NodePath<?> path) {
        queue.addFirst(new ExitTask(path));
    }

    @Nonnull
    private NodeScanResult scanNodeDependencies(Node<?, ?> node) {
        return scanResults.computeIfAbsent(node,
                n -> new NodeScanResult(plugin.scan(new NodeDependencies(n,
                        Stream.empty(), Stream.empty()))));
    }

    private class EnterTask extends Task {
        public EnterTask(@Nonnull NodePath<?> path) {
            super(path);
        }

        @Override
        void execute() {
            var scanResult = scanNodeDependencies(getPath().getNode());
            plugin.enter(getPath());

            PluginExecutor.this.enqueueExitFirst(getPath());

            var reverseChildList = new LinkedList<NodePath<?>>();
            scanResult.getChildNodes().stream()
                    .map(node -> plugin.resolve(node, getPath()))
                    .map(getPath()::withChildNode)
                    .forEachOrdered(reverseChildList::addFirst);
            reverseChildList.forEach(PluginExecutor.this::enqueueEnterFirst);

            scanResult.getRelatedNodes().stream()
                    .map(node -> plugin.resolve(node, getPath().getRootPath()))
                    .map(getPath().getRootPath()::withChildNode)
                    .forEachOrdered(PluginExecutor.this::enqueueEnterLast);
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

    private static class NodeScanResult {
        private final List<Node<?, ?>> childNodes;
        private final Node<?, ?> node;
        private final List<Node<?, ?>> relatedNodes;

        public NodeScanResult(@Nonnull NodeDependencies nodeDependencies) {
            Objects.requireNonNull(nodeDependencies);
            this.node = nodeDependencies.getNode();
            this.childNodes = nodeDependencies.getChildNodes()
                    .collect(Collectors.toList());
            this.relatedNodes = nodeDependencies.getRelatedNodes()
                    .collect(Collectors.toList());
        }

        @Nonnull
        public List<Node<?, ?>> getChildNodes() {
            return childNodes;
        }

        @Nonnull
        public Node<?, ?> getNode() {
            return node;
        }

        @Nonnull
        public List<Node<?, ?>> getRelatedNodes() {
            return relatedNodes;
        }
    }

    private static abstract class Task {
        private final NodePath<?> path;

        public Task(@Nonnull NodePath<?> path) {
            this.path = Objects.requireNonNull(path);
        }

        protected NodePath<?> getPath() {
            return path;
        }

        abstract void execute();
    }
}
