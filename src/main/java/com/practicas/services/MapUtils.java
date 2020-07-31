package com.practicas.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class MapUtils {
	
	private static final Logger LOGGER = Logger.getLogger("practicas.utils.MapUtils");
	
	private static final String ERROR = "Error while processing request";
	private static final String CORRECT = "The request was process correctly";
	
	public JSONObject getInfoPlace(String location) {
		
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url("https://geocode.search.hereapi.com/v1/geocode?q=" + location + "&apiKey=gHk4apBrrcKjcUNdft0h7lgEEhOhZlxzd5he90aB42A")
				.method("GET", null).build();

		try (Response response = client.newCall(request).execute()) {
			return new JSONObject(response.body().string());
		} catch (IOException e) {
			LOGGER.log(Level.INFO, ERROR);
			e.printStackTrace();
		}
		return null;
		
	}
	
}
