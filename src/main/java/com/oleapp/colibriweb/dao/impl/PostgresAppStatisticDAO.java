package com.oleapp.colibriweb.dao.impl;

import static com.oleapp.colibriweb.dao.impl.PostgresWordDAO.TIME_DELTA;
import static com.oleapp.colibriweb.dao.impl.PostgresWordDAO.WORD_TABLE;
import static com.oleapp.colibriweb.dao.impl.PostgresWordDAO.td_box;
import static com.oleapp.colibriweb.dao.impl.PostgresWordDAO.td_time_delta;
import static com.oleapp.colibriweb.dao.impl.PostgresWordDAO.wt_box;
import static com.oleapp.colibriweb.dao.impl.PostgresWordDAO.wt_date_repeat;
import static com.oleapp.colibriweb.dao.impl.PostgresWordDAO.wt_user_id;

import java.text.ParseException;
import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oleapp.colibriweb.controller.WordController;
import com.oleapp.colibriweb.dao.interfaces.ADataSource;
import com.oleapp.colibriweb.dao.interfaces.IAppStatisticDAO;
import com.oleapp.colibriweb.service.AppSettings;

@Component
public class PostgresAppStatisticDAO extends ADataSource implements IAppStatisticDAO {

	private static final String DOWNLOADS_TABLE = "downloads";

	public static PostgresAppStatisticDAO getInstance() {
		return AppSettings.context.getBean("postgresAppStatisticDAO", PostgresAppStatisticDAO.class);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void incrementDownloadCount() {
		String sql = "update " + DOWNLOADS_TABLE + " set count = nextval('downloads_count')";
		jdbcTemplate.update(sql);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public int getDownloadCount() {
		String sql = "select COALESCE(max(count), 0) as count from " + DOWNLOADS_TABLE;
		return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("count")).get(0);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public int getTodayWordsRepeatCount(int userId) {
		try {
			Date date = new Date();
			date.setTime(System.currentTimeMillis() + WordController.day_ms);
			date = WordController.dateFormat.parse(WordController.dateFormat.format(date));
			long tomorrowDate = date.getTime();

			String sql = "select count(w.*) as count from " + WORD_TABLE + " w, " + TIME_DELTA + " d where w." + wt_box + " = d."
					+ td_box + " and  w." + wt_user_id + " = " + userId + " and w." + wt_date_repeat + " + d." + td_time_delta
					+ " < " + tomorrowDate;

			return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("count")).get(0);
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

}
