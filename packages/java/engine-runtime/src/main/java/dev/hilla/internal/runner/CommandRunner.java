package dev.hilla.internal.runner;

/**
 * A generic command runner which throws a {@link RunnerException}.
 */
public interface CommandRunner {

    /**
     * Run the command.
     *
     * @throws RunnerException
     *             if the command fails
     */
    void run() throws RunnerException;
}
