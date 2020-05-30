package com.oleapp.colibriweb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oleapp.colibriweb.dao.impl.PostgresUserDAO;
import com.oleapp.colibriweb.model.User;
import com.oleapp.colibriweb.model.UserDataRegistry;
import com.oleapp.colibriweb.service.AppSettings;
import com.oleapp.colibriweb.service.crypt.CryptoUtils;
import com.oleapp.colibriweb.service.crypt.Password;

@Controller
public class RESTController {

	@RequestMapping(value = "/getuserdata", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String[] getUserData(@RequestBody Integer[] request) {
		try {
			int userId = request[0];

			User user = PostgresUserDAO.getInstance().getUser(userId);

			UserDataRegistry userDataRegistry = PostgresUserDAO.getInstance().getUserData(userId);

			ObjectMapper mapper = new ObjectMapper();

			String jsonUserDataRegistry = mapper.writeValueAsString(userDataRegistry);

			return new String[] { "200", CryptoUtils.encrypt(user.getAuthorizationToken(), jsonUserDataRegistry) };

		} catch (Exception e) {
			return new String[] { "400" }; // 400 HttpStatus.BAD_REQUEST
		}
	}

	@RequestMapping(value = "/senduserdata", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Integer[] sendUserData(@RequestBody String[] encryptedUserData) {
		try {
			int userId = Integer.parseInt(encryptedUserData[0]);

			User user = PostgresUserDAO.getInstance().getUser(userId);

			ObjectMapper mapper = new ObjectMapper();

			UserDataRegistry userDataRegistry = mapper
					.readValue(CryptoUtils.decrypt(user.getAuthorizationToken(), encryptedUserData[1]), UserDataRegistry.class);

			if (PostgresUserDAO.getInstance().updateUserData(userDataRegistry)) {
				return new Integer[] { 200 }; // 200 HttpStatus.OK
			} else {
				return new Integer[] { 400 }; // 400 HttpStatus.BAD_REQUEST
			}

		} catch (Exception e) {
			return new Integer[] { 400 }; // 400 HttpStatus.BAD_REQUEST
		}
	}

	@RequestMapping(value = "/checkuserauth", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Integer[] checkUserAuth(@RequestBody String[] encryptedUser) {
		try {
			int userId = Integer.parseInt(encryptedUser[0]);

			User user = PostgresUserDAO.getInstance().getUser(userId);

			ObjectMapper mapper = new ObjectMapper();

			mapper.readValue(CryptoUtils.decrypt(user.getAuthorizationToken(), encryptedUser[1]), User.class);

			return new Integer[] { 200 }; // 200 HttpStatus.OK

		} catch (Exception e) {
			return new Integer[] { 400 }; // 400 HttpStatus.BAD_REQUEST
		}
	}

	@RequestMapping(value = "/loginconfirm", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Integer[] loginConfirm(@RequestBody String[] encryptedUserNameToken) {
		try {
			String userName = CryptoUtils.decrypt(AppSettings.KEY, encryptedUserNameToken[0]);

			User user = PostgresUserDAO.getInstance().getUser(userName);

			String authToken = CryptoUtils.decrypt(user.getAuthorizationTokenBuffer(), encryptedUserNameToken[1]);

			user.setAuthorizationToken(authToken);
			user.setAuthorizationTokenBuffer(null);

			if (!PostgresUserDAO.getInstance().update(user)) {
				return new Integer[] { 400, -1 }; // 400 HttpStatus.BAD_REQUEST
			}

			return new Integer[] { 200, user.getId() }; // 200 HttpStatus.OK

		} catch (Exception e) {
			return new Integer[] { 400, -1 }; // 400 HttpStatus.BAD_REQUEST
		}
	}

	@RequestMapping(value = "/loginstart", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String[] loginStart(@RequestBody String[] encryptedUserName) {
		try {
			String userName = CryptoUtils.decrypt(AppSettings.KEY, encryptedUserName[0]);

			User user = PostgresUserDAO.getInstance().getUser(userName);

			if (user != null) {
				final String userPassHash = user.getUserPasswordHash();
				final int end = (int) (Math.random() * 10 + 50);
				final String authTokenBuffer = Password.hashPassword(user.getUserName() + System.currentTimeMillis())
						.substring(end - 16, end);

				user.setAuthorizationTokenBuffer(authTokenBuffer);

				if (!PostgresUserDAO.getInstance().update(user)) {
					return new String[] { "400" }; // 400 HttpStatus.BAD_REQUEST
				}

				final String authTokenBufferEncypted = CryptoUtils
						.encrypt(userPassHash.substring(userPassHash.length() - 16, userPassHash.length()), authTokenBuffer);

				final String salt = userPassHash.substring(0, 29);

				return new String[] { "200", salt, authTokenBufferEncypted }; // 200 HttpStatus.OK
			} else {
				return new String[] { "404" }; // 404 HttpStatus.NOT_FOUND
			}

		} catch (Exception e) {
			return new String[] { "400" }; // 400 HttpStatus.BAD_REQUEST
		}
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Integer[] register(@RequestBody String[] encryptedJsonUser) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			String jsonUser = CryptoUtils.decrypt(AppSettings.KEY, encryptedJsonUser[0]);
			User user = mapper.readValue(jsonUser, User.class);

			if (PostgresUserDAO.getInstance().insert(user))
				return new Integer[] { 201, user.getId() }; // 201 HttpStatus.CREATED
			else
				return new Integer[] { 409, -1 }; // 409 HttpStatus.CONFLICT

		} catch (Exception e) {
			return new Integer[] { 400, -1 }; // 400 HttpStatus.BAD_REQUEST
		}
	}

	@RequestMapping(value = "/checkconnection", method = RequestMethod.GET)
	public ResponseEntity<String> checkConnection() {
		return new ResponseEntity<String>(HttpStatus.OK); // 200
	}

}
