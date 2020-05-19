package com.oleapp.colibriweb.dao.impl;

import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.USER_DATA_TABLE;
import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.ud_max_tag_id;
import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.ud_user_id;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oleapp.colibriweb.dao.interfaces.ADataSource;
import com.oleapp.colibriweb.dao.interfaces.ITagDAO;
import com.oleapp.colibriweb.model.TagRegistry;
import com.oleapp.colibriweb.service.AppSettings;

@Component
public class PostgresTagDAO extends ADataSource implements ITagDAO {

	private static final String TAG_TABLE = "public.tag";
	private static final String tt_user_id = "user_id";
	private static final String tt_id = "id";
	private static final String tt_tag = "tag";

	private static final String WORD_TAG_TABLE = "public.word_tag";
	private static final String wtt_user_id = "user_id";
	private static final String wtt_word_id = "word_id";
	private static final String wtt_tag_id = "tag_id";

	public static PostgresTagDAO getInstance() {
		return AppSettings.context.getBean("postgresTagDAO", PostgresTagDAO.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean insertTags(TagRegistry tagRegistry, int userId) {
		try {
			tagRegistry.getIdTagMap().forEach((id, tag) -> {
				String insertTag = "INSERT INTO " + TAG_TABLE + "(" + tt_user_id + ", " + tt_id + ", " + tt_tag + ") VALUES ("
						+ userId + ", " + id + ", '" + tag + "')";
				jdbcTemplate.update(insertTag);
			});

			String updateMaxTagId = "UPDATE " + USER_DATA_TABLE + " SET " + ud_max_tag_id + " = " + tagRegistry.getMaxID().get()
					+ " WHERE " + ud_user_id + " = " + userId;
			jdbcTemplate.update(updateMaxTagId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean updateTags(TagRegistry tagRegistry, int userId) {
		deleteTags(userId);
		return insertTags(tagRegistry, userId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean deleteTags(int userId) {
		try {
			String deleteTags = "DELETE from " + TAG_TABLE + " WHERE " + tt_user_id + " = " + userId;
			jdbcTemplate.update(deleteTags);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public TagRegistry getTags(int userId) {
		TagRegistry tagRegistry = TagRegistry.getNewInstance();
		try {
			String selectTags = "SELECT " + tt_id + ", " + tt_tag + " FROM " + TAG_TABLE + " WHERE " + tt_user_id + " = "
					+ userId;
			jdbcTemplate.query(selectTags, new ResultSetExtractor<Object>() {
				@Override
				public String extractData(ResultSet rs) throws SQLException {
					while (rs.next()) {
						tagRegistry.getIdTagMap().put(rs.getInt(tt_id), rs.getString(tt_tag));
					}
					return null;
				};

			});

			String selectMaxTagId = "SELECT " + ud_max_tag_id + " FROM " + USER_DATA_TABLE + " WHERE " + ud_user_id + " = "
					+ userId;
			tagRegistry.getMaxID().set(jdbcTemplate.query(selectMaxTagId, (rs, i) -> rs.getInt(ud_max_tag_id)).get(0));

		} catch (Exception e) {
			return null;
		}
		return tagRegistry;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean insertWordTagIdSet(Set<Integer> tagSet, int userId, int wordId) {
		try {
			tagSet.forEach(id -> {
				String insertWordTag = "INSERT INTO " + WORD_TAG_TABLE + "(" + wtt_user_id + ", " + wtt_tag_id + ", "
						+ wtt_word_id + ") VALUES (" + userId + ", " + id + ", " + wordId + ")";
				jdbcTemplate.update(insertWordTag);
			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean updateWordTagIdSet(Set<Integer> tagSet, int userId, int wordId) {
		deleteWordTagIdSet(userId, wordId);
		return insertWordTagIdSet(tagSet, userId, wordId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean deleteWordTagIdSet(int userId, int wordId) {
		try {
			String deleteWordTags = "DELETE from " + WORD_TAG_TABLE + " WHERE " + wtt_user_id + " = " + userId + " and "
					+ wtt_word_id + " = " + wordId;
			jdbcTemplate.update(deleteWordTags);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean deleteAllWordTagIdSet(int userId) {
		try {
			String deleteWordTags = "DELETE from " + WORD_TAG_TABLE + " WHERE " + wtt_user_id + " = " + userId;
			jdbcTemplate.update(deleteWordTags);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Set<Integer> getWordTagIdSet(int userId, int wordId) {
		try {
			String selectTagSet = "SELECT " + wtt_tag_id + " FROM " + WORD_TAG_TABLE + " WHERE " + wtt_user_id + " = " + userId
					+ " and " + wtt_word_id + " = " + wordId;

			return jdbcTemplate.query(selectTagSet, (rs, i) -> rs.getInt(wtt_tag_id)).stream().collect(Collectors.toSet());

		} catch (Exception e) {
			return new HashSet<>();
		}
	}

}
