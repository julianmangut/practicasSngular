package com.practicas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.practicas.model.EmailModel;

public interface EmailListDAO extends CrudRepository<EmailModel, String> {

	@Query(value="SELECT id FROM emaillist WHERE email = 'julianmangut@gmail.com'",nativeQuery=true)
	List<String> readListId();
	
}
