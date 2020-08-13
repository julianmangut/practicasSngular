package com.practicas.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

	public void setAuthentication() {
		authentication = SecurityContextHolder.getContext().getAuthentication();
	}

}
