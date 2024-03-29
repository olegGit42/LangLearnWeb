package com.oleapp.colibriweb.dao.impl;

import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.USER_DATA_TABLE;
import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.ud_max_word_id;
import static com.oleapp.colibriweb.dao.impl.PostgresUserDAO.ud_user_id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oleapp.colibriweb.controller.WordController;
import com.oleapp.colibriweb.dao.interfaces.ADataSource;
import com.oleapp.colibriweb.dao.interfaces.IWordDAO;
import com.oleapp.colibriweb.model.Word;
import com.oleapp.colibriweb.service.AppSettings;

@Component
public class PostgresWordDAO extends ADataSource implements IWordDAO {

	public static final String WORD_TABLE = "public.word";
	public static final String wt_user_id = "user_id";
	public static final String wt_id = "id";
	public static final String wt_word = "word";
	public static final String wt_translate = "translate";
	public static final String wt_date_repeat = "date_repeat";
	public static final String wt_date_create = "date_create";
	public static final String wt_box = "box";
	public static final String wt_repeat_count = "repeat_count";

	public static final String TIME_DELTA = "public.time_delta";
	public static final String td_box = "box";
	public static final String td_time_delta = "time_delta";
	// public static final String td_note = "note";

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
			return jdbcTemplate.query(getSelectWordString(wt_word, "'" + word.replaceAll("'", "''") + "'", userId), wordRowMapper)
					.get(0);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Word getNearestRepeatWord(int userId, boolean fromTodayWords, long timezoneOffset, List<String> waitingWordList) {
		try {
			long tomorrowDate = 1;
			if (fromTodayWords) {
				Date date = new Date();
				date.setTime(System.currentTimeMillis() + WordController.day_ms + timezoneOffset);
				date = WordController.dateFormat.parse(WordController.dateFormat.format(date));

				tomorrowDate = date.getTime() - timezoneOffset;
			}

			String waitingWords;

			if (waitingWordList == null || waitingWordList.isEmpty()) {
				waitingWords = "''";
			} else {
				String firstWaitingWord = waitingWordList.get(0);
				waitingWords = "'" + firstWaitingWord.replaceAll("'", "''") + "'";
				waitingWordList.remove(0);
				for (String word : waitingWordList) {
					waitingWords += ", '" + word.replaceAll("'", "''") + "'";
				}
				waitingWordList.add(firstWaitingWord);
			}

			final String w = "www", d = "ddd";
			final String min_today_repeat_box = "(select " + "min(" + w + "." + wt_box + ") from " + WORD_TABLE + " " + w + ", "
					+ TIME_DELTA + " " + d + " where " + w + "." + wt_user_id + " = " + userId + " and " + w + "." + wt_box
					+ " = " + d + "." + td_box + " and " + w + "." + wt_date_repeat + " + " + d + "." + td_time_delta + " < "
					+ tomorrowDate + " and " + w + "." + wt_word + " not in (" + waitingWords + "))";

			final String sql = "SELECT " + wt_user_id + ", " + wt_id + ", " + wt_word + ", " + wt_translate + ", "
					+ wt_date_repeat + ", " + wt_date_create + ", " + wt_box + ", " + wt_repeat_count + " FROM " + WORD_TABLE
					+ " where " + wt_user_id + " = " + userId + " and " + wt_id + " = " + "(select min(w." + wt_id + ") from "
					+ WORD_TABLE + " w, " + TIME_DELTA + " d where w." + wt_user_id + " = " + userId + " and w." + wt_box
					+ " = d." + td_box + " and w." + wt_date_repeat + " + d." + td_time_delta + " = " + "(select min(ww."
					+ wt_date_repeat + " + dd." + td_time_delta + ") from " + WORD_TABLE + " ww, " + TIME_DELTA + " dd where ww."
					+ wt_user_id + " = " + userId + " and ww." + wt_box + " = dd." + td_box + " and (1 = " + tomorrowDate
					+ " or (ww." + wt_date_repeat + " + dd." + td_time_delta + " < " + tomorrowDate + " and ww." + wt_box + " = "
					+ min_today_repeat_box + ")) and ww." + wt_word + " not in (" + waitingWords + ")))";

			return jdbcTemplate.query(sql, wordRowMapper).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public String getSelectWordString(final String column, final String value, int userId) {
		return "SELECT " + wt_user_id + ", " + wt_id + ", " + wt_word + ", " + wt_translate + ", " + wt_date_repeat + ", "
				+ wt_date_create + ", " + wt_box + ", " + wt_repeat_count + " FROM " + WORD_TABLE + " where " + wt_user_id + " = "
				+ userId + " and " + column + " = " + value + " ORDER BY " + wt_word;
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
	public List<Word> getForgettableWords(int userId, boolean isAll) {
		try {
			return jdbcTemplate.query(getForgettableWordSQL(userId, isAll), wordRowMapper);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public String getForgettableWordSQL(int userId, boolean isAll) {

		String condition = " AND ((" + wt_repeat_count + " > 3 AND " + wt_box + " < 4) OR (" + wt_repeat_count + " > 10 AND "
				+ wt_box + " < 5) OR (" + wt_repeat_count + " > 20 AND " + wt_box + " < 6))";

		return "SELECT " + wt_user_id + ", " + wt_id + ", " + wt_word + ", " + wt_translate + ", " + wt_date_repeat + ", "
				+ wt_date_create + ", " + wt_box + ", " + wt_repeat_count + " FROM " + WORD_TABLE + " where " + wt_user_id + " = "
				+ userId + (isAll ? "" : condition) + " ORDER BY " + wt_repeat_count + " DESC, " + wt_word;
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

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int startRepeatPlannedWords(int userId, int count) {
		try {
			List<Word> plannedWordsList = getPlannedWords(userId, count);

			if (plannedWordsList == null || plannedWordsList.size() == 0) {
				return 0;
			}

			for (Word word : plannedWordsList) {
				word.startRepeatPlanned();
				this.update(word, userId);
			}

			return plannedWordsList.size();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Word> getPlannedWords(int userId, int count) {
		try {
			return jdbcTemplate.query(getPlannedWordSql(userId, count, false), wordRowMapper);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPlannedWordSql(int userId, int count, boolean isStarted) {
		return "SELECT " + wt_user_id + ", " + wt_id + ", " + wt_word + ", " + wt_translate + ", " + wt_date_repeat + ", "
				+ wt_date_create + ", " + wt_box + ", " + wt_repeat_count + " FROM " + WORD_TABLE + " where " + wt_user_id + " = "
				+ userId + " and " + wt_date_repeat + (isStarted ? " <> " : " = ") + Word.PLANNED_TIME
				+ (isStarted ? " and " + wt_repeat_count + " = (-1)" : "") + " ORDER BY " + wt_id + (isStarted ? " DESC" : "")
				+ " limit " + count;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Word> getStartedPlannedWords(int userId, int count) {
		try {
			return jdbcTemplate.query(getPlannedWordSql(userId, count, true), wordRowMapper);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int rollbackStartedPlannedWords(int userId, int count) {
		try {
			List<Word> plannedWordsList = getStartedPlannedWords(userId, count);

			if (plannedWordsList == null || plannedWordsList.size() == 0) {
				return 0;
			}

			for (Word word : plannedWordsList) {
				word.becomePlanned();
				this.update(word, userId);
			}

			return plannedWordsList.size();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Word> searchUserWordsLike(String word, int userId) {
		try {
			return jdbcTemplate.query(searchWordString(word.trim().toUpperCase().replace("'", "''"), userId), wordRowMapper);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public String searchWordString(final String value, int userId) {
		return "SELECT " + wt_user_id + ", " + wt_id + ", " + wt_word + ", " + wt_translate + ", " + wt_date_repeat + ", "
				+ wt_date_create + ", " + wt_box + ", " + wt_repeat_count + " FROM " + WORD_TABLE + " where " + wt_user_id + " = "
				+ userId + " and (UPPER(" + wt_word + ") like '%" + value + "%' or UPPER(" + wt_translate + ") like '%" + value
				+ "%') ORDER BY " + wt_word;
	}

}
