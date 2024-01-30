package io.optimism.batcher.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.optimism.type.L1BlockRef;
import io.optimism.type.L2BlockRef;
import io.optimism.utilities.derive.stages.Frame;
import io.optimism.utilities.telemetry.MetricsSupplier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.ArrayUtils;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

/**
 * The BatcherPrometheusMetrics type. Use this instance to record metrics value
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class BatcherPrometheusMetrics implements BatcherMetrics {

    private static final Map<String, String> EMPTY_TAGS = new HashMap<>();

    private static final String LAYER_L1 = "l1";

    private static final String LAYER_L1_ORIGIN = "l1_origin";

    private static final String LAYER_L2 = "l2";

    private static final String LATEST = "latest";

    private static final String STAGE_LOADED = "loaded";

    private static final String STAGE_OPENED = "opened";

    private static final String STAGE_ADDED = "added";

    private static final String STAGE_CLOSED = "closed";

    private static final String STAGE_FULLY_SUBMITTED = "fully_submitted";

    private static final String STAGE_TIMEOUT = "timed_out";

    private static final String TX_STAGE_SUBMITTED = "submitted";

    private static final String TX_STAGE_SUCCESS = "success";

    private static final String TX_STAGE_FAILED = "failed";

    private static final Map<String, String> DESC_MAP = new HashMap<>();

    static {
        DESC_MAP.put("refs_number", "Gauge representing the different L1/L2 reference block numbers");
        DESC_MAP.put("refs_time", "Gauge representing the different L1/L2 reference block timestamps");
        DESC_MAP.put(
                "refs_hash", "Gauge representing the different L1/L2 reference block hashes truncated to float values");
        DESC_MAP.put(
                "refs_latency",
                "Gauge representing the different L1/L2 reference block timestamps minus current time,"
                        + " in seconds");
        DESC_MAP.put("refs_seqnr", "Gauge representing the different L2 reference sequence numbers");
        DESC_MAP.put(
                "pending_blocks_bytes_total",
                "Total size of transactions in pending blocks as they are fetched from L2.");
        DESC_MAP.put(
                "pending_blocks_bytes_current",
                "Current size of transactions in the pending" + " (fetched from L2 but not in a channel) stage.");

        DESC_MAP.put("info", "Tracking version and config info.");
        DESC_MAP.put("up", "1 if the op-batcher has finished starting up.");
    }

    private final MetricsSupplier metricsSupplier;

    private final HashMap<String, String> latencySeen;

    private final double[] channelComprRatioBucket;

    /**
     * The BatcherPrometheusMetrics constructor.
     *
     * @param registry The MeterRegistry instance
     * @param namespace The prefix of metrics name
     */
    public BatcherPrometheusMetrics(MeterRegistry registry, String namespace) {
        this.metricsSupplier = new MetricsSupplier(registry, namespace, DESC_MAP);
        this.latencySeen = new HashMap<>();
        this.channelComprRatioBucket =
                ArrayUtils.addAll(new double[] {0.1, 0.2}, this.metricsSupplier.linearBuckets(0.3, 0.05, 14));
    }

    @Override
    public void recordLatestL1Block(L1BlockRef l1ref) {
        this.recordRef(LAYER_L1, LATEST, l1ref.number(), l1ref.timestamp(), l1ref.hash());
    }

    @Override
    public void recordL2BlocksLoaded(L2BlockRef l2ref) {
        this.recordL2Ref(STAGE_LOADED, l2ref);
    }

    @Override
    public void recordChannelOpened(Frame frame, int numPendingBlocks) {
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", STAGE_OPENED);
        this.metricsSupplier.getOrCreateEventMeter("channel", tags).record();
    }

    @Override
    public void recordL2BlocksAdded(
            L2BlockRef l2ref, int numBlocksAdded, int numPendingBlocks, int inputBytes, int outputComprBytes) {
        this.recordL2Ref(STAGE_ADDED, l2ref);
        Counter blocksAddedCount = this.metricsSupplier.getOrCreateCounter("blocks_added_count", EMPTY_TAGS);
        blocksAddedCount.increment(numBlocksAdded);
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", STAGE_ADDED);
        AtomicLong pendingBlocksCount = this.metricsSupplier.getOrCreateGauge("pending_blocks_count", tags);
        pendingBlocksCount.getAndSet(numPendingBlocks);
        AtomicLong channelInputBytes = this.metricsSupplier.getOrCreateGauge("input_bytes", tags);
        channelInputBytes.getAndSet(inputBytes);
        AtomicLong channelReadyBytes = this.metricsSupplier.getOrCreateGauge("ready_bytes", EMPTY_TAGS);
        channelReadyBytes.getAndSet(outputComprBytes);
    }

    @Override
    public void recordChannelClosed(
            Frame frame,
            int numPendingBlocks,
            int numFrames,
            int inputBytes,
            int outputComprBytes,
            String errorReason) {
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", STAGE_CLOSED);
        this.metricsSupplier.getOrCreateEventMeter("channel", tags).record();
        this.metricsSupplier.getOrCreateGauge("pending_blocks_count", tags).getAndSet(numPendingBlocks);
        this.metricsSupplier.getOrCreateGauge("channel_num_frames", EMPTY_TAGS).getAndSet(numFrames);
        this.metricsSupplier.getOrCreateGauge("input_bytes", tags).getAndSet(inputBytes);
        this.metricsSupplier.getOrCreateGauge("ready_bytes", EMPTY_TAGS).getAndSet(outputComprBytes);
        this.metricsSupplier.getOrCreateCounter("input_bytes_total", EMPTY_TAGS).increment(inputBytes);
        this.metricsSupplier
                .getOrCreateCounter("output_bytes_total", EMPTY_TAGS)
                .increment(outputComprBytes);
        BigDecimal comprRatio = BigDecimal.ZERO;
        if (inputBytes > 0) {
            comprRatio = new BigDecimal(outputComprBytes).divide(new BigDecimal(inputBytes), 2, RoundingMode.UP);
        }
        this.metricsSupplier
                .getOrCreateHistogram("channel_compr_ratio", null, this.channelComprRatioBucket, EMPTY_TAGS)
                .record(comprRatio.doubleValue());
    }

    @Override
    public void recordL2BlockInPendingQueue(EthBlock.Block block) {
        var size = estimateBatchSize(block);
        this.metricsSupplier
                .getOrCreateCounter("pending_blocks_bytes_total", EMPTY_TAGS)
                .increment((double) size);
        this.metricsSupplier
                .getOrCreateCounter("pending_blocks_bytes_current", EMPTY_TAGS)
                .increment((double) size);
    }

    @Override
    public void recordL2BlockInChannel(EthBlock.Block block) {
        var size = estimateBatchSize(block);
        this.metricsSupplier
                .getOrCreateCounter("pending_blocks_bytes_current", EMPTY_TAGS)
                .increment((double) -1 * size);
    }

    @Override
    public void recordChannelFullySubmitted(Frame frame) {
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", STAGE_FULLY_SUBMITTED);
        this.metricsSupplier.getOrCreateEventMeter("channel", tags).record();
    }

    @Override
    public void recordChannelTimedOut(Frame frame) {
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", STAGE_TIMEOUT);
        this.metricsSupplier.getOrCreateEventMeter("channel", tags).record();
    }

    @Override
    public void recordBatchTxSubmitted() {
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", TX_STAGE_SUBMITTED);
        this.metricsSupplier.getOrCreateEventMeter("batcher_tx", tags).record();
    }

    @Override
    public void recordBatchTxSuccess() {
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", TX_STAGE_SUCCESS);
        this.metricsSupplier.getOrCreateEventMeter("batcher_tx", tags).record();
    }

    @Override
    public void recordBatchTxFailed() {
        Map<String, String> tags = new HashMap<>();
        tags.put("stage", TX_STAGE_FAILED);
        this.metricsSupplier.getOrCreateEventMeter("batcher_tx", tags).record();
    }

    @Override
    public void recordInfo(String version) {
        Map<String, String> tags = new HashMap<>();
        tags.put("version", version);
        this.metricsSupplier.getOrCreateGauge("info", tags).getAndSet(1);
    }

    @Override
    public void recordUp() {
        this.metricsSupplier.getOrCreateGauge("up", EMPTY_TAGS).getAndSet(1);
    }

    private void recordL2Ref(String stage, L2BlockRef l2ref) {
        this.recordRef(LAYER_L2, stage, l2ref.number(), l2ref.timestamp(), l2ref.hash());

        this.recordRef(
                LAYER_L1_ORIGIN,
                stage,
                l2ref.l1origin().number(),
                BigInteger.ZERO,
                l2ref.l1origin().hash());

        Map<String, String> tags = new HashMap<>();
        tags.put("stage", stage);
        AtomicLong seqNr = this.metricsSupplier.getOrCreateGauge("refs_seqnr", tags);
        seqNr.getAndSet(l2ref.sequenceNumber().longValue());
    }

    private void recordRef(String layer, String type, BigInteger number, BigInteger timestamp, String hash) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("layer", layer);
        tags.put("type", type);
        AtomicLong refsNumber = this.metricsSupplier.getOrCreateGauge("refs_number", tags);
        refsNumber.getAndSet(number.longValue());
        if (timestamp != null && !timestamp.equals(BigInteger.ZERO)) {
            AtomicLong refsTime = this.metricsSupplier.getOrCreateGauge("refs_time", tags);
            refsTime.getAndSet(timestamp.longValue());
            if (!hash.equals(this.latencySeen.get(LATEST))) {
                this.latencySeen.put(LATEST, hash);
                AtomicLong letencySeen = this.metricsSupplier.getOrCreateGauge("refs_latency", tags);
                letencySeen.getAndSet(timestamp.longValue() - Instant.now().toEpochMilli());
            }
        }
        AtomicLong hashGuage = this.metricsSupplier.getOrCreateGauge("refs_hash", tags);
        hashGuage.getAndSet(Numeric.toBigInt(hash).longValue());
    }

    @SuppressWarnings("rawtypes")
    private long estimateBatchSize(EthBlock.Block block) {
        int size = 70;
        var txs = block.getTransactions();
        for (EthBlock.TransactionResult tx : txs) {
            var txObj = (EthBlock.TransactionObject) tx;
            if (txObj.getType().equalsIgnoreCase("0x7E")) {
                continue;
            }
            String input = txObj.getInput();
            byte[] encode = RlpEncoder.encode(RlpString.create(input));
            size += encode.length + 2;
        }
        return size;
    }
}
