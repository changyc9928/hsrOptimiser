package com.hsrOptimiser.controller;

import com.hsrOptimiser.properties.ConditionalStatBonus;
import com.hsrOptimiser.properties.Formula;
import com.hsrOptimiser.properties.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SetBonusController {

    @Autowired
    Properties properties;

    @GetMapping("/set-bonus")
    public HashMap<String, HashMap<Integer, ArrayList<ConditionalStatBonus>>> getConditionalSetBonus() {
        return properties.getConditionalSetBonus();
    }

    @GetMapping("/formula")
    public Formula getFormula() {
        return properties.getFormula();
    }
}
