package com.hsrOptimiser.DTO.hsrScanner;

import java.io.Serializable;
import lombok.Data;

@Data
public class SubStats implements Serializable {
    String key;
    double value;
    int count;
    int step;
}
