package com.practicas.model;

import org.joda.time.DateTime;

import lombok.Data;

@Data
public class LocalEvent {

	private DateTime initialHour;
	private DateTime endHour;
	private float latitude;
	private float longitude;
	private String location;

	private LocalEvent previousEvent;

	public LocalEvent() {

	}

	public LocalEvent(DateTime initialHour, DateTime endHour) {
		super();
		this.initialHour = initialHour;
		this.endHour = endHour;
	}

	public LocalEvent(DateTime initialHour, DateTime endHour, String location) {
		super();
		this.initialHour = initialHour;
		this.endHour = endHour;
		this.location = location;
	}

	public LocalEvent(DateTime initialHour, DateTime endHour, float latitude, float longitude) {
		super();
		this.initialHour = initialHour;
		this.endHour = endHour;
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
