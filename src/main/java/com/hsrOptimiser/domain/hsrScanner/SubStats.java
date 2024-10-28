package com.hsrOptimiser.domain.hsrScanner;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubStats implements Serializable {
    Stats key;
    float value;
}
