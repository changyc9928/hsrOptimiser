package com.hsrOptimiser.mapper;

import static com.hsrOptimiser.clientConfig.AsagiLightConeMetadata.getInfoById;

import com.hsrOptimiser.DTO.asagi.LightConesObj;
import com.hsrOptimiser.clientConfig.AsagiLightConeMetadata;

public class AsagiLightConeMapper {

    public static LightConesObj mapToObjectById(String id) {
        AsagiLightConeMetadata asagiLightConeMetadata = getInfoById(id);
        LightConesObj obj = new LightConesObj();
        obj.setKey(asagiLightConeMetadata.getInternalName());
        obj.setReality(asagiLightConeMetadata.getRarity());
        obj.setType(asagiLightConeMetadata.getType());
        return obj;
    }
}