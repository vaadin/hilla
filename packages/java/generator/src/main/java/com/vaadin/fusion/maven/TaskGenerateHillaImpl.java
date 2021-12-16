package com.vaadin.fusion.maven;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateHilla;

public class TaskGenerateHillaImpl implements TaskGenerateHilla {

    @Override
    public void execute() throws ExecutionFailedException {
        try {
            List<String> command = new ArrayList<>();
            command.add("mvn");
            command.add("fusion:generate");

            var builder = new ProcessBuilder().command(command).inheritIO();

            var process = builder.start();

            var exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("The Generator process finished with the exit code "
                        + exitCode);
            } else {
                throw new ExecutionFailedException(
                        "Generator execution failed with exit code " + exitCode);
            }
        } catch(Exception e) {
            throw new ExecutionFailedException(e);
        }

    }
    
}
