package io.optimism;

import io.optimism.cli.Cli;
import picocli.CommandLine;

/**
 * The type Hildr.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class Hildr {

    /** Instantiates a new Hildr. */
    public Hildr() {}

    /**
     * Gets greeting.
     *
     * @return the greeting
     */
    public String getGreeting() {
        return "Hello World!";
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Cli()).execute(args);
        System.exit(exitCode);
    }
}
