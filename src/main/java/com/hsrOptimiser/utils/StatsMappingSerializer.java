package com.hsrOptimiser.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.hsrOptimiser.properties.Properties;
import java.io.IOException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;

public class StatsMappingSerializer extends JsonObjectDeserializer<String> {

    @Autowired
    Properties properties;

    @Override
    protected String deserializeObject(JsonParser jsonParser, DeserializationContext context,
        ObjectCodec codec, JsonNode tree) throws IOException {
        return Objects.requireNonNull(properties.getRelicStatMapper().get(tree.textValue()));
    }
}
