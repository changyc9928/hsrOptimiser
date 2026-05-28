package com.hsrOptimiser.DTO.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum Path {
    @JsonAlias({"The Hunt", "hunt"})
    TheHunt,
    @JsonAlias("harmony")
    Harmony,
    @JsonAlias("preservation")
    Preservation,
    @JsonAlias("abundance")
    Abundance,
    @JsonAlias("nihility")
    Nihility,
    @JsonAlias("destruction")
    Destruction,
    @JsonAlias("erudition")
    Erudition,
    @JsonAlias("elation")
    Elation,
}
