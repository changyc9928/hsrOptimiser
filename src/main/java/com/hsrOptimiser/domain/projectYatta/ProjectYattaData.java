package com.hsrOptimiser.domain.projectYatta;

import java.io.Serializable;
import java.util.ArrayList;
import lombok.Data;

@Data
public class ProjectYattaData implements Serializable {

    Traces traces;
    ArrayList<Upgrade> upgrade;
}
