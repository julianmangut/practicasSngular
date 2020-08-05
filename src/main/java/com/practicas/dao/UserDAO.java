package com.practicas.dao;

import org.springframework.data.repository.CrudRepository;

import com.practicas.model.UserModel;

public interface UserDAO extends CrudRepository<UserModel, String> {

}
