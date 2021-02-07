package com.oleapp.colibriweb.model;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.oleapp.colibriweb.dao.impl.PostgresTagDAO;
import com.oleapp.colibriweb.service.AppSettings;

import lombok.Data;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class TagRegistry implements Serializable {
	private static final long serialVersionUID = 543191549900073090L;

	private AtomicInteger maxID = new AtomicInteger();
	private SortedMap<Integer, String> idTagMap = new TreeMap<>();

	public static TagRegistry getNewInstance() {
		return AppSettings.context.getBean("tagRegistry", TagRegistry.class);
	}

	public boolean containsTag(String tag) {
		for (String existedTag : idTagMap.values()) {
			if (tag.trim().equalsIgnoreCase(existedTag)) {
				return true;
			}
		}
		return false;
	}

	public int addNewTag(String tag, int userId) {
		if (containsTag(tag)) {
			return obtainTagId(tag);
		} else {
			int newId = maxID.incrementAndGet();
			idTagMap.put(newId, tag.trim().toUpperCase());
			if (PostgresTagDAO.getInstance().updateTags(this, userId)) {
				return newId;
			} else {
				return 0;
			}
		}
	}

	public int obtainTagId(String tag) {
		for (int id : idTagMap.keySet()) {
			if (tag.trim().equalsIgnoreCase(idTagMap.get(id))) {
				return id;
			}
		}
		return 0;
	}

}
