package com.practicas.model;

import org.joda.time.DateTime;

public class LocalEvent {

	private DateTime initialHour;
	private DateTime endHour;
	private float latitude;
	private float longitude;
	private String location;

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

	public DateTime getInitialHour() {
		return initialHour;
	}

	public void setInitialHour(DateTime initialHour) {
		this.initialHour = initialHour;
	}

	public DateTime getEndHour() {
		return endHour;
	}

	public void setEndHour(DateTime endHour) {
		this.endHour = endHour;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
