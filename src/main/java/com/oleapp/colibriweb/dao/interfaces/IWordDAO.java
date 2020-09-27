package com.oleapp.colibriweb.dao.interfaces;

import java.util.List;

import com.oleapp.colibriweb.model.Word;

public interface IWordDAO {

	public boolean insert(Word word, int userId);

	public boolean update(Word word, int userId);

	public boolean delete(int wordId, int userId);

	public Word getWord(int wordId, int userId);

	public Word getWord(String word, int userId);

	public Word getNearestRepeatWord(int userId, boolean fromTodayWords, List<String> waitingWordList);

	public boolean insertWords(List<Word> wordsList, int userId);

	public boolean updateWords(List<Word> wordsList, int userId);

	public boolean deleteAllUserWords(int userId);

	public List<Word> getUserWords(int userId);

	public int getMaxWordId(int userId);

	public int incrementAndGetMaxWordId(int userId);

}
