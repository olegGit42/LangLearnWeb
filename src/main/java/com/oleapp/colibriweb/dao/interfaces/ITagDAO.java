package com.oleapp.colibriweb.dao.interfaces;

import java.util.Set;

import com.oleapp.colibriweb.model.TagRegistry;

public interface ITagDAO {

	public boolean insertTags(TagRegistry tagRegistry, int userId);

	public boolean updateTags(TagRegistry tagRegistry, int userId);

	public boolean deleteTags(int userId);

	public TagRegistry getTags(int userId);

	public boolean insertWordTagIdSet(Set<Integer> tagSet, int userId, int wordId);

	public boolean updateWordTagIdSet(Set<Integer> tagSet, int userId, int wordId);

	public boolean deleteWordTagIdSet(int userId, int wordId);

	public boolean deleteAllWordTagIdSet(int userId);

	public Set<Integer> getWordTagIdSet(int userId, int wordId);

}
