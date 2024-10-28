package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum Slot {
    Head,
    Feet,
    Body,
    Hands,
    @JsonAlias("Link Rope")
    LinkRope,
    @JsonAlias("Planar Sphere")
    PlanarSphere,
}
