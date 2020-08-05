package com.practicas.utils;

import org.springframework.security.core.Authentication;

public class AuthenticationUtils {

	private Authentication authentication;
	private static AuthenticationUtils authenticationUtils;

	public static AuthenticationUtils getAuthenticationUtils() {

		if (authenticationUtils == null) {

			authenticationUtils = new AuthenticationUtils();
		}
		return authenticationUtils;
	}

	private AuthenticationUtils() {

	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

}
