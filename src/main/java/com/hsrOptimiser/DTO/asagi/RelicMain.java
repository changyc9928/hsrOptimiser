package com.hsrOptimiser.DTO.asagi;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RelicMain{
	private String body;
	private String feet;
	private String sphere;
	private String rope;
}