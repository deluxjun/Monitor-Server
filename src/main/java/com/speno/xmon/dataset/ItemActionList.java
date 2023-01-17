package com.speno.xmon.dataset;

public class ItemActionList {
	private String agentName	="";
	private String actionName	= "";
	private String title 				= "";
	private String description 	= "";
	private String aggreUseYN = "";
	private String healthUseYN = "";

	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAggreUseYN() {
		return aggreUseYN;
	}
	public void setAggreUseYN(String aggreUseYN) {
		this.aggreUseYN = aggreUseYN;
	}
	public String getHealthUseYN() {
		return healthUseYN;
	}
	public void setHealthUseYN(String healthUseYN) {
		this.healthUseYN = healthUseYN;
	}

}
