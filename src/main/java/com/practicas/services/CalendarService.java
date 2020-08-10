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
	private static final Logger LOGGER = Logger.getLogger(CalendarService.class.toString());

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

	/**
	 * Create the structure of an event
	 * 
	 * @return : Event object with the structure.
	 */
	private Event createEvent() {

		Event event = new Event();

		EventDateTime start = new EventDateTime();
		event.setStart(start);

		EventDateTime end = new EventDateTime();
		event.setEnd(end);

		return event;

	}

	/**
	 * Calculate the events that have the possibility to add exactly before them the
	 * Event that the User want to add.
	 * 
	 * @param eventsInfo       : List of Events that the User have on the calendar
	 *                         the day that is looking for.
	 * @param startWorkingHour : The first hour we are going to look, the hour that
	 *                         the user start working.
	 * @param endWorkingHour   : Last hour we are going to look for, the hour that
	 *                         the user stop working.
	 * @param durationStimated : Duration of the Event that the user want to add.
	 * 
	 * @return : List of Events that is possible to add the Event that the User want
	 *         before them.
	 */
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

	/**
	 * Get a look if there is another event with the same address that the one that
	 * have been process before, in case of find one it will add the longitude and
	 * latitude to it.
	 * 
	 * @param processEvent    : Information of the event that have been process
	 *                        before.
	 * @param bestCombination : List of Events of the day that the user is looking
	 *                        for.
	 */
	private void samePlace(LocalEvent processEvent, List<LocalEvent> bestCombination) {

		for (int j = 0; j < bestCombination.size(); j++) {
			if ((bestCombination.get(j).getLatitude() == 0.0)
					|| (bestCombination.get(j).getPreviousEvent().getLatitude() == 0.0)) {
				if ((bestCombination.get(j).getLatitude() == 0.0)
						&& bestCombination.get(j).getLocation().equals(processEvent.getLocation())) {
					bestCombination.get(j).setLatitude(processEvent.getLatitude());
					bestCombination.get(j).setLongitude(processEvent.getLongitude());
				} else if ((bestCombination.get(j).getPreviousEvent().getLatitude() == 0.0)
						&& bestCombination.get(j).getPreviousEvent().getLocation().equals(processEvent.getLocation())) {
					bestCombination.get(j).getPreviousEvent().setLatitude(processEvent.getLatitude());
					bestCombination.get(j).getPreviousEvent().setLongitude(processEvent.getLongitude());
				}
			}
		}

	}

	/**
	 * Calculate the pair of events that generate the minor distance for go from the
	 * route to the place propose by the User.
	 * 
	 * @param infoEvents : Filter List of Events, have the Events that can have the
	 *                   Event propose by the User.
	 * @param place      : Name of the place that the User is looking for.
	 * 
	 * @return : A LocalEvent Object with the information of the pair of Events that
	 *         present the minor distance.
	 */
	private LocalEvent minorDistance(List<LocalEvent> infoEvents, String place) {

		float minorDistanceCalculate = 99999;
		LocalEvent finalEvent = null;

		for (int i = 0; i < infoEvents.size(); i++) {
			if (infoEvents.get(i).getPreviousEvent().getLocation() != null) {
				JSONObject response = mapUtils.getDistanceBetweenPlaces(infoEvents.get(i));

				if (response != null) {

					JSONObject section = response.getJSONArray("routes").getJSONObject(0).getJSONArray("sections")
							.getJSONObject(0);

					JSONObject responseBetter = mapUtils.getBestOption(infoEvents.get(i), section.getString("polyline"),
							place);

					JSONObject responseMediumRoute = mapUtils.getDistanceBetweenPlaces(infoEvents.get(i),
							responseBetter.getJSONArray(ITEMS_JSONARRAY).getJSONObject(0).getJSONObject("position")
									.getString("lat"),
							responseBetter.getJSONArray(ITEMS_JSONARRAY).getJSONObject(0).getJSONObject("position")
									.getString("lng"));

					float distance = Float.parseFloat(
							responseBetter.getJSONArray(ITEMS_JSONARRAY).getJSONObject(0).getString(DISTANCE_STRING))
							+ Float.parseFloat(
									responseMediumRoute.getJSONArray("routes").getJSONObject(0).getJSONArray("sections")
											.getJSONObject(0).getJSONObject("summary").getString("length"));

					if ((distance - Float.parseFloat(
							section.getJSONObject("summary").getString("length"))) < minorDistanceCalculate) {

						minorDistanceCalculate = distance
								- Float.parseFloat(section.getJSONObject("summary").getString("length"));
						finalEvent = infoEvents.get(i);
					}

				}
			}
		}
		return finalEvent;
	}

	/**
	 * Sort the given JSON with the starting time of the Events.
	 * 
	 * @param valuesJSON : List of Events.
	 * 
	 * @return : A JSONArray sorted.
	 */
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

	/**
	 * Show all the events that start 2 days before of the request to the API.
	 * 
	 * @param user : Token of the User.
	 */
	public void getEvents2DaysBack(OAuth2AuthorizedClient user) {

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

	/**
	 * Calculate if there is site on the User calendar for the Event that he/she
	 * want to set.
	 * 
	 * @param user  : Token of the User.
	 * @param date  : Date of the Event.
	 * @param hours : Estimation of the duration of the Event.
	 * 
	 * @return : List of LocalEvent with information in pairs of the Events that
	 *         have place previously for the new Event.
	 */
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

	/**
	 * Calculate if there is a site on the two calendars for that Event.
	 * 
	 * @param user  : Token of the User.
	 * @param email : Email of the other User that we want to look his/her calendar.
	 * @param date  : Date of the Event that the User want to set.
	 * @param hours : Estimation of the duration of the Event.
	 */
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

	/**
	 * Calculate if there time on the day set by the User for that Event, in case of
	 * have time calculate which one is the best option base on the location. The
	 * best option is the one that lose less time for arrive to the destination.
	 * 
	 * @param user  : Token of the User.
	 * @param date  : Date of the Event that the User want to set.
	 * @param hours : Estimation of the duration that is going to have the Event.
	 * @param place : Location where the Event is going to take place.
	 */
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

					samePlace(bestCombination.get(i), bestCombination);
				}
			}
		}
		LocalEvent finalEvent = minorDistance(bestCombination, place);

		System.out.println("Evento previo : " + finalEvent.getPreviousEvent().getLocation() + " Evento siguiente : "
				+ finalEvent.getLocation());
	}

	/**
	 * Add a new Event.
	 * 
	 * @param user : Token of the User.
	 */
	public void addEvent(OAuth2AuthorizedClient user) {

		JSONObject jsonObject = new JSONObject(createEvent());

		jsonObject.getJSONObject(START_STRING).put(DATETIME_STRING, "2020-07-24T01:00:00Z");
		jsonObject.getJSONObject(END_STRING).put(DATETIME_STRING, "2020-07-24T04:00:00Z");

		calendarUtils.addEvent(user, jsonObject);
	}

	/**
	 * TODO : Modify an Event.
	 */
	public void modifyEvent() {
		// TODO
	}

	/**
	 * Delete and concrete Event.
	 * 
	 * @param user    : Token of the User.
	 * @param idEvent : Id of the Event that we want to delete.
	 */
	public void deleteEvent(OAuth2AuthorizedClient user, String idEvent) {

		calendarUtils.deleteEvent(user, idEvent);
	}

}
