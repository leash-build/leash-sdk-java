package build.leash.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Internal Jackson facade — the SDK uses exactly one shared
 * {@link ObjectMapper}, configured once.
 */
public final class Json {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {}

    public static String writeValueAsString(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            // The body we serialise is always SDK-built — failure here is a bug.
            throw new IllegalStateException("Failed to serialise request body", e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> T treeToValue(JsonNode node, Class<T> type) {
        try {
            return MAPPER.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> T treeToValue(JsonNode node, TypeReference<T> typeRef) {
        if (node == null) return null;
        return MAPPER.convertValue(node, typeRef);
    }

    public static ObjectNode newObject() {
        return MAPPER.createObjectNode();
    }
}
