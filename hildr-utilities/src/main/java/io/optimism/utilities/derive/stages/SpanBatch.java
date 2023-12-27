package io.optimism.utilities.derive.stages;

import java.math.BigInteger;
import java.util.List;

/**
 *  SpanBatch is an implementation of Batch interface,
 *  containing the input to build a span of L2 blocks in derived form (SpanBatchElement)
 *
 *  @author zhouop0
 *  @since 0.1.0
 */
public class SpanBatch implements IBatch {

    // First 20 bytes of the first block's parent hash
    private String parentCheck;
    // First 20 bytes of the last block's L1 origin hash
    private String l1OriginCheck;
    // List of block input in derived form
    private List<SpanBatchElement> batches;

    public String getParentCheck() {
        return parentCheck;
    }

    public String getL1OriginCheck() {
        return l1OriginCheck;
    }

    public List<SpanBatchElement> getBatches() {
        return batches;
    }

    @Override
    public int getBatchType() {
        return BatchType.SPAN_BATCH_TYPE.getCode();
    }

    @Override
    public BigInteger getTimestamp() {
        return batches.getFirst().timestamp();
    }

    /**
     * GetStartEpochNum returns epoch number(L1 origin block number) of the first block in the span.
     */
    public BigInteger getStartEpochNum() {
        return this.batches.getFirst().epochNum();
    }

    /**
     * checks if the parentCheck matches the first 20 bytes of given hash, probably the current L2 safe head.
     * @param hash the first 20 bytes of given hash.
     * @return boolean.
     */
    public boolean checkOriginHash(String hash) {
        return this.l1OriginCheck.equals(hash);
    }

    /**
     * checks if the parentCheck matches the first 20 bytes of given hash, probably the current L2 safe head.
     * @param hash the first 20 bytes of given hash.
     * @return boolean.
     */
    public boolean checkParentHash(String hash) {
        return this.parentCheck.equals(hash);
    }

    /**
     * GetBlockEpochNum
     * @param index batches index.
     * @return the epoch number(L1 origin block number) of the block at the given index in the span.
     */
    public BigInteger getBlockEpochNum(int index) {
        return this.batches.get(index).epochNum();
    }

    /**
     * GetBlockTimestamp
     * @param index batches index.
     * @return the timestamp of the block at the given index in the span.
     */
    public BigInteger getBlockTimestamp(int index) {
        return this.batches.get(index).timestamp();
    }

    /**
     * GetBlockCount
     * @return the number of blocks in the span.
     */
    public int getBlockCount() {
        return this.batches.size();
    }

    /**
     * AppendSingularBatch appends a SingularBatch into the span batch
     * updates l1OriginCheck or parentCheck if needed.
     *
     * @param singularBatch SingularBatch
     */
    public void AppendSingularBatch(SingularBatch singularBatch) {
        if (batches.size() == 0) {
            this.parentCheck = singularBatch.parentHash().substring(0, 20);
        }
        this.batches.add(SpanBatchElement.singularBatchToElement(singularBatch)); // add the batch to the list
    }
}
