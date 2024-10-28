package com.hsrOptimiser.domain.projectYatta;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class ProjectYattaData implements Serializable {
    Traces traces;
    ArrayList<Upgrade> upgrade;
}
