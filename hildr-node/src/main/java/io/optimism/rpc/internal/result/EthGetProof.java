/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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

  /** eth_getProof response. */
  public EthGetProof() {}

  /** json rpc result of object */
  public static class Proof {

    private String address;

    private String balance;

    private String codeHash;

    private String nonce;

    private String storageHash;

    private List<String> accountProof;

    private List<StorageProof> storageProof;

    public Proof() {}

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

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public String getBalanceRaw() {
      return this.balance;
    }

    public BigInteger getBalance() {
      return Numeric.decodeQuantity(balance);
    }

    public void setBalance(String balance) {
      this.balance = balance;
    }

    public String getCodeHash() {
      return codeHash;
    }

    public void setCodeHash(String codeHash) {
      this.codeHash = codeHash;
    }

    public String getNonce() {
      return nonce;
    }

    public void setNonce(String nonce) {
      this.nonce = nonce;
    }

    public String getStorageHash() {
      return storageHash;
    }

    public void setStorageHash(String storageHash) {
      this.storageHash = storageHash;
    }

    public List<String> getAccountProof() {
      return accountProof;
    }

    public void setAccountProof(List<String> accountProof) {
      this.accountProof = accountProof;
    }

    public List<StorageProof> getStorageProof() {
      return storageProof;
    }

    public void setStorageProof(List<StorageProof> storageProof) {
      this.storageProof = storageProof;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof EthGetProof.Proof)) {
        return false;
      }
      EthGetProof.Proof proof = (EthGetProof.Proof) o;

      if (getAddress() != null
          ? !getAddress().equals(proof.getAddress())
          : proof.getAddress() != null) {
        return false;
      }

      if (getBalanceRaw() != null
          ? !getBalanceRaw().equals(proof.getBalanceRaw())
          : proof.getBalanceRaw() != null) {
        return false;
      }

      if (getCodeHash() != null
          ? !getCodeHash().equals(proof.getCodeHash())
          : proof.getCodeHash() != null) {
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
      result = 31 * result + (getAccountProof() != null ? getAccountProof().hashCode() : 0);
      result = 31 * result + (getStorageProof() != null ? getStorageProof().hashCode() : 0);
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

    public StorageProof(String key, String value, List<String> proof) {
      this.key = key;
      this.value = value;
      this.proof = proof;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public List<String> getProof() {
      return proof;
    }

    public void setProof(List<String> proof) {
      this.proof = proof;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof StorageProof)) {
        return false;
      }

      StorageProof proof = (EthGetProof.StorageProof) o;

      if (getKey() != null ? !getKey().equals(proof.getKey()) : proof.getKey() != null) {
        return false;
      }
      if (getValue() != null ? !getValue().equals(proof.getValue()) : proof.getValue() != null) {
        return false;
      }
      return getProof() != null ? getProof().equals(proof.getProof()) : proof.getProof() == null;
    }

    @Override
    public int hashCode() {
      int result = getKey() != null ? getKey().hashCode() : 0;
      result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
      result = 31 * result + (getProof() != null ? getProof().hashCode() : 0);
      return result;
    }
  }

  /** Json Deserializer of Proof. */
  public static class ResponseDeserializer extends JsonDeserializer<EthGetProof.Proof> {

    private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

    @Override
    public EthGetProof.Proof deserialize(
        JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
        return objectReader.readValue(jsonParser, EthGetProof.Proof.class);
      } else {
        return null; // null is wrapped by Optional in above getter
      }
    }
  }

  // {
  //  "id": 1,
  //  "jsonrpc": "2.0",
  //  "result": {
  //    "accountProof": [
  //      "0xf90211a...0701bc80",
  //      "0xf90211a...0d832380",
  //      "0xf90211a...5fb20c80",
  //      "0xf90211a...0675b80",
  //      "0xf90151a0...ca08080"
  //    ],
  //    "balance": "0x0",
  //    "codeHash": "0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470",
  //    "nonce": "0x0",
  //    "storageHash": "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
  //    "storageProof": [
  //      {
  //        "key": "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
  //        "proof": [
  //          "0xf90211a...0701bc80",
  //          "0xf90211a...0d832380"
  //        ],
  //        "value": "0x1"
  //      }
  //    ]
  //  }
  // }

}
