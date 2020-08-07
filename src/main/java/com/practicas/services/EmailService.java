package com.practicas.services;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import com.practicas.dao.EmailListDAO;
import com.practicas.model.EmailModel;
import com.practicas.model.UserModel;
import com.practicas.utils.AuthenticationUtils;

@Service
public class EmailService {

	@Autowired
	private GEmailRequest gEmailRequest;

	@Autowired
	private CalendarService calendarService;

	@Autowired
	private OAuth2AuthorizedClientService clientService;

	@Autowired
	private EmailListDAO emailListDAO;

	private OAuth2AuthorizedClient authentication() {
		Authentication authentication = AuthenticationUtils.getAuthenticationUtils().getAuthentication();

		if (authentication != null) {
			OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

			return clientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(),
					oauthToken.getName());
		}

		return null;
	}

	private void createSaveEmail(JSONObject emailInformation, UserModel userModel) {
		EmailModel email = new EmailModel();
		email.setId(emailInformation.getString("id"));
		email.setEmail(userModel);
		emailListDAO.save(email);
	}

	private JSONArray recursiveReading(List<String> listId, JSONArray emailList) {
		for (int i = emailList.length() / 2; i < emailList.length(); i++) {
			if (listId.contains(emailList.getJSONObject(i).getString("id"))) {

				int emailLength = emailList.length();

				for (int j = i; j < emailLength; j++) {
					emailList.remove(j);
				}
				if (i == emailLength / 2) {
					recursiveReading(listId, emailList);
				}
			}
		}
		return emailList;
	}

	private JSONArray cleanList(JSONArray emailList) {

		List<String> listId = emailListDAO.readListId();

		if (listId.isEmpty())
			return emailList;

		return recursiveReading(listId, emailList);

	}

	@Scheduled(fixedDelay = 60000)
	public void controlEmail() {
		OAuth2AuthorizedClient user = authentication();

		UserModel userModel = new UserModel();
		userModel.setEmail("julianmangut@gmail.com");

		if (user != null) {

			JSONObject response = gEmailRequest.getEmailsList(user);

			if (response != null) {
				System.out.println("Entre");
				JSONArray emailsList = response.getJSONArray("messages");

				cleanList(emailsList);

				if (emailsList.length() > 0) {
					for (int i = 0; i < emailsList.length(); i++) {

						createSaveEmail(emailsList.getJSONObject(i), userModel);

						JSONObject responseInfoEmail = gEmailRequest.getEmail(user,
								emailsList.getJSONObject(i).getString("id"));

						if (responseInfoEmail.getJSONArray("labelIds").get(0).equals("SENT")
								&& responseInfoEmail.getJSONObject("payload").getJSONArray("headers").getJSONObject(5)
										.get("value").equals("practicassngular@gmail.com")) {
							String affair = responseInfoEmail.getJSONObject("payload").getJSONArray("headers")
									.getJSONObject(3).get("value").toString().trim();

							if (affair.replaceAll("\\s","").startsWith("Calendar:")) {

								String cleanAffair = affair.substring(affair.indexOf(":") + 1);

								String[] parameters = cleanAffair.split(",");

								if (parameters[0].contains("-"))
									calendarService.getBestHour(user, parameters[0].trim(), parameters[1].trim());
								else
									calendarService.getBestCombination(user, parameters[1].trim(), parameters[2].trim(),
											parameters[0].trim());
							}
						}
					}
				}
			}
		}

	}

}
