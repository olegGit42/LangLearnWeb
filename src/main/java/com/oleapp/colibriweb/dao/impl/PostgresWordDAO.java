package com.oleapp.colibriweb.dao.impl;

import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.USER_DATA_TABLE;
import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.ud_max_word_id;
import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.ud_user_id;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oleapp.colibriweb.dao.interfaces.ADataSource;
import com.oleapp.colibriweb.dao.interfaces.IWordDAO;
import com.oleapp.colibriweb.model.Word;
import com.oleapp.colibriweb.service.AppSettings;

@Component
public class PostgresWordDAO extends ADataSource implements IWordDAO {

	private static final String WORD_TABLE = "public.word";
	private static final String wt_user_id = "user_id";
	private static final String wt_id = "id";
	private static final String wt_word = "word";
	private static final String wt_translate = "translate";
	private static final String wt_date_repeat = "date_repeat";
	private static final String wt_date_create = "date_create";
	private static final String wt_box = "box";
	private static final String wt_repeat_count = "repeat_count";

	private static final RowMapper<Word> wordRowMapper = (rs, i) -> {
		Word word = Word.getNewInstance();
		word.setId(rs.getInt(wt_id));
		word.setWord(rs.getString(wt_word));
		word.setTranslate(rs.getString(wt_translate));
		word.setRegTime(rs.getLong(wt_date_repeat));
		word.setCreationTime(rs.getLong(wt_date_create));
		word.setBox(rs.getInt(wt_box));
		word.setRepeateIndicator(rs.getInt(wt_repeat_count));
		word.setTagIdSet(PostgresTagDAO.getInstance().getWordTagIdSet(rs.getInt(wt_user_id), word.getId()));
		return word;
	};

	public static PostgresWordDAO getInstance() {
		return AppSettings.context.getBean("postgresWordDAO", PostgresWordDAO.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean insert(Word word, int userId) {
		String sqlInsertWord = "insert into " + WORD_TABLE + "(" + wt_user_id + ", " + wt_id + ", " + wt_word + ", "
				+ wt_translate + ", " + wt_date_repeat + ", " + wt_date_create + ", " + wt_box + ", " + wt_repeat_count
				+ ") VALUES (:" + wt_user_id + ", :" + wt_id + ", :" + wt_word + ", :" + wt_translate + ", :" + wt_date_repeat
				+ ", :" + wt_date_create + ", :" + wt_box + ", :" + wt_repeat_count + ")";

		try {
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue(wt_user_id, userId);
			params.addValue(wt_id, word.getId());
			params.addValue(wt_word, word.getWord());
			params.addValue(wt_translate, word.getTranslate());
			params.addValue(wt_date_repeat, word.getRegTime());
			params.addValue(wt_date_create, word.getCreationTime());
			params.addValue(wt_box, word.getBox());
			params.addValue(wt_repeat_count, word.getRepeateIndicator());

			namedParameterJdbcTemplate.update(sqlInsertWord, params);
			PostgresTagDAO.getInstance().insertWordTagIdSet(word.getTagIdSet(), userId, word.getId());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean update(Word word, int userId) {
		delete(word.getId(), userId);
		return insert(word, userId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean delete(int wordId, int userId) {
		try {
			String deleteWord = "DELETE from " + WORD_TABLE + " WHERE " + wt_user_id + " = " + userId + " and " + wt_id + " = "
					+ wordId;
			jdbcTemplate.update(deleteWord);
			PostgresTagDAO.getInstance().deleteWordTagIdSet(userId, wordId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Word getWord(int wordId, int userId) {
		try {
			return jdbcTemplate.query(getSelectWordString(wt_id, String.valueOf(wordId), userId), wordRowMapper).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Word getWord(String word, int userId) {
		try {
			return jdbcTemplate.query(getSelectWordString(wt_word, "'" + word + "'", userId), wordRowMapper).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public String getSelectWordString(final String column, final String value, int userId) {
		return "SELECT " + wt_user_id + ", " + wt_id + ", " + wt_word + ", " + wt_translate + ", " + wt_date_repeat + ", "
				+ wt_date_create + ", " + wt_box + ", " + wt_repeat_count + " FROM " + WORD_TABLE + " where " + column + " = "
				+ value + " and " + wt_user_id + " = " + userId;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean insertWords(List<Word> wordsList, int userId) {
		try {
			wordsList.forEach(w -> insert(w, userId));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean updateWords(List<Word> wordsList, int userId) {
		try {
			deleteAllUserWords(userId);
			insertWords(wordsList, userId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean deleteAllUserWords(int userId) {
		try {
			String deleteWords = "DELETE from " + WORD_TABLE + " WHERE " + wt_user_id + " = " + userId;
			jdbcTemplate.update(deleteWords);
			PostgresTagDAO.getInstance().deleteAllWordTagIdSet(userId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Word> getUserWords(int userId) {
		try {
			return jdbcTemplate.query(getSelectWordString("1", "1", userId), wordRowMapper);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int getMaxWordId(int userId) {
		try {
			String selectMaxWordId = "SELECT " + ud_max_word_id + " FROM " + USER_DATA_TABLE + " WHERE " + ud_user_id + " = "
					+ userId;

			return jdbcTemplate.query(selectMaxWordId, (rs, i) -> rs.getInt(ud_max_word_id)).get(0);

		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int incrementAndGetMaxWordId(int userId) {
		int newId = getMaxWordId(userId) + 1;
		try {
			String updateMaxWordId = "UPDATE " + USER_DATA_TABLE + " SET " + ud_max_word_id + " = " + newId + " WHERE "
					+ ud_user_id + " = " + userId;
			jdbcTemplate.update(updateMaxWordId);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return newId;
	}

}
