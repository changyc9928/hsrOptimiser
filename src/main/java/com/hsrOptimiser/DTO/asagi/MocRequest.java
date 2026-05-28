package com.hsrOptimiser.DTO.asagi;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MocRequest{
	private Object body;
	private String battleMode;
	private int round;
	private int topazStrategy;
	private List<String> followerWeaks;
	private int startSp;
	private String simMode;
	private boolean createLink;
	private boolean fixBreak;
	private boolean enableBreak;
	private String mocScenario;
	private List<CharactersItem> characters;
	private int bronyaStrategy;
	private boolean castoriceStrategy;
	private int boothillStrategy;
	private String enemyLevel;
	private boolean distributeEp;
	private List<String> boss2Weaks;
	private List<List<String>> waveSet;
	private List<String> bossWeaks;
	private String mocBuff;
	private boolean inputActualNumber;
	private List<TotalSubStats> totalSubStatus;
	private int healerSpeed;
	private boolean enableDefeated;
	private boolean exoToughness;
	private boolean enableWaveReset;
}