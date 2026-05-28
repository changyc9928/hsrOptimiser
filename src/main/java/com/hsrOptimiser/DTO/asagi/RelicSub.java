package com.hsrOptimiser.DTO.asagi;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RelicSub{
	private int defFlat;
	private int breakRate;
	private int hpFlat;
	private int hpRate;
	private int spdFlat;
	private int defRate;
	private int atkFlat;
	private int critRate;
	private int effectRes;
	private int atkRate;
	private int critDmg;
	private int effectHit;
}