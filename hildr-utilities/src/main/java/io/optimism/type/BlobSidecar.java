package io.optimism.type;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import java.util.Objects;

/**
 * The BlobSidecar class
 *
 * @author thinkAfCod
 * @since 0.2.6
 */
public class BlobSidecar {
    private String index;
    private String blob;

    @JsonAlias("signed_block_header")
    private BeaconSignedBlockHeader signedBlockHeader;

    @JsonAlias("kzg_commitment")
    private String kzgCommitment;

    @JsonAlias("kzg_proof")
    private String kzgProof;

    @JsonAlias("kzg_commitment_inclusion_proof")
    private List<String> kzgCommitmentInclusionProof;

    /**
     * The BlobSidecar constructor.
     */
    public BlobSidecar() {}

    /**
     * The BlobSidecar constructor.
     *
     * @param index the blob index
     * @param blob the blob data
     * @param signedBlockHeader signed blob block header info
     * @param kzgCommitment the kzg commitment info
     * @param kzgProof the kzg proofs
     */
    public BlobSidecar(
            String index,
            String blob,
            BeaconSignedBlockHeader signedBlockHeader,
            String kzgCommitment,
            String kzgProof,
            List<String> kzgCommitmentInclusionProof) {
        this.index = index;
        this.blob = blob;
        this.signedBlockHeader = signedBlockHeader;
        this.kzgCommitment = kzgCommitment;
        this.kzgProof = kzgProof;
        this.kzgCommitmentInclusionProof = kzgCommitmentInclusionProof;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }

    public BeaconSignedBlockHeader getSignedBlockHeader() {
        return signedBlockHeader;
    }

    public void setSignedBlockHeader(BeaconSignedBlockHeader signedBlockHeader) {
        this.signedBlockHeader = signedBlockHeader;
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

    public List<String> getKzgCommitmentInclusionProof() {
        return kzgCommitmentInclusionProof;
    }

    public void setKzgCommitmentInclusionProof(List<String> kzgCommitmentInclusionProof) {
        this.kzgCommitmentInclusionProof = kzgCommitmentInclusionProof;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlobSidecar that)) {
            return false;
        }
        return Objects.equals(index, that.index)
                && Objects.equals(blob, that.blob)
                && Objects.equals(kzgCommitment, that.kzgCommitment)
                && Objects.equals(kzgProof, that.kzgProof)
                && Objects.equals(kzgCommitmentInclusionProof, that.kzgCommitmentInclusionProof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, blob, signedBlockHeader, kzgCommitment, kzgProof, kzgCommitmentInclusionProof);
    }
}
