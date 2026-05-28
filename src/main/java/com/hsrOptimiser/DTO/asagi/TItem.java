package com.hsrOptimiser.DTO.asagi;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TItem{
	private double normal;
	private double total;
	private String name;
	private double superBreak;
	private double t_break;
	private double breakDot;
	private double elationSkill;
	private double enhancedSkill;
	private double enhancedNormal;
	private double enhancedUltimate;
	private double skill;
	private double talent;
	private double ultimate;
	private double technique;
	private double talent2;
	private double talent3;
	private double followUp;
}