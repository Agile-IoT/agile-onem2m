package com.srcsolution.things.interworking.mapper.content;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.srcsolution.things.interworking.data.OperationData;

public class JsonContentConverter implements ContentConverter {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
    }

    public JsonContentConverter() {
    }

    @Override
    public String getMediaType() {
        return "application/json";
    }

    @Override
    public String serialize(Map<String, Object> map) {
        String out = "";
        if (map != null) {
            try {
                out = MAPPER.writeValueAsString(serializeNode(map));
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot convert content data to " + getMediaType());
            }
        }
        return out;
    }

    private JsonNode serializeNode(Object obj) throws IOException {
        if (obj == null) {
            return NullNode.getInstance();

        } else if (obj instanceof Map) {
            ObjectNode json = MAPPER.createObjectNode();
            Map<String, ?> map = (Map<String, ?>) obj;
            for (String key : map.keySet()) {
                json.set(key, serializeNode(map.get(key)));
            }
            return json;

        } else if (obj instanceof OperationContent) {
            OperationContent operationContent = (OperationContent) obj;
            OperationData operation = operationContent.getOperation();

            ObjectNode paramsNode = MAPPER.createObjectNode();
            operation.getParameters().forEach((param, clazz) -> paramsNode.set(param, new TextNode(clazz.getSimpleName())));

            ObjectNode json = MAPPER.createObjectNode();
            json.set("isOperation", BooleanNode.TRUE);
            json.set("name", new TextNode(operation.getName()));
            json.set("url", new TextNode(operationContent.getUrl()));
            json.set("parameters", paramsNode);
            return json;

        } else if (obj.getClass().isArray()) {
            ArrayNode arrayNode = MAPPER.createArrayNode();
            Object[] array = (Object[]) obj;
            for (Object o : array) {
                arrayNode.add(serializeNode(o));
            }
            return arrayNode;

        } else if (obj.getClass().isEnum()) {
            return MAPPER.readTree(MAPPER.writeValueAsString(obj));

        } else if (obj instanceof ZonedDateTime) {
            return MAPPER.readTree(MAPPER.writeValueAsString(obj));

        } else if (obj instanceof Number) {
            return MAPPER.readTree(MAPPER.writeValueAsString(obj));

        } else if (obj instanceof Boolean) {
            return Boolean.TRUE.equals(obj) ? BooleanNode.TRUE : BooleanNode.FALSE;

        } else if (obj instanceof String) {
            return new TextNode((String) obj);

        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> deserialize(String s) {
        try {
            return (Map<String, Object>) deserializeNode(MAPPER.readTree(s));
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Cannot convert from " + getMediaType(), ioe);
        }
    }

    private Object deserializeNode(JsonNode node) {
        if (node instanceof NullNode) {
            return null;

        } else if (node instanceof NumericNode) {
            return node.numberValue();

        } else if (node instanceof TextNode) {
            return node.asText();

        } else if (node instanceof BooleanNode) {
            return node.booleanValue();

        } else if (node instanceof ArrayNode) {
            return StreamSupport.stream(node.spliterator(), false)
                                .map(this::deserializeNode).toArray(Object[]::new);

        } else if (node instanceof ObjectNode) {
            if (node.path("isOperation").asBoolean()) {
                OperationData op = new OperationData(node.path("name").asText(), node.path("url").asText());
                JsonNode parameters = node.path("parameters");

                for (Iterator<String> it = parameters.fieldNames(); it.hasNext(); ) {
                    String key = it.next();
                    op.addParameter(key, String.class);
                }

                return new OperationContent(op);

            } else {
                HashMap<String, Object> map = new HashMap<>();
                node.fields().forEachRemaining(nodeEntry -> map.put(nodeEntry.getKey(), deserializeNode(nodeEntry.getValue())));
                return map;
            }
        }
        throw new UnsupportedOperationException();
    }

}
