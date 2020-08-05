package com.practicas.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	GEmailRequest gEmailRequest;

	public OAuth2AuthorizedClient user = null;

	@Scheduled(fixedRate = 60000)
	public void controlEmail() {

		if (user != null) {

			JSONObject response = gEmailRequest.getEmailsList(user);

			if (response != null) {
				System.out.println("Entre");
				JSONArray emailsList = response.getJSONArray("messages");

				for (int i = 0; i < emailsList.length(); i++) {

					JSONObject responseInfoEmail = gEmailRequest.getEmail(user,
							emailsList.getJSONObject(i).getString("id"));

					if (responseInfoEmail.getJSONArray("labelIds").get(0).equals("SENT")
							&& responseInfoEmail.getJSONObject("payload").getJSONArray("headers").getJSONObject(5)
									.get("value").equals("practicassngular@gmail.com"))
						System.out.println(responseInfoEmail.getJSONObject("payload").getJSONArray("headers")
								.getJSONObject(3).get("value"));

				}

			}
		}

	}

}
