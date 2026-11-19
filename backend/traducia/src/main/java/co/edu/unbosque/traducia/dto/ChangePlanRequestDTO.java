package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.PlanType;
public class ChangePlanRequestDTO {
	
	private PlanType plan;

	public ChangePlanRequestDTO() {
	}

	public ChangePlanRequestDTO(PlanType plan) {
		this.plan = plan;
	}

	public PlanType getPlan() {
		return plan;
	}

	public void setPlan(PlanType plan) {
		this.plan = plan;
	}
}


