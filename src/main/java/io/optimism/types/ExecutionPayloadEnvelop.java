package io.optimism.types;

import io.optimism.types.enums.BlockVersion;
import org.apache.tuweni.bytes.Bytes;

/**
 * The type ExecutionPayloadEnvelop.
 *
 * @param parentBeaconBlockRoot the parent beacon block root
 * @param executionPayload      the execution payload
 * @author grapebaba
 * @since 0.2.6
 */
public record ExecutionPayloadEnvelop(Bytes parentBeaconBlockRoot, ExecutionPayload executionPayload) {

    /**
     * From execution payload envelop.
     *
     * @param data the data
     * @return the execution payload envelop
     */
    public static ExecutionPayloadEnvelop from(Bytes data) {
        if (data.size() < 32) {
            throw new IllegalArgumentException(
                    "scope too small to decode execution payloadEnvelop envelope: %d".formatted(data.size()));
        }

        Bytes parentBeaconBlockRoot = data.slice(0, 32);

        ExecutionPayloadSSZ executionPayloadSSZ = ExecutionPayloadSSZ.from(data.slice(32), BlockVersion.V3);
        ExecutionPayload executionPayload =
                ExecutionPayload.from(executionPayloadSSZ, parentBeaconBlockRoot.toHexString());
        return new ExecutionPayloadEnvelop(parentBeaconBlockRoot, executionPayload);
    }
}
