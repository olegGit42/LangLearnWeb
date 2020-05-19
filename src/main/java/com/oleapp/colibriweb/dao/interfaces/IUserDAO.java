package com.oleapp.colibriweb.dao.interfaces;

import com.oleapp.colibriweb.model.User;
import com.oleapp.colibriweb.model.UserDataRegistry;

public interface IUserDAO {

	public boolean insert(User user);

	public boolean update(User user);

	public boolean delete(int userId);

	public User getUser(String name);

	public User getUser(int id);

	public boolean insertUserData(UserDataRegistry userDataRegistry);

	public boolean updateUserData(UserDataRegistry userDataRegistry);

	public boolean deleteUserData(int userId);

	public UserDataRegistry getUserData(int userId);

}
