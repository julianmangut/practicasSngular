package com.practicas.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import com.practicas.utils.AuthenticationUtils;

@Service
public class EmailService {

	@Autowired
	GEmailRequest gEmailRequest;

	@Autowired
	CalendarService calendarService;

	@Autowired
	OAuth2AuthorizedClientService clientService;

	private OAuth2AuthorizedClient authentication() {
		Authentication authentication = AuthenticationUtils.getAuthenticationUtils().getAuthentication();

		if (authentication != null) {
			OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

			return clientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(),
					oauthToken.getName());
		}

		return null;
	}

	@Scheduled(fixedDelay = 60000)
	public void controlEmail() {
		OAuth2AuthorizedClient user = authentication();

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
									.get("value").equals("practicassngular@gmail.com")) {
						String affair = responseInfoEmail.getJSONObject("payload").getJSONArray("headers")
								.getJSONObject(3).get("value").toString().trim();

						if (affair.startsWith("Calendar:")) {
				
							String cleanAffair = affair.substring(affair.indexOf(":") + 1);
							
							String[] parameters = cleanAffair.split(",");

							if (parameters[0].contains("-"))
								calendarService.getBestHour(user, parameters[0], parameters[1]);
							else
								calendarService.getBestCombination(user, parameters[1], parameters[2], parameters[0]);
						}
					}

				}

			}
		}

	}

}
