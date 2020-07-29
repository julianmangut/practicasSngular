package com.practicas.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class CalendarUtils {

	private static final Logger LOGGER = Logger.getLogger("practicas.utils.CalendarUtils");

	private static final String URL = "https://www.googleapis.com/calendar/v3/calendars/";
	private static final String ERROR = "Error while processing request";
	private static final String CORRECT = "The request was process correctly";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_TOKEN = "Bearer";

	public JSONObject getEvents2DaysBack(OAuth2AuthorizedClient user, String date) {

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url(URL + "primary/events?maxResults=10&timeMin=" + date)
				.method("GET", null)
				.addHeader(AUTHORIZATION_HEADER, BEARER_TOKEN + user.getAccessToken().getTokenValue()).build();

		try (Response response = client.newCall(request).execute()) {
			return new JSONObject(response.body().string());
		} catch (IOException e) {
			LOGGER.log(Level.INFO, ERROR);
			e.printStackTrace();
		}
		return null;

	}

	public JSONObject getBestHourOwnCalendar(OAuth2AuthorizedClient user, String startDate, String endDate) {

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder()
				.url(URL + "primary/events?timeMin=" + startDate + "&timeMax=" + endDate
						+ "&orderBy=startTime&singleEvents=True")
				.method("GET", null)
				.addHeader(AUTHORIZATION_HEADER, BEARER_TOKEN + user.getAccessToken().getTokenValue()).build();

		try (Response response = client.newCall(request).execute()) {
			return new JSONObject(response.body().string());
		} catch (IOException e) {
			LOGGER.log(Level.INFO, ERROR);
			e.printStackTrace();
		}
		return null;

	}

	public JSONObject getBestHourMoreCalendars(OAuth2AuthorizedClient user, String email, String startDate,
			String endDate) {

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder()
				.url(URL + email + "/events?timeMin=" + startDate + "&timeMax=" + endDate
						+ "&orderBy=startTime&singleEvents=True")
				.method("GET", null)
				.addHeader(AUTHORIZATION_HEADER, BEARER_TOKEN + user.getAccessToken().getTokenValue()).build();

		try (Response response = client.newCall(request).execute()) {
			return new JSONObject(response.body().string());
		} catch (IOException e) {
			LOGGER.log(Level.INFO, ERROR);
			e.printStackTrace();
		}
		return null;

	}

	public JSONObject addEvent(OAuth2AuthorizedClient user, JSONObject jsonObject) {

		MediaType json = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(jsonObject.toString(), json);

		OkHttpClient client = new OkHttpClient().newBuilder().build();

		Request request = new Request.Builder().url(URL + "primary/events").post(body)
				.addHeader(AUTHORIZATION_HEADER, BEARER_TOKEN + user.getAccessToken().getTokenValue()).build();
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful())
				LOGGER.log(Level.INFO, CORRECT);

			return new JSONObject(response.body().string());
		} catch (IOException e) {
			LOGGER.log(Level.INFO, ERROR);
			e.printStackTrace();
		}
		return null;

	}

	public void deleteEvent(OAuth2AuthorizedClient user, String idEvent) {

		OkHttpClient client = new OkHttpClient().newBuilder().build();

		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create("", mediaType);

		Request request = new Request.Builder().url(URL + "primary/events/" + idEvent).method("DELETE", body)
				.addHeader(AUTHORIZATION_HEADER, BEARER_TOKEN + user.getAccessToken().getTokenValue()).build();
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful())
				LOGGER.log(Level.INFO, "{0}", response.body());
			LOGGER.log(Level.INFO, CORRECT);
		} catch (IOException e) {

			LOGGER.log(Level.INFO, ERROR);
			e.printStackTrace();
		}
	}

}
