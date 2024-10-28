package com.hsrOptimiser.domain.projectYatta;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class SubSkills implements Serializable {
    String name;
    ArrayList<StatusList> statusList;
}
