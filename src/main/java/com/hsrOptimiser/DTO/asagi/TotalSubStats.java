package com.hsrOptimiser.DTO.asagi;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TotalSubStats{
	private double hpFlat;
	private double hpRate;
	private double spdFlat;
	private double atkFlat;
	private double critRate;
	private double atkRate;
	private double critDmg;
	private double effectHit;
	private double defFlat;
	private double breakRate;
	private double defRate;
	private double effectRes;
	private String key;

	public void addHpFlat(double hpFlat) {
		this.hpFlat += hpFlat;
	}

	public void addHpRate(double hpRate) {
		this.hpRate += hpRate;
	}

	public void addSpdFlat(double spdFlat) {
		this.spdFlat += spdFlat;
	}

	public void addAtkFlat(double atkFlat) {
		this.atkFlat += atkFlat;
	}

	public void addCritRate(double critRate) {
		this.critRate += critRate;
	}

	public void addAtkRate(double atkRate) {
		this.atkRate += atkRate;
	}

	public void addCritDmg(double critDmg) {
		this.critDmg += critDmg;
	}

	public void addEffectHit(double effectHit) {
		this.effectHit += effectHit;
	}

	public void addDefFlat(double defFlat) {
		this.defFlat += defFlat;
	}

	public void addBreakRate(double breakRate) {
		this.breakRate += breakRate;
	}

	public void addDefRate(double defRate) {
		this.defRate += defRate;
	}

	public void addEffectRes(double effectRes) {
		this.effectRes += effectRes;
	}
}