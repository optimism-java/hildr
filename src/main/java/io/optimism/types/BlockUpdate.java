package io.optimism.types;

import java.math.BigInteger;

/**
 * the BlockUpdate class.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
public abstract class BlockUpdate {

    /** not public constructor. */
    private BlockUpdate() {}

    /** update new Block. */
    public static class NewBlock extends BlockUpdate {

        private L1Info l1Info;

        /**
         * NewBlock constructor.
         *
         * @param l1Info update l1Info
         */
        public NewBlock(L1Info l1Info) {
            this.l1Info = l1Info;
        }

        /**
         * get update L1Info.
         *
         * @return l1Info
         */
        public L1Info get() {
            return l1Info;
        }
    }

    /** update finalized Block. */
    public static class FinalityUpdate extends BlockUpdate {

        private BigInteger finalizedBlock;

        /**
         * FinalityUpdate constructor.
         *
         * @param finalizedBlock finalized block
         */
        public FinalityUpdate(BigInteger finalizedBlock) {
            this.finalizedBlock = finalizedBlock;
        }

        /**
         * get finalized block.
         *
         * @return finalized block
         */
        public BigInteger get() {
            return finalizedBlock;
        }
    }

    /** update Reorg. */
    public static class Reorg extends BlockUpdate {

        /** Reorg constructor. */
        public Reorg() {}
    }
}
