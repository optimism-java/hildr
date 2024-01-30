package io.optimism.batcher.cli;

import io.micrometer.tracing.Tracer;
import io.optimism.batcher.BatcherSubmitter;
import io.optimism.batcher.config.Config;
import io.optimism.batcher.exception.BatcherExecutionException;
import io.optimism.batcher.telemetry.BatcherMetricsServer;
import io.optimism.batcher.telemetry.BatcherPrometheusMetrics;
import io.optimism.utilities.telemetry.Logging;
import io.optimism.utilities.telemetry.TracerTaskWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sun.misc.Signal;

/**
 * CLI handler.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
@Command(name = "hildr", mixinStandardHelpOptions = true, description = "")
public class Cli implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);

    @Option(names = "--l1-rpc-url", required = true, description = "The base chain RPC URL")
    String l1RpcUrl;

    @Option(names = "--l2-rpc-url", required = true, description = "The L2 engine RPC URL")
    String l2RpcUrl;

    @Option(names = "--rollup-rpc-url", required = true, description = "The rollup node RPC URL")
    String rollupRpcUrl;

    @Option(names = "--l1-signer", required = true, description = "The base chain private key")
    String l1Signer;

    @Option(names = "--batch-inbox-address", required = true, description = "The address of batch inbox contract")
    String batchInboxAddress;

    @Option(names = "--sub-safety-margin", required = true, description = "")
    Long subSafetyMargin;

    @Option(names = "--pull-interval", required = true, description = "")
    Long pollInterval;

    @Option(names = "--max-l1-tx-size", required = true, description = "")
    Long maxL1TxSize;

    @Option(names = "--target-frame-size", required = true, description = "")
    Integer targetFrameSize;

    @Option(names = "--target-num-frames", required = true, description = "")
    Integer targetNumFrames;

    @Option(names = "--approx-compr-ratio", required = true, description = "")
    String approxComprRatio;

    @Option(names = "--enable-metrics", description = "If not contains this option, will not open metrics server")
    boolean enableMetrics;

    @Option(
            names = "--metrics-port",
            defaultValue = "9200",
            required = true,
            description = "The port of metrics server ")
    Integer metricsPort;

    /** the Cli constructor. */
    public Cli() {}

    @Override
    public void run() {
        TracerTaskWrapper.setTracerSupplier(Logging.INSTANCE::getTracer);

        // listen close signal
        Signal.handle(new Signal("INT"), sig -> System.exit(0));
        Signal.handle(new Signal("TERM"), sig -> System.exit(0));

        Tracer tracer = Logging.INSTANCE.getTracer("hildr-batcher-cli");
        var span = tracer.nextSpan().name("batcher-submitter").start();
        try (var unused = tracer.withSpan(span)) {
            // start metrics server
            if (this.enableMetrics) {
                BatcherMetricsServer.start(this.metricsPort);
            }
            // start batcher submitter
            BatcherSubmitter submitter = new BatcherSubmitter(this.optionToConfig());
            submitter.startAsync().awaitTerminated();
        } catch (Exception e) {
            LOGGER.error("hildr batcher: ", e);
            throw new BatcherExecutionException(e);
        } finally {
            if (this.enableMetrics) {
                LOGGER.info("stop metrics");
                BatcherMetricsServer.stop();
            }
            span.end();
        }
    }

    private Config optionToConfig() {
        return new Config(
                this.l1RpcUrl,
                this.l2RpcUrl,
                this.rollupRpcUrl,
                this.l1Signer,
                this.batchInboxAddress,
                this.subSafetyMargin,
                this.pollInterval,
                this.maxL1TxSize,
                this.targetFrameSize,
                this.targetNumFrames,
                this.approxComprRatio,
                new BatcherPrometheusMetrics(BatcherMetricsServer.getRegistry(), "hildr_batcher"));
    }
}
