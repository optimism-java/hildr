package io.optimism.engine;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.Objects;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.Response;

/**
 * The type OpEthExecutionPayload.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class OpEthExecutionPayload extends Response<OpEthExecutionPayload.ExecutionPayloadObj> {

    /** Instantiates a new Op eth execution payload. */
    public OpEthExecutionPayload() {}

    /**
     * Gets execution payload.
     *
     * @return the execution payload
     */
    public ExecutionPayload getExecutionPayload() {
        return getResult().getExecutionPayload().toExecutionPayload();
    }

    @Override
    @JsonDeserialize(using = ResponseDeserializer.class)
    public void setResult(ExecutionPayloadObj result) {
        super.setResult(result);
    }

    /**
     * The type Execution payload obj.
     * @since 0.2.6
     */
    public static class ExecutionPayloadObj {

        /** The Execution payload result. */
        private ExecutionPayload.ExecutionPayloadRes executionPayload;

        /** Instantiates a new Execution payload obj. */
        public ExecutionPayloadObj() {}

        /**
         * Instantiates a new Execution payload obj.
         *
         * @param executionPayload the execution payload result
         */
        public ExecutionPayloadObj(ExecutionPayload.ExecutionPayloadRes executionPayload) {
            this.executionPayload = executionPayload;
        }

        /**
         * Gets execution payload result.
         *
         * @return the execution payload result
         */
        public ExecutionPayload.ExecutionPayloadRes getExecutionPayload() {
            return executionPayload;
        }

        /**
         * Sets execution payload.
         *
         * @param executionPayload the execution payload result
         */
        public void setExecutionPayload(ExecutionPayload.ExecutionPayloadRes executionPayload) {
            this.executionPayload = executionPayload;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ExecutionPayloadObj that)) {
                return false;
            }
            return Objects.equals(executionPayload, that.executionPayload);
        }

        @Override
        public int hashCode() {
            return Objects.hash(executionPayload);
        }
    }

    /** Json Deserializer of ExecutionPayloadObj. */
    public static class ResponseDeserializer extends JsonDeserializer<ExecutionPayloadObj> {

        private final ObjectReader objectReader;

        /** Instantiates a new Response deserializer. */
        public ResponseDeserializer() {
            this.objectReader = ObjectMapperFactory.getObjectReader();
        }

        @Override
        public ExecutionPayloadObj deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                return objectReader.readValue(jsonParser, ExecutionPayloadObj.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
