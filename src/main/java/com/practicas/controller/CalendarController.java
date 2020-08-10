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

	/**
	 * 
	 * 
	 * @param user :
	 */
	@GetMapping
	public void getEvents2DaysBack(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {
		calendarService.getEvents2DaysBack(user);
	}

	/**
	 * 
	 * 
	 * @param user  :
	 * @param date  :
	 * @param hours :
	 */
	@GetMapping(path = "/bestHourOwnCalendar/{date}/{hours}")
	public void getBestHour(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String date, @PathVariable String hours) {
		calendarService.getBestHour(user, date, hours);
	}

	/**
	 * 
	 * 
	 * @param user  :
	 * @param email :
	 * @param date  :
	 * @param hours :
	 */
	@GetMapping(path = "/bestHourMoreCalendars/email={email}/{date}/{hours}")
	public void bestHourMoreCalendars(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String email, @PathVariable String date, @PathVariable String hours) {
		calendarService.bestHourMoreCalendars(user, email, date, hours);
	}

	/**
	 * 
	 * 
	 * @param user  :
	 * @param place :
	 * @param date  :
	 * @param hours :
	 */
	@GetMapping(path = "/bestCombination/place={place}/{date}/{hours}")
	public void getBestCombination(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String place, @PathVariable String date, @PathVariable String hours) {
		calendarService.getBestCombination(user, date, hours, place);
	}

	/**
	 * 
	 * 
	 * @param user :
	 */
	@PostMapping(path = "/addEvent")
	public void addEvent(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {
		calendarService.addEvent(user);
	}

	/**
	 * TODO : Modify an Event.
	 */
	@PutMapping
	public void modifyEvent() {
		calendarService.modifyEvent();
	}

	/**
	 * 
	 * 
	 * @param user    :
	 * @param idEvent :
	 */
	@DeleteMapping(path = "/deleteEvent/{idEvent}")
	public void deleteEvent(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user,
			@PathVariable String idEvent) {
		calendarService.deleteEvent(user, idEvent);
	}

}
