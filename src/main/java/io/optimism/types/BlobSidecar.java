package io.optimism.types;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import java.util.Objects;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * The BlobSidecar class
 *
 * @author thinkAfCod
 * @since 0.3.0
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
     * @param kzgCommitmentInclusionProof the kzg commitment inclusion proofs
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

    /**
     * Gets index.
     *
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets index value.
     *
     * @param index the index
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Gets blob.
     *
     * @return the blob
     */
    public String getBlob() {
        return blob;
    }

    /**
     * Sets blob value.
     *
     * @param blob the blob
     */
    public void setBlob(String blob) {
        this.blob = blob;
    }

    /**
     * Gets signed block header.
     *
     * @return the signed block header
     */
    public BeaconSignedBlockHeader getSignedBlockHeader() {
        return signedBlockHeader;
    }

    /**
     * Sets signed block header value.
     *
     * @param signedBlockHeader the signed block header
     */
    public void setSignedBlockHeader(BeaconSignedBlockHeader signedBlockHeader) {
        this.signedBlockHeader = signedBlockHeader;
    }

    /**
     * Gets kzg commitment.
     *
     * @return the kzg commitment
     */
    public String getKzgCommitment() {
        return kzgCommitment;
    }

    /**
     * Sets kzg commitment value.
     *
     * @param kzgCommitment the kzg commitment
     */
    public void setKzgCommitment(String kzgCommitment) {
        this.kzgCommitment = kzgCommitment;
    }

    /**
     * Gets kzg proof.
     *
     * @return the kzg proof
     */
    public String getKzgProof() {
        return kzgProof;
    }

    /**
     * Sets kzg proof value.
     *
     * @param kzgProof the kzg proof
     */
    public void setKzgProof(String kzgProof) {
        this.kzgProof = kzgProof;
    }

    /**
     * Gets kzg commitment inclusion proof.
     *
     * @return the kzg commitment inclusion proof
     */
    public List<String> getKzgCommitmentInclusionProof() {
        return kzgCommitmentInclusionProof;
    }

    /**
     * Sets kzg commitment inclusion proof value.
     *
     * @param kzgCommitmentInclusionProof the kzg commitment inclusion proof
     */
    public void setKzgCommitmentInclusionProof(List<String> kzgCommitmentInclusionProof) {
        this.kzgCommitmentInclusionProof = kzgCommitmentInclusionProof;
    }

    /**
     * Gets versioned hash.
     *
     * @return the versioned hash
     */
    public String getVersionedHash() {
        var hash = Hash.sha256(Numeric.hexStringToByteArray(this.kzgCommitment));
        hash[0] = 1;
        return Numeric.toHexString(hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlobSidecar that)) {
            return false;
        }
        return Objects.equals(index, that.index)
                && Objects.equals(blob, that.blob)
                && Objects.equals(signedBlockHeader, that.signedBlockHeader)
                && Objects.equals(kzgCommitment, that.kzgCommitment)
                && Objects.equals(kzgProof, that.kzgProof)
                && Objects.equals(kzgCommitmentInclusionProof, that.kzgCommitmentInclusionProof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, blob, signedBlockHeader, kzgCommitment, kzgProof, kzgCommitmentInclusionProof);
    }
}
