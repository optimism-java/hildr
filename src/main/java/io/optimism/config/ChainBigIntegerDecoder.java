package io.optimism.config;

import java.math.BigInteger;
import org.apache.commons.lang3.StringUtils;
import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.decoder.LeafDecoder;
import org.github.gestalt.config.decoder.Priority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Decode BigInteger field in Chain Config.
 */
public class ChainBigIntegerDecoder extends LeafDecoder<BigInteger> {

    /**
     * ChainBigIntegerDecoder constructor.
     */
    public ChainBigIntegerDecoder() {}

    @Override
    protected GResultOf<BigInteger> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<BigInteger> results;
        String value = node.getValue().orElse("");
        if (!org.github.gestalt.config.utils.StringUtils.isReal(value)) {
            results = GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
        } else {
            try {
                BigInteger bigInteger = new BigInteger(value);
                results = GResultOf.result(bigInteger);
            } catch (NumberFormatException e) {
                results = GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
            }
        }
        return results;
    }

    @Override
    public Priority priority() {
        return Priority.HIGH;
    }

    @Override
    public String name() {
        return "BigInteger-Cust";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode configNode, TypeCapture<?> type) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return path.startsWith("config.chainConfig") && BigInteger.class.isAssignableFrom(type.getRawType());
    }
}
