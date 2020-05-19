package com.oleapp.colibriweb.dao.impl;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oleapp.colibriweb.dao.interfaces.ADataSource;
import com.oleapp.colibriweb.dao.interfaces.IUserDAO;
import com.oleapp.colibriweb.model.User;
import com.oleapp.colibriweb.model.UserDataRegistry;
import com.oleapp.colibriweb.service.AppSettings;

@Component
public class PostgresUserDAO extends ADataSource implements IUserDAO {

	private static final String USER_TABLE = "public.user";
	private static final String ut_id = "id";
	private static final String ut_name = "name";
	private static final String ut_password_hash = "password_hash";
	private static final String ut_auth_token = "auth_token";
	private static final String ut_auth_token_buffer = "auth_token_buffer";

	public static final String USER_DATA_TABLE = "public.user_data";
	public static final String ud_user_id = "user_id";
	public static final String ud_max_word_id = "max_word_id";
	public static final String ud_max_tag_id = "max_tag_id";

	private static final RowMapper<User> userRowMapper = (resultSet, rowNum) -> {
		User user = User.getNewInstance();
		user.setId(resultSet.getInt(ut_id));
		user.setUserName(resultSet.getString(ut_name));
		user.setUserPasswordHash(resultSet.getString(ut_password_hash));
		user.setAuthorizationToken(resultSet.getString(ut_auth_token));
		user.setAuthorizationTokenBuffer(resultSet.getString(ut_auth_token_buffer));
		user.setAutoEnter(false);
		return user;
	};

	public static PostgresUserDAO getInstance() {
		return AppSettings.context.getBean("postgresUserDAO", PostgresUserDAO.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean insert(User user) {
		String sqlInsertUser = "insert into " + USER_TABLE + "(" + ut_name + ", " + ut_password_hash + ") VALUES (:" + ut_name
				+ ", :" + ut_password_hash + ")";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(ut_name, user.getUserName());
		params.addValue(ut_password_hash, user.getUserPasswordHash());

		KeyHolder keyHolder = new GeneratedKeyHolder();

		try {
			namedParameterJdbcTemplate.update(sqlInsertUser, params, keyHolder, new String[] { "id" });
			String sqlInsertUserData = "insert into " + USER_DATA_TABLE + "(" + ud_user_id + ", " + ud_max_word_id + ", "
					+ ud_max_tag_id + ") VALUES (" + keyHolder.getKey().intValue() + ", 0, 0)";
			jdbcTemplate.update(sqlInsertUserData);
			user.setId(keyHolder.getKey().intValue());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean update(User user) {
		String sqlInsertUser = "update " + USER_TABLE + " set " + ut_name + " = :" + ut_name + ", " + ut_password_hash + " = :"
				+ ut_password_hash + ", " + ut_auth_token + " = :" + ut_auth_token + ", " + ut_auth_token_buffer + " = :"
				+ ut_auth_token_buffer + "  where " + ut_id + " = :" + ut_id;

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(ut_id, user.getId());
		params.addValue(ut_name, user.getUserName());
		params.addValue(ut_password_hash, user.getUserPasswordHash());
		params.addValue(ut_auth_token, user.getAuthorizationToken());
		params.addValue(ut_auth_token_buffer, user.getAuthorizationTokenBuffer());

		try {
			namedParameterJdbcTemplate.update(sqlInsertUser, params);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean delete(int userId) {
		String sqlDeleteUser = "DELETE FROM " + USER_TABLE + " WHERE " + ut_id + " = " + userId;

		try {
			jdbcTemplate.update(sqlDeleteUser);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public User getUser(String name) {
		try {
			return jdbcTemplate.query(getSelectUserString(ut_name, "'" + name + "'"), userRowMapper).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public User getUser(int id) {
		try {
			return jdbcTemplate.query(getSelectUserString(ut_id, String.valueOf(id)), userRowMapper).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public String getSelectUserString(final String column, final String value) {
		return "SELECT " + ut_id + ", " + ut_name + ", " + ut_password_hash + ", " + ut_auth_token + ", " + ut_auth_token_buffer
				+ " FROM " + USER_TABLE + " where " + column + " = " + value;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean insertUserData(UserDataRegistry userDataRegistry) {
		try {
			int userId = userDataRegistry.getUserId();

			String sqlInsertUserData = "insert into " + USER_DATA_TABLE + "(" + ud_user_id + ", " + ud_max_word_id + ", "
					+ ud_max_tag_id + ") VALUES (" + userId + ", " + userDataRegistry.getMaxWordID() + ", 0)";
			jdbcTemplate.update(sqlInsertUserData);

			PostgresWordDAO.getInstance().insertWords(userDataRegistry.getAllUserWordsList(), userId);
			PostgresTagDAO.getInstance().insertTags(userDataRegistry.getTagRegistry(), userId);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean updateUserData(UserDataRegistry userDataRegistry) {
		deleteUserData(userDataRegistry.getUserId());
		return insertUserData(userDataRegistry);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean deleteUserData(int userId) {
		try {
			String deleteUserData = "DELETE from " + USER_DATA_TABLE + " WHERE " + ud_user_id + " = " + userId;
			jdbcTemplate.update(deleteUserData);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserDataRegistry getUserData(int userId) {
		try {
			UserDataRegistry userDataRegistry = UserDataRegistry.getNewInstance();

			userDataRegistry.setUserId(userId);
			userDataRegistry.setTagRegistry(PostgresTagDAO.getInstance().getTags(userId));
			userDataRegistry.setAllUserWordsList(PostgresWordDAO.getInstance().getUserWords(userId));
			userDataRegistry.setMaxWordID(PostgresWordDAO.getInstance().getMaxWordId(userId));

			return userDataRegistry;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
