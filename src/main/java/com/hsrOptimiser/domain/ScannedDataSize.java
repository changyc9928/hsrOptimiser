package com.hsrOptimiser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScannedDataSize {
    int numCharacters;
    int numLightCones;
    int numRelics;
}
