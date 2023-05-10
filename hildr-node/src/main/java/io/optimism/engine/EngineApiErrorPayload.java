package io.optimism.engine;

import java.math.BigInteger;

public record EngineApiErrorPayload(BigInteger code, String message, String data) {
}
