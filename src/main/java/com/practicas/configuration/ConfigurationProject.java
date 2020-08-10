package com.practicas.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.practicas.dao.UserDAO;
import com.practicas.model.UserModel;
import com.practicas.services.EmailService;
import com.practicas.utils.AuthenticationUtils;

@Configuration
@EnableScheduling
public class ConfigurationProject implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

	@Autowired
	private UserDAO userDAO;

	/**
	 *
	 */
	@Override
	public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
		System.out.println("User Logged In");

		DefaultOAuth2User userDetails = (DefaultOAuth2User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		if (!userDAO.existsById(userDetails.getAttribute("email").toString())) {
			UserModel user = new UserModel();
			user.setEmail(userDetails.getAttribute("email").toString());
			userDAO.save(user);
		}

		prueba();
	}

	/**
	 * @return
	 */
	@Bean
	@Lazy
	public EmailService prueba() {
		AuthenticationUtils.getAuthenticationUtils()
				.setAuthentication(SecurityContextHolder.getContext().getAuthentication());
		return new EmailService();
	}

}
