package com.oleapp.colibriweb.dao.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

}
