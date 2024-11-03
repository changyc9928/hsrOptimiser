package com.hsrOptimiser.domain.projectYatta;

import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

@Data
public class Traces implements Serializable {

    HashMap<String, SubSkills> subSkills;
}
