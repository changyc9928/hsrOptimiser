package com.hsrOptimiser.controller;

import com.hsrOptimiser.properties.ConditionalStatBonus;
import com.hsrOptimiser.properties.Formula;
import com.hsrOptimiser.properties.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/bonus")
public class SetBonusController {
    @Autowired
    Properties properties;

    @GetMapping
    public HashMap<String, HashMap<Integer, ArrayList<ConditionalStatBonus>>> getConditionalSetBonus() {
        return properties.getConditionalSetBonus();
    }

    @GetMapping("/formula")
    public Formula getFormula() {
        return properties.getFormula();
    }
}
