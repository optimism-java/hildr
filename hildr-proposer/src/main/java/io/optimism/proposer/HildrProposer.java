package io.optimism.proposer;

import io.optimism.proposer.cli.Cli;
import picocli.CommandLine;

/**
 * Batcher main method.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class HildrProposer {

    /** Constructor of HildrBatcher. */
    public HildrProposer() {}

    /**
     * Main method of HildrBatcher.
     *
     * @param args Starts arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Cli()).execute(args);
        System.exit(exitCode);
    }
}
