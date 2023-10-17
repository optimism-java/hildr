package io.optimism.proposer.cli;

import io.optimism.proposer.L2OutputSubmitter;
import io.optimism.proposer.config.Config;
import io.optimism.utilities.telemetry.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import sun.misc.Signal;

/**
 * Proposer CLI handler.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class Cli implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);

    @CommandLine.Option(names = "--enable-metrics", required = false, description = "Enable metrics server")
    boolean enableMetrics;

    @CommandLine.Option(names = "--l1-rpc-url", required = true, description = "The L1 chain RPC URL")
    String l1RpcUrl;

    @CommandLine.Option(names = "--l2-rpc-url", required = true, description = "The L2 engine RPC URL")
    String l2RpcUrl;

    @CommandLine.Option(names = "--rollup-rpc-url", required = true, description = "The rollup node RPC URL")
    String rollupRpcUrl;

    @CommandLine.Option(names = "--l2-chainId", required = true, description = "The L2 chain ID")
    Long l2ChainId;

    @CommandLine.Option(names = "--l2-signer", required = true, description = "The L2 chain private key")
    String l2Signer;

    @CommandLine.Option(names = "--l2oo-address", description = "The L2 output oracle contract address")
    String l2OutputOracleAddr;

    @CommandLine.Option(names = "--l2dgf-address", description = "The L2 dispute game factory contract address")
    String dgfContractAddr;

    @CommandLine.Option(names = "--poll-interval", description = "How frequently to poll L2 for new blocks")
    Long pollInterval;

    @CommandLine.Option(
            names = "--network-timeout",
            defaultValue = "300",
            description = "How frequently to poll L2 for new blocks")
    Long networkTimeout;

    @CommandLine.Option(
            names = "--allow-non-finalized",
            defaultValue = "false",
            description = "Allow the proposer to submit proposals for L2 blocks derived from non-finalized L1 blocks")
    boolean allowNonFinalized;

    /**
     * The proposer CLI constructor.
     */
    public Cli() {}

    @Override
    public void run() {

        // listen close signal
        Signal.handle(new Signal("INT"), sig -> System.exit(0));
        Signal.handle(new Signal("TERM"), sig -> System.exit(0));

        var tracer = Logging.INSTANCE.getTracer("hildr-proposer-cli");
        var span = tracer.nextSpan().name("proposer-submitter").start();
        try (var unused = tracer.withSpan(span)) {
            //      if (this.enableMetrics) {
            //        // todo start metrics server
            //      }
            // start l2 output submitter
            var submitter = new L2OutputSubmitter(optionToConfig());
            submitter.startAsync().awaitTerminated();
        } catch (Exception e) {
            LOGGER.error("hildr proposer: ", e);
            throw new RuntimeException(e);
        } finally {
            if (this.enableMetrics) {
                LOGGER.info("stop metrics");
                // todo stop metrics server
            }
            span.end();
        }
    }

    private Config optionToConfig() {
        return new Config(
                this.l2ChainId,
                this.l1RpcUrl,
                this.l2RpcUrl,
                this.rollupRpcUrl,
                this.l2Signer,
                this.l2OutputOracleAddr,
                this.dgfContractAddr,
                this.pollInterval,
                this.networkTimeout,
                this.allowNonFinalized);
    }
}
