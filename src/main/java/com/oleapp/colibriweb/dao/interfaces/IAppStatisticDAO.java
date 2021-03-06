package com.oleapp.colibriweb.dao.interfaces;

public interface IAppStatisticDAO {

	public void incrementDownloadCount();

	public int getDownloadCount();

	public int getTodayWordsRepeatCount(int userId, long timezoneOffset);

	public int getAllWordsCount(int userId, boolean withPlanned);

}
