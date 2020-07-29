package com.practicas.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

@Service
public class CalendarService {

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
	private static final Logger LOGGER = Logger.getLogger("practicas.controller.UserController");

	private static final String ITEMS_JSONARRAY = "items";
	private static final String START_STRING = "start";
	private static final String DATETIME_STRING = "dateTime";
	private static final String END_STRING = "end";

	@Autowired
	private CalendarUtils calendarUtils;

	private Event createEvent() {

		Event event = new Event();

		EventDateTime start = new EventDateTime();
		event.setStart(start);

		EventDateTime end = new EventDateTime();
		event.setEnd(end);

		return event;

	}

	private void possibleHours(JSONArray eventsInfo, DateTime startWorkingHour, DateTime endWorkingHour,
			DateTime durationStimated) {

		boolean firstWorkingTask = false;
		boolean longEvent = false;

		DateTime differenceTime;
		DateTime previousEndTaskHour = new DateTime();

		for (int i = 0; i < eventsInfo.length(); i++) {

			try {

				LOGGER.log(Level.INFO, "Inicio : {0} ", eventsInfo.getJSONObject(i).getJSONObject(START_STRING)
						.getString(DATETIME_STRING).substring(11, 19));
				LOGGER.log(Level.INFO, "Fin : {0} ", eventsInfo.getJSONObject(i).getJSONObject(END_STRING)
						.getString(DATETIME_STRING).substring(11, 19));

				DateTime startTaskHour = formatter.parseDateTime(eventsInfo.getJSONObject(i).getJSONObject(START_STRING)
						.getString(DATETIME_STRING).substring(11, 19));

				if (startTaskHour.isAfter(endWorkingHour))
					break;

				else if (!firstWorkingTask && !startTaskHour.isBefore(startWorkingHour)) {
					differenceTime = startTaskHour.minusMinutes(startWorkingHour.getMinuteOfDay());

					firstWorkingTask = true;
				}

				else {
					if (!longEvent)
						previousEndTaskHour = formatter.parseDateTime(eventsInfo.getJSONObject(i - 1)
								.getJSONObject(END_STRING).getString(DATETIME_STRING).substring(11, 19));

					differenceTime = startTaskHour.minusMinutes(previousEndTaskHour.getMinuteOfDay());

					longEvent = (previousEndTaskHour.isAfter(formatter.parseDateTime(eventsInfo.getJSONObject(i)
							.getJSONObject(END_STRING).getString(DATETIME_STRING).substring(11, 19))));

				}

				if (differenceTime.isAfter(durationStimated) || differenceTime.equals(durationStimated)) {
					LOGGER.log(Level.INFO, "{0}", differenceTime.toString("HH:mm:ss"));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	public JSONArray sortJSON(JSONArray valuesJSON) {

		JSONArray sortedJSON = new JSONArray();

		List<JSONObject> jsonValues = new ArrayList<>();
		for (int i = 0; i < valuesJSON.length(); i++) {
			jsonValues.add(valuesJSON.getJSONObject(i));
		}

		jsonValues = jsonValues.stream().sorted((JSONObject a, JSONObject b) -> {
			DateTime startEventA = formatter
					.parseDateTime(a.getJSONObject(START_STRING).getString(DATETIME_STRING).substring(11, 19));
			DateTime startEventB = formatter
					.parseDateTime(b.getJSONObject(START_STRING).getString(DATETIME_STRING).substring(11, 19));

			return startEventA.compareTo(startEventB);
		}).collect(Collectors.toList());

		for (int i = 0; i < valuesJSON.length(); i++) {
			sortedJSON.put(jsonValues.get(i));
		}

		return sortedJSON;

	}

	public void getUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {

		Clock clock = Clock.systemDefaultZone();
		String date = Instant.now(clock).minus(2, ChronoUnit.DAYS).toString();

		JSONObject response = calendarUtils.getEvents2DaysBack(user, date);

		if (response != null) {
			JSONArray arr = response.getJSONArray(ITEMS_JSONARRAY);
			for (int i = 0; i < arr.length(); i++) {
				LOGGER.log(Level.INFO, "{0}", arr.getJSONObject(i).getString("id"));
			}
		}

	}

	public void getBestHour(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String date, @PathVariable String hours) {

		String startDate = date.concat("T00:00:00Z");
		String endDate = date.concat("T23:59:00Z");

		String startHour = "08:00:00";
		DateTime startWorkingHour = formatter.parseDateTime(startHour);

		String endHour = "15:00:00";
		DateTime endWorkingHour = formatter.parseDateTime(endHour);

		DateTime durationStimated = formatter.parseDateTime(hours);

		JSONObject response = calendarUtils.getBestHourOwnCalendar(user, startDate, endDate);

		if (response != null) {
			JSONArray arr = response.getJSONArray(ITEMS_JSONARRAY);

			possibleHours(arr, startWorkingHour, endWorkingHour, durationStimated);
		}

	}

	public void bestHourMoreCalendars(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String email, @PathVariable String date, @PathVariable String hours) {

		String startDate = date.concat("T00:00:00Z");
		String endDate = date.concat("T23:59:00Z");

		String startHour = "08:00:00";
		DateTime startWorkingHour = formatter.parseDateTime(startHour);

		String endHour = "15:00:00";
		DateTime endWorkingHour = formatter.parseDateTime(endHour);

		DateTime durationStimated = formatter.parseDateTime(hours);

		String[] emailList = { "primary", email };

		JSONArray arr = new JSONArray();

		for (int i = 0; i < emailList.length; i++) {

			JSONObject response = calendarUtils.getBestHourMoreCalendars(user, emailList[i], startDate, endDate);

			if (response != null) {
				if (i == 0)
					arr = response.getJSONArray(ITEMS_JSONARRAY);
				else {

					JSONArray elements = response.getJSONArray(ITEMS_JSONARRAY);

					for (int j = 0; j < elements.length(); j++) {
						arr.put(elements.get(j));
					}
				}
			}

		}

		arr = sortJSON(arr);

		possibleHours(arr, startWorkingHour, endWorkingHour, durationStimated);

	}

	public void addUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {

		JSONObject jsonObject = new JSONObject(createEvent());

		jsonObject.getJSONObject(START_STRING).put(DATETIME_STRING, "2020-07-24T01:00:00Z");
		jsonObject.getJSONObject(END_STRING).put(DATETIME_STRING, "2020-07-24T04:00:00Z");

		calendarUtils.addEvent(user, jsonObject);
	}

	public void modifyUser() {
		// TODO
	}

	public void deleteUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String idEvent) {

		calendarUtils.deleteEvent(user, idEvent);
	}

}
