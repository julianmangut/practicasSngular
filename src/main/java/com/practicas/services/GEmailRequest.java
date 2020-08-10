package com.practicas.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class GEmailRequest {

	private static final Logger LOGGER = Logger.getLogger("practicas.utils.CalendarUtils");

	private static final String URL = "https://www.googleapis.com/gmail/v1/users/";
	private static final String ERROR = "Error while processing request";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_TOKEN = "Bearer ";

	/**
	 * 
	 * 
	 * @param user :
	 * 
	 * @return :
	 */
	public JSONObject getEmailsList(OAuth2AuthorizedClient user) {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url(URL + "julianmangut@gmail.com/messages?maxResults=10")
				.method("GET", null)
				.addHeader(AUTHORIZATION_HEADER, BEARER_TOKEN + user.getAccessToken().getTokenValue()).build();

		try (Response response = client.newCall(request).execute()) {
			return new JSONObject(response.body().string());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, ERROR);
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param user      :
	 * @param idMessage :
	 * 
	 * @return :
	 */
	public JSONObject getEmail(OAuth2AuthorizedClient user, String idMessage) {

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url(URL + "me/messages/" + idMessage + "?format=metadata")
				.method("GET", null)
				.addHeader(AUTHORIZATION_HEADER, BEARER_TOKEN + user.getAccessToken().getTokenValue()).build();

		try (Response response = client.newCall(request).execute()) {
			return new JSONObject(response.body().string());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, ERROR);
		}
		return null;

	}

}
