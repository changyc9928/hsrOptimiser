package com.hsrOptimiser.domain.projectYatta;

import java.io.Serializable;
import java.util.ArrayList;
import lombok.Data;

@Data
public class SubSkills implements Serializable {

    String name;
    ArrayList<StatusList> statusList;
}
