package com.practicas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.practicas.model.EmailModel;

public interface EmailListDAO extends CrudRepository<EmailModel, String> {

	/**
	 * Query for obtain all the id of the events on the DB that have the email of the User.
	 * 
	 * @return > List of id of the events that are on the DB.
	 */
	@Query(value="SELECT id FROM emaillist WHERE email = 'julianmangut@gmail.com'",nativeQuery=true)
	List<String> readListId();
	
}
