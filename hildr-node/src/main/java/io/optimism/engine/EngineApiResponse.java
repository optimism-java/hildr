package io.optimism.engine;

import java.math.BigInteger;

public record EngineApiResponse<T>(String jsonrpc, BigInteger id, T result,
                                   EngineApiErrorPayload error) {
}
