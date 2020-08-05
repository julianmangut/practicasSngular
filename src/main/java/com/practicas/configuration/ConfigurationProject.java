package com.practicas.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import com.practicas.services.EmailService;

@Configuration
@EnableScheduling
public class ConfigurationProject implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {
	
	@Autowired
	OAuth2AuthorizedClientService clientService;
	
	@Autowired
	EmailService emailService;
	
	@Override
	public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
		System.out.println("User Logged In");
		prueba();
	}

	@Bean
	@Lazy
	public EmailService prueba() {

		Authentication authentication =
			    SecurityContextHolder
			        .getContext()
			        .getAuthentication();

		OAuth2AuthenticationToken oauthToken =
			    (OAuth2AuthenticationToken) authentication;
		
		OAuth2AuthorizedClient client =
			    clientService.loadAuthorizedClient(
			            oauthToken.getAuthorizedClientRegistrationId(),
			            oauthToken.getName());
		
		System.out.println(client.getAccessToken().toString());
		
		emailService.user = client;

		return new EmailService();
	}

}
