package com.piggy.microservice.notification.domain;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class NotificationSettings {

	@NotNull
	private Boolean active;

	@NotNull
	private Frequency frequency;


	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}
}