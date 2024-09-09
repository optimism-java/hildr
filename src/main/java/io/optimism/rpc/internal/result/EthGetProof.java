package io.optimism.rpc.internal.result;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.Response;
import org.web3j.utils.Numeric;

/**
 * eth_getProof api response.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class EthGetProof extends Response<EthGetProof.Proof> {

    @Override
    @JsonDeserialize(using = EthGetProof.ResponseDeserializer.class)
    public void setResult(EthGetProof.Proof result) {
        super.setResult(result);
    }

    /**
     * get proof result.
     *
     * @return proof result
     */
    public EthGetProof.Proof getProof() {
        return getResult();
    }

    /** Instantiates a new Eth get proof. */
    public EthGetProof() {}

    /** json rpc result of object. */
    public static class Proof {

        private String address;

        private String balance;

        private String codeHash;

        private String nonce;

        private String storageHash;

        private List<String> accountProof;

        private List<StorageProof> storageProof;

        /** Instantiates a new Proof. */
        public Proof() {}

        /**
         * Instantiates a new Proof.
         *
         * @param address the address
         * @param balance the balance
         * @param codeHash the code hash
         * @param nonce the nonce
         * @param storageHash the storage hash
         * @param accountProof the account proof
         * @param storageProof the storage proof
         */
        public Proof(
                String address,
                String balance,
                String codeHash,
                String nonce,
                String storageHash,
                List<String> accountProof,
                List<StorageProof> storageProof) {
            this.address = address;
            this.balance = balance;
            this.codeHash = codeHash;
            this.nonce = nonce;
            this.storageHash = storageHash;
            this.accountProof = accountProof;
            this.storageProof = storageProof;
        }

        /**
         * Gets address.
         *
         * @return the address
         */
        public String getAddress() {
            return address;
        }

        /**
         * Sets address.
         *
         * @param address the address
         */
        public void setAddress(String address) {
            this.address = address;
        }

        /**
         * Gets balance raw.
         *
         * @return the balance raw
         */
        public String getBalanceRaw() {
            return this.balance;
        }

        /**
         * Gets balance.
         *
         * @return the balance
         */
        public BigInteger getBalance() {
            return Numeric.decodeQuantity(balance);
        }

        /**
         * Sets balance.
         *
         * @param balance the balance
         */
        public void setBalance(String balance) {
            this.balance = balance;
        }

        /**
         * Gets code hash.
         *
         * @return the code hash
         */
        public String getCodeHash() {
            return codeHash;
        }

        /**
         * Sets code hash.
         *
         * @param codeHash the code hash
         */
        public void setCodeHash(String codeHash) {
            this.codeHash = codeHash;
        }

        /**
         * Gets nonce.
         *
         * @return the nonce
         */
        public String getNonce() {
            return nonce;
        }

        /**
         * Sets nonce.
         *
         * @param nonce the nonce
         */
        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        /**
         * Gets storage hash.
         *
         * @return the storage hash
         */
        public String getStorageHash() {
            return storageHash;
        }

        /**
         * Sets storage hash.
         *
         * @param storageHash the storage hash
         */
        public void setStorageHash(String storageHash) {
            this.storageHash = storageHash;
        }

        /**
         * Gets account proof.
         *
         * @return the account proof
         */
        public List<String> getAccountProof() {
            return accountProof;
        }

        /**
         * Sets account proof.
         *
         * @param accountProof the account proof
         */
        public void setAccountProof(List<String> accountProof) {
            this.accountProof = accountProof;
        }

        /**
         * Gets storage proof.
         *
         * @return the storage proof
         */
        public List<StorageProof> getStorageProof() {
            return storageProof;
        }

        /**
         * Sets storage proof.
         *
         * @param storageProof the storage proof
         */
        public void setStorageProof(List<StorageProof> storageProof) {
            this.storageProof = storageProof;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Proof proof)) {
                return false;
            }

            if (getAddress() != null ? !getAddress().equals(proof.getAddress()) : proof.getAddress() != null) {
                return false;
            }

            if (getBalanceRaw() != null
                    ? !getBalanceRaw().equals(proof.getBalanceRaw())
                    : proof.getBalanceRaw() != null) {
                return false;
            }

            if (getCodeHash() != null ? !getCodeHash().equals(proof.getCodeHash()) : proof.getCodeHash() != null) {
                return false;
            }
            if (getNonce() != null ? !getNonce().equals(proof.getNonce()) : proof.getNonce() != null) {
                return false;
            }

            if (getStorageHash() != null
                    ? !getStorageHash().equals(proof.getStorageHash())
                    : proof.getStorageHash() != null) {
                return false;
            }

            if (getAccountProof() != null
                    ? !getAccountProof().equals(proof.getAccountProof())
                    : proof.getAccountProof() != null) {
                return false;
            }

            return getStorageProof() != null
                    ? !getStorageProof().equals(proof.getStorageProof())
                    : proof.getStorageProof() != null;
        }

        @Override
        public int hashCode() {
            int result = getAddress() != null ? getAddress().hashCode() : 0;
            result = 31 * result + (getBalanceRaw() != null ? getBalanceRaw().hashCode() : 0);
            result = 31 * result + (getCodeHash() != null ? getCodeHash().hashCode() : 0);
            result = 31 * result + (getNonce() != null ? getNonce().hashCode() : 0);
            result = 31 * result + (getStorageHash() != null ? getStorageHash().hashCode() : 0);
            result =
                    31 * result + (getAccountProof() != null ? getAccountProof().hashCode() : 0);
            result =
                    31 * result + (getStorageProof() != null ? getStorageProof().hashCode() : 0);
            return result;
        }
    }

    /** storage proof. */
    public static class StorageProof {
        private String key;

        private String value;

        private List<String> proof;

        /** Storage proof. */
        public StorageProof() {}

        /**
         * Instantiates a new Storage proof.
         *
         * @param key the key
         * @param value the value
         * @param proof the proof
         */
        public StorageProof(String key, String value, List<String> proof) {
            this.key = key;
            this.value = value;
            this.proof = proof;
        }

        /**
         * Gets key.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets key.
         *
         * @param key the key
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value the value
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets proof.
         *
         * @return the proof
         */
        public List<String> getProof() {
            return proof;
        }

        /**
         * Sets proof.
         *
         * @param proof the proof
         */
        public void setProof(List<String> proof) {
            this.proof = proof;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StorageProof that)) return false;
            return Objects.equals(key, that.key)
                    && Objects.equals(value, that.value)
                    && Objects.equals(proof, that.proof);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value, proof);
        }
    }

    /** Json Deserializer of Proof. */
    public static class ResponseDeserializer extends JsonDeserializer<EthGetProof.Proof> {

        /** Instantiates a new Response deserializer. */
        public ResponseDeserializer() {}

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public EthGetProof.Proof deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                return objectReader.readValue(jsonParser, EthGetProof.Proof.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
