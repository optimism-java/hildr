package io.optimism.network;

import com.google.common.base.Objects;
import io.libp2p.etc.types.ByteBufExtKt;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;

/**
 * The type OpStackEnrData.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class OpStackEnrData {

    private UInt64 chainId;

    private UInt64 version;

    /**
     * Instantiates a new Op stack enr data.
     *
     * @param chainId the chain id
     * @param version the version
     */
    public OpStackEnrData(UInt64 chainId, UInt64 version) {
        this.chainId = chainId;
        this.version = version;
    }

    /**
     * Gets chain id.
     *
     * @return the chain id
     */
    public UInt64 getChainId() {
        return chainId;
    }

    /**
     * Sets chain id.
     *
     * @param chainId the chain id
     */
    public void setChainId(UInt64 chainId) {
        this.chainId = chainId;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public UInt64 getVersion() {
        return version;
    }

    /**
     * Sets version.
     *
     * @param version the version
     */
    public void setVersion(UInt64 version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpStackEnrData that)) {
            return false;
        }
        return Objects.equal(chainId, that.chainId) && Objects.equal(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chainId, version);
    }

    @Override
    public String toString() {
        return "OpStackEnrData{chainId=%s, version=%s}".formatted(chainId, version);
    }

    /**
     * Encode bytes.
     *
     * @return the bytes
     */
    public Bytes encode() {
        ByteBuf buffer = Unpooled.buffer(20);
        ByteBufExtKt.writeUvarint(buffer, chainId.toLong());
        ByteBufExtKt.writeUvarint(buffer, version.toLong());
        return Bytes.wrap(ByteBufUtil.getBytes(buffer));
    }

    /**
     * Decode op stack enr data.
     *
     * @param value the value
     * @return the op stack enr data
     */
    public static OpStackEnrData decode(Bytes value) {
        ByteBuf buffer = Unpooled.wrappedBuffer(value.toArray());
        UInt64 chainId = UInt64.valueOf(ByteBufExtKt.readUvarint(buffer));
        UInt64 version = UInt64.valueOf(ByteBufExtKt.readUvarint(buffer));
        return new OpStackEnrData(chainId, version);
    }
}
