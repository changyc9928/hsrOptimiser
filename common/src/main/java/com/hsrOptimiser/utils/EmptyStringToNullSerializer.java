package com.hsrOptimiser.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.springframework.boot.jackson.JsonObjectDeserializer;

public class EmptyStringToNullSerializer extends JsonObjectDeserializer<String> {

    @Override
    protected String deserializeObject(JsonParser jsonParser, DeserializationContext context,
        ObjectCodec codec, JsonNode tree) throws IOException {
        if (tree.textValue().isEmpty()) {
            return null;
        } else {
            return tree.textValue();
        }
    }
}

