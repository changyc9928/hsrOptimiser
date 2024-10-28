package com.hsrOptimiser.domain.projectYatta;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class Traces implements Serializable {
    HashMap<String, SubSkills> subSkills;
}
