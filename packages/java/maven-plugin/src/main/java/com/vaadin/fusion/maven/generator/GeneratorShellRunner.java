package com.vaadin.fusion.maven.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class GeneratorShellRunner {
    private final List<String> arguments = new ArrayList<>();
    private final Log logger;

    public GeneratorShellRunner(List<String> executable, Log logger) {
        this.logger = logger;

        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            arguments.add("cmd.exe");
            arguments.add("/c");
        } else {
            arguments.add("bash");
            arguments.add("-c");
        }

        arguments.addAll(executable);
    }

    public void add(String... args) {
        arguments.addAll(List.of(args));
    }

    public void run() throws InterruptedException, IOException {
        logger.info(String.format("Running command: %s",
                String.join(" ", arguments)));

        var builder = new ProcessBuilder().command(arguments);
        var mapper = new ObjectMapper(new JsonFactory());

        var process = builder.start();

        var logWatcher = new LogWatcher(process,
                new Handler(LogStreamType.INPUT, logger, mapper),
                new Handler(LogStreamType.ERROR, logger, mapper), logger);

        logWatcher.start();

        var exitCode = process.waitFor();

        if (exitCode == 0) {
            logger.info("The Generator process finished with the exit code "
                    + exitCode);
        } else {
            throw new GeneratorException(
                    "Generator execution failed with exit code " + exitCode);
        }
    }

    private enum Level {
        TRACE(10), DEBUG(20), INFO(30), WARN(40), ERROR(50), FATAL(60);

        private final int id;

        Level(int id) {
            this.id = id;
        }

        @JsonValue
        public int getId() {
            return id;
        }
    }

    private enum LogStreamType {
        ERROR, INPUT
    }

    private static final class Handler {
        private final List<String> collector = new ArrayList<>();
        private final Log logger;
        private final ObjectReader reader;
        private final LogStreamType type;
        private final ObjectWriter writer;

        public Handler(LogStreamType type, Log logger, ObjectMapper mapper) {
            this.logger = logger;
            this.reader = mapper.reader();
            this.type = type;
            writer = mapper.writer().withDefaultPrettyPrinter();
        }

        public void processLine(String line) throws IOException {
            if (line.startsWith("{")) {
                clearCollector();
                processJSONLog(line);
            } else {
                processRegularLog(line);
            }
        }

        public void finish() {
            clearCollector();
        }

        private void clearCollector() {
            if (collector.size() > 0) {
                var message = String.join("\n", collector);

                if (type == LogStreamType.ERROR) {
                    logger.error(message);
                } else {
                    logger.debug(message);
                }

                collector.clear();
            }
        }

        private String createMessage(Record record)
                throws JsonProcessingException {
            return "[GENERATOR] " + record.getMsg() + "\n\n"
                    + writer.writeValueAsString(record.getDynamic());
        }

        private void processJSONLog(String line) throws IOException {
            var record = reader.readValue(line, Record.class);
            var message = createMessage(record);

            switch (record.getLevel()) {
            case TRACE:
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case FATAL:
            case ERROR:
                logger.error(message);
                break;
            }
        }

        private void processRegularLog(String line) {
            collector.add(line);
        }
    }

    private static final class Record {
        private final Map<String, Object> dynamic = new HashMap<>();
        private String hostname;
        private Level level;
        private String msg;
        private int pid;
        private long time;

        @JsonAnyGetter
        public Map<String, Object> getDynamic() {
            return dynamic;
        }

        public String getHostname() {
            return hostname;
        }

        public Level getLevel() {
            return level;
        }

        public String getMsg() {
            return msg;
        }

        public int getPid() {
            return pid;
        }

        public long getTime() {
            return time;
        }

        @JsonAnySetter
        private void setDynamicProperty(String key, Object value) {
            this.dynamic.put(key, value);
        }
    }

    private static class LogWatcher implements Runnable {
        private final Thread worker = new Thread(this);
        private final Handler inputHandler;
        private final Handler errorHandler;
        private final Process process;
        private final Log logger;

        public LogWatcher(Process process, Handler inputHandler,
                Handler errorHandler, Log logger) {
            this.logger = logger;
            this.process = process;
            this.inputHandler = inputHandler;
            this.errorHandler = errorHandler;
        }

        public void start() {
            worker.start();
        }

        @Override
        public void run() {
            try {
                var inputReader = process.inputReader();
                var errorReader = process.errorReader();

                String inputLine = null;
                String errorLine = null;

                while ((inputLine = inputReader.readLine()) != null
                        || (errorLine = errorReader.readLine()) != null) {

                    if (inputLine != null) {
                        logger.debug(inputLine);
                        inputHandler.processLine(inputLine);
                    }

                    if (errorLine != null) {
                        logger.debug(errorLine);
                        errorHandler.processLine(errorLine);
                    }
                }

                inputHandler.finish();
                errorHandler.finish();
            } catch (IOException e) {
                throw new GeneratorException("The generator log is malformed",
                        e);
            }
        }
    }
}
