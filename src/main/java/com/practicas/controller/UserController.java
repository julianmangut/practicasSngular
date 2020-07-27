package com.practicas.controller;

import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RestController
@RequestMapping("/users")
public class UserController {

	DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");

	private Event createEvent(String startTime, String endTime) {

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

		for (int i = 0; i < eventsInfo.length(); i++) {

			try {

				System.out.print("Inicio : "
						+ eventsInfo.getJSONObject(i).getJSONObject("start").getString("dateTime").substring(11, 19));
				System.out.println("  Fin : "
						+ eventsInfo.getJSONObject(i).getJSONObject("end").getString("dateTime").substring(11, 19));

				DateTime startTaskHour = formatter.parseDateTime(
						eventsInfo.getJSONObject(i).getJSONObject("start").getString("dateTime").substring(11, 19));

				if (!firstWorkingTask) {
					if (startTaskHour.isBefore(startWorkingHour))
						continue;
				}

				if (startTaskHour.isAfter(endWorkingHour))
					break;

				DateTime differenceTime;

				if (!firstWorkingTask) {
					differenceTime = startTaskHour.minusMinutes(startWorkingHour.getMinuteOfDay());

					firstWorkingTask = true;
				} else {
					DateTime previousEndTaskHour = formatter.parseDateTime(eventsInfo.getJSONObject(i - 1)
							.getJSONObject("end").getString("dateTime").substring(11, 19));

					differenceTime = startTaskHour.minusMinutes(previousEndTaskHour.getMinuteOfDay());
				}

				if (differenceTime.isAfter(durationStimated)) {
					System.out.println(differenceTime.toString("HH:mm:ss"));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
	
	public JSONArray sortJSON (JSONArray valuesJSON) {
		
		JSONArray sortedJSON = new JSONArray();
		
		List<JSONObject> jsonValues = new ArrayList<JSONObject>();
	    for (int i = 0; i < valuesJSON.length(); i++) {
	        jsonValues.add(valuesJSON.getJSONObject(i));
	    }
		
		Collections.sort(jsonValues, new Comparator<JSONObject>() {
			@Override
			public int compare (JSONObject a, JSONObject b) {
				
				DateTime startEventA = formatter.parseDateTime(a.getJSONObject("start").getString("dateTime").substring(11, 19));
				DateTime startEventB = formatter.parseDateTime(b.getJSONObject("start").getString("dateTime").substring(11, 19));
				
				return startEventA.compareTo(startEventB);
			}
		});
		
		for (int i = 0; i < valuesJSON.length(); i++) {
			sortedJSON.put(jsonValues.get(i));
	    }
		
		return sortedJSON;
		
	}

	@GetMapping
	public String getUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {

		Clock clock = Clock.systemDefaultZone();
		String date = Instant.now(clock).minus(2, ChronoUnit.DAYS).toString();

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url(
				"https://www.googleapis.com/calendar/v3/calendars/primary/events?maxResults=10&timeMin="
						+ date)
				.method("GET", null).addHeader("Authorization", "Bearer " + user.getAccessToken().getTokenValue())
				.build();

		JSONObject obj = null;
		try (Response response = client.newCall(request).execute()) {
			obj = new JSONObject(response.body().string());
		} catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}

		JSONArray arr = obj.getJSONArray("items");
		for (int i = 0; i < arr.length(); i++) {
			System.out.println(arr.getJSONObject(i).getString("id"));
		}

		return "Devolviendo usuario";

	}

	@GetMapping(path = "/automaticHour/{date}/{hours}")
	public String getBestHour(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String date, @PathVariable String hours) {

		String startDate = date.concat("T00:00:00Z");
		String endDate = date.concat("T23:59:00Z");

		String startHour = "08:00:00";
		DateTime startWorkingHour = formatter.parseDateTime(startHour);

		String endHour = "15:00:00";
		DateTime endWorkingHour = formatter.parseDateTime(endHour);

		DateTime durationStimated = formatter.parseDateTime(hours);

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder()
				.url("https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin="
						+ startDate + "&timeMax=" + endDate + "&orderBy=startTime&singleEvents=True")
				.method("GET", null).addHeader("Authorization", "Bearer " + user.getAccessToken().getTokenValue())
				.build();

		JSONObject obj = null;
		try (Response response = client.newCall(request).execute()) {
			obj = new JSONObject(response.body().string());
		} catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}

		JSONArray arr = obj.getJSONArray("items");

		possibleHours(arr, startWorkingHour, endWorkingHour, durationStimated);

		return "BestHour";

	}

	@GetMapping(path = "/bestHourMoreCalendars/{email}/{date}/{hours}")
	public void bestHourMoreCalendars(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String email, @PathVariable String date, @PathVariable String hours) {

		String startDate = date.concat("T00:00:00Z");
		String endDate = date.concat("T23:59:00Z");

		String startHour = "08:00:00";
		DateTime startWorkingHour = formatter.parseDateTime(startHour);

		String endHour = "15:00:00";
		DateTime endWorkingHour = formatter.parseDateTime(endHour);

		DateTime durationStimated = formatter.parseDateTime(hours);

		String emailList[] = { "primary", email };

		JSONArray arr = new JSONArray();

		for (int i = 0; i < emailList.length; i++) {

			OkHttpClient client = new OkHttpClient().newBuilder().build();
			Request request = new Request.Builder()
					.url("https://www.googleapis.com/calendar/v3/calendars/" + emailList[i] + "/events?timeMin="
							+ startDate + "&timeMax=" + endDate + "&orderBy=startTime&singleEvents=True")
					.method("GET", null).addHeader("Authorization", "Bearer " + user.getAccessToken().getTokenValue())
					.build();

			JSONObject obj = null;
			try (Response response = client.newCall(request).execute()) {
				obj = new JSONObject(response.body().string());
			} catch (IOException e) {
				System.out.println("Error");
				e.printStackTrace();
			}

			if(i == 0)
				arr = obj.getJSONArray("items");
			else {
				
				JSONArray elements = obj.getJSONArray("items");
				
				for(int j = 0; j < elements.length(); j++) {
					arr.put(elements.get(j));
				}
			}
		}
		
		arr = sortJSON(arr);

		possibleHours(arr, startWorkingHour, endWorkingHour, durationStimated);
		
	}

	@PostMapping(path = "/addEvent")
	public String addUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {

		System.out.println("Creando evento");

		JSONObject jsonObject = new JSONObject(createEvent("hola", "adios"));

		jsonObject.getJSONObject("start").put("dateTime", "2020-07-24T01:00:00Z");
		jsonObject.getJSONObject("end").put("dateTime", "2020-07-24T04:00:00Z");

		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

		OkHttpClient client = new OkHttpClient().newBuilder().build();

		JSONObject obj = null;

		Request request = new Request.Builder()
				.url("https://www.googleapis.com/calendar/v3/calendars/julianmangut@gmail.com/events").post(body)
				.addHeader("Authorization", "Bearer " + user.getAccessToken().getTokenValue()).build();
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful())
				System.out.println("Correcto todo");

			obj = new JSONObject(response.body().string());

			System.out.println(obj.get("id"));

			return String.valueOf(response.isSuccessful());
		} catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}

		return "Añadiendo usuario";
	}

	@PutMapping
	public String modifyUser() {
		return "Modificando usuario";
	}

	@DeleteMapping(path = "/deleteEvent/{idEvent}")
	public String deleteUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String idEvent) {

		OkHttpClient client = new OkHttpClient().newBuilder().build();

		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create("", mediaType);

		Request request = new Request.Builder()
				.url("https://www.googleapis.com/calendar/v3/calendars/primary/events/" + idEvent)
				.method("DELETE", body).addHeader("Authorization", "Bearer " + user.getAccessToken().getTokenValue())
				.build();
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful())
				System.out.println(response.body().toString());
			System.out.println("Correcto todo");
			return String.valueOf(response.isSuccessful());
		} catch (IOException e) {

			System.out.println("Error");
			e.printStackTrace();
		}

		return "Eliminando usuario";
	}

}
