package com.practicas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.practicas.services.CalendarService;

@RestController
@RequestMapping("/calendar")
public class CalendarController {
	
	@Autowired
	CalendarService calendarService;

	@GetMapping
	public void getUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {
		calendarService.getUser(user);
	}
	
	@GetMapping(path = "/bestHourOwnCalendar/{date}/{hours}")
	public void getBestHour(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String date, @PathVariable String hours) {
		calendarService.getBestHour(user, date, hours);
	}
	
	@GetMapping(path = "/bestHourMoreCalendars/email={email}/{date}/{hours}")
	public void bestHourMoreCalendars(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String email, @PathVariable String date, @PathVariable String hours) {
		calendarService.bestHourMoreCalendars(user, email, date, hours);
	}
	
	@GetMapping(path = "/bestCombination/place={place}/{date}/{hours}")
	public void getBestCombination(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String place, @PathVariable String date, @PathVariable String hours) {
		calendarService.getBestCombination(user, date, hours, place);
	}
	
	@PostMapping(path = "/addEvent")
	public void addUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {
		calendarService.addUser(user);
	}
	
	@PutMapping
	public void modifyUser() {
		calendarService.modifyUser();
	}
	
	@DeleteMapping(path = "/deleteEvent/{idEvent}")
	public void deleteUser(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String idEvent) {
		calendarService.deleteUser(user, idEvent);
	}
	
}
