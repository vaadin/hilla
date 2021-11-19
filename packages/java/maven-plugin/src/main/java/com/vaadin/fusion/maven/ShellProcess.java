package com.vaadin.fusion.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ShellProcess {
    private final List<String> command = new ArrayList<>();

    {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            command.add("cmd.exe");
            command.add("/c");
        } else {
            command.add("/bin/bash");
            command.add("-c");
        }
    }

    public ShellProcess command(String cmd) {
        command.add(cmd);
        return this;
    }

    public ShellProcess command(Collection<String> cmd) {
        command.addAll(cmd);
        return this;
    }

    public ShellProcess command(String... cmd) {
        command(List.of(cmd));
        return this;
    }

    public List<String> getCommand() {
        return command;
    }

    public Process start() throws IOException {
        return new ProcessBuilder().command(command).start();
    }
}
