package io.optimism.type;

import java.math.BigInteger;

/**
 * The BlobSidecar class
 *
 * @author thinkAfCod
 * @since 0.2.6
 */
public class BlobSidecar {
    private String blockRoot;
    private String index;
    private BigInteger slot;
    private String blockParentRoot;
    private BigInteger proposerIndex;
    private String blob;
    private String kzgCommitment;
    private String kzgProof;

    /**
     * The BlobSidecar constructor.
     */
    public BlobSidecar() {
    }

    /**
     * The BlobSidecar constructor.
     *
     * @param blockRoot
     * @param index
     * @param slot
     * @param blockParentRoot
     * @param proposerIndex
     * @param blob
     * @param kzgCommitment
     * @param kzgProof
     */
    public BlobSidecar(
            String blockRoot,
            String index,
            BigInteger slot,
            String blockParentRoot,
            BigInteger proposerIndex,
            String blob,
            String kzgCommitment,
            String kzgProof
    ) {
        this.blockRoot = blockRoot;
        this.index = index;
        this.slot = slot;
        this.blockParentRoot = blockParentRoot;
        this.proposerIndex = proposerIndex;
        this.blob = blob;
        this.kzgCommitment = kzgCommitment;
        this.kzgProof = kzgProof;
    }

    public String getBlockRoot() {
        return blockRoot;
    }

    public void setBlockRoot(String blockRoot) {
        this.blockRoot = blockRoot;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public BigInteger getSlot() {
        return slot;
    }

    public void setSlot(BigInteger slot) {
        this.slot = slot;
    }

    public String getBlockParentRoot() {
        return blockParentRoot;
    }

    public void setBlockParentRoot(String blockParentRoot) {
        this.blockParentRoot = blockParentRoot;
    }

    public BigInteger getProposerIndex() {
        return proposerIndex;
    }

    public void setProposerIndex(BigInteger proposerIndex) {
        this.proposerIndex = proposerIndex;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }

    public String getKzgCommitment() {
        return kzgCommitment;
    }

    public void setKzgCommitment(String kzgCommitment) {
        this.kzgCommitment = kzgCommitment;
    }

    public String getKzgProof() {
        return kzgProof;
    }

    public void setKzgProof(String kzgProof) {
        this.kzgProof = kzgProof;
    }
}
