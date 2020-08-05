package com.practicas.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
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
import org.springframework.stereotype.Service;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.practicas.model.LocalEvent;

@Service
public class CalendarService {

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
	private static final Logger LOGGER = Logger.getLogger("practicas.controller.UserController");

	private static final String ITEMS_JSONARRAY = "items";
	private static final String START_STRING = "start";
	private static final String DATETIME_STRING = "dateTime";
	private static final String END_STRING = "end";
	private static final String LOCATION_STRING = "location";
	private static final String DISTANCE_STRING = "distance";
	private static final String POSITION_STRING = "position";

	@Autowired
	private GCalendarRequest calendarUtils;

	@Autowired
	private HMapRequest mapUtils;

	private Event createEvent() {

		Event event = new Event();

		EventDateTime start = new EventDateTime();
		event.setStart(start);

		EventDateTime end = new EventDateTime();
		event.setEnd(end);

		return event;

	}

	private List<LocalEvent> possibleHours(JSONArray eventsInfo, DateTime startWorkingHour, DateTime endWorkingHour,
			DateTime durationStimated) {

		boolean firstWorkingTask = false;
		boolean longEvent = false;

		DateTime differenceTime;
		DateTime previousEndTaskHour = new DateTime();

		LocalEvent localEvent;
		List<LocalEvent> bestCombination = new LinkedList<>();

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
					previousEndTaskHour = endWorkingHour;

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

					DateTime endTaskHour = formatter.parseDateTime(eventsInfo.getJSONObject(i)
							.getJSONObject(START_STRING).getString(DATETIME_STRING).substring(11, 19));

					localEvent = new LocalEvent(startTaskHour, endTaskHour);

					if (!previousEndTaskHour.equals(endWorkingHour)
							&& eventsInfo.getJSONObject(i - 1).has(LOCATION_STRING)) {
						localEvent.setPreviousEvent(new LocalEvent(null, previousEndTaskHour,
								eventsInfo.getJSONObject(i - 1).getString(LOCATION_STRING)));
					} else {
						localEvent.setPreviousEvent(new LocalEvent(null, previousEndTaskHour));
					}

					if (eventsInfo.getJSONObject(i).has(LOCATION_STRING)) {

						localEvent.setLocation(eventsInfo.getJSONObject(i).getString(LOCATION_STRING));

						bestCombination.add(localEvent);
					} else
						bestCombination.add(localEvent);

					LOGGER.log(Level.INFO, "{0}", differenceTime.toString("HH:mm:ss"));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return bestCombination;

	}

	private void samePlace(float longitude, float latitude, LocalEvent processEvent, List<LocalEvent> bestCombination) {

		for (int j = 0; j < bestCombination.size(); j++) {
			if ((bestCombination.get(j).getLatitude() == 0.0)
					&& (bestCombination.get(j).getPreviousEvent().getLatitude() == 0.0)) {
				if ((bestCombination.get(j).getLatitude() == 0.0)
						&& bestCombination.get(j).getLocation().equals(processEvent.getLocation())) {
					bestCombination.get(j).setLatitude(latitude);
					bestCombination.get(j).setLongitude(longitude);
				} else {
					bestCombination.get(j).getPreviousEvent().setLatitude(latitude);
					bestCombination.get(j).getPreviousEvent().setLongitude(longitude);
				}
			}
		}

	}

	private LocalEvent minorDistance(List<LocalEvent> infoEvents, String place) {

		float minorDistanceCalculate = 99999;
		LocalEvent finalEvent = null;

		for (int i = 0; i < infoEvents.size(); i++) {
			if (infoEvents.get(i).getPreviousEvent().getLocation() != null) {
				JSONObject response = mapUtils.getDistanceBetweenPlaces(String.valueOf(infoEvents.get(i).getLatitude()),
						String.valueOf(infoEvents.get(i).getLongitude()), place);

				if (response != null) {
					float distance = Float.parseFloat(
							response.getJSONArray(ITEMS_JSONARRAY).getJSONObject(0).getString(DISTANCE_STRING));

					if (distance < minorDistanceCalculate) {
						JSONObject childResponse = mapUtils.getDistanceBetweenPlaces(
								response.getJSONArray(ITEMS_JSONARRAY).getJSONObject(0).getJSONObject(POSITION_STRING)
										.getString("lat"),
								response.getJSONArray(ITEMS_JSONARRAY).getJSONObject(0).getJSONObject(POSITION_STRING)
										.getString("lng"),
								infoEvents.get(i).getPreviousEvent().getLocation());

						if (childResponse != null) {
							float childDistance = Float.parseFloat(childResponse.getJSONArray(ITEMS_JSONARRAY)
									.getJSONObject(0).getString(DISTANCE_STRING));

							if (childDistance + distance < minorDistanceCalculate) {
								minorDistanceCalculate = childDistance + distance;
								finalEvent = infoEvents.get(i);
							}
						}
					}
				}
			}
		}
		return finalEvent;
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

	public void getUser(OAuth2AuthorizedClient user) {

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

	public List<LocalEvent> getBestHour(OAuth2AuthorizedClient user, String date, String hours) {

		String startDate = date.concat("T00:00:00Z");
		String endDate = date.concat("T23:59:00Z");

		String startHour = "08:00:00";
		DateTime startWorkingHour = formatter.parseDateTime(startHour);

		String endHour = "15:00:00";
		DateTime endWorkingHour = formatter.parseDateTime(endHour);

		DateTime durationStimated = formatter.parseDateTime(hours);

		JSONObject response = calendarUtils.getBestHourOwnCalendar(user, startDate, endDate);

		if (response.getJSONArray(ITEMS_JSONARRAY) != null) {
			JSONArray arr = response.getJSONArray(ITEMS_JSONARRAY);

			return possibleHours(arr, startWorkingHour, endWorkingHour, durationStimated);
		}
		return new LinkedList<>();

	}

	public void bestHourMoreCalendars(OAuth2AuthorizedClient user, String email, String date, String hours) {

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

	public void getBestCombination(OAuth2AuthorizedClient user, String date, String hours, String place) {

		List<LocalEvent> bestCombination = getBestHour(user, date, hours);

		for (int i = 0; i < bestCombination.size(); i++) {
			if (bestCombination.get(i).getLocation() != null) {
				JSONObject response = mapUtils.getInfoPlace(bestCombination.get(i).getLocation());

				if (response != null) {
					JSONObject position = response.getJSONArray(ITEMS_JSONARRAY).getJSONObject(0)
							.getJSONObject(POSITION_STRING);

					bestCombination.get(i).setLatitude(Float.valueOf(position.getString("lat")));
					bestCombination.get(i).setLongitude(Float.valueOf(position.getString("lng")));

					samePlace(Float.valueOf(position.getString("lng")), Float.valueOf(position.getString("lat")),
							bestCombination.get(i), bestCombination);
				}
			}
		}
		minorDistance(bestCombination, place);
	}

	public void addUser(OAuth2AuthorizedClient user) {

		JSONObject jsonObject = new JSONObject(createEvent());

		jsonObject.getJSONObject(START_STRING).put(DATETIME_STRING, "2020-07-24T01:00:00Z");
		jsonObject.getJSONObject(END_STRING).put(DATETIME_STRING, "2020-07-24T04:00:00Z");

		calendarUtils.addEvent(user, jsonObject);
	}

	public void modifyUser() {
		// TODO
	}

	public void deleteUser(OAuth2AuthorizedClient user, String idEvent) {

		calendarUtils.deleteEvent(user, idEvent);
	}

}
