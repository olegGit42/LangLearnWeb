package com.oleapp.colibriweb.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.oleapp.colibriweb.controller.WordController;
import com.oleapp.colibriweb.dao.impl.PostgresWordDAO;
import com.oleapp.colibriweb.service.AppSettings;

import lombok.Data;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class Word implements Serializable {
	private static final long serialVersionUID = -1943399776402594085L;

	private int id;
	private String word;
	private String translate;
	private long regTime;
	private long creationTime;
	private int box;
	private int repeateIndicator;
	private Set<Integer> tagIdSet = new HashSet<>();

	public static Word getNewInstance() {
		return AppSettings.context.getBean("word", Word.class);
	}

	public Word afterInitNewWord(String word, String translate, int userId) {
		final long time = System.currentTimeMillis();
		this.id = PostgresWordDAO.getInstance().incrementAndGetMaxWordId(userId);
		this.regTime = time;
		this.creationTime = time;
		this.word = word;
		this.translate = translate;

		return this;
	}

	public void setNewBoxAndUpdDate(int box) {
		if (box < 0) {
			box = 0;
		}
		if (box > 7) {
			box = 7;
		}
		setBox(box);
		inctementRepeateIndicator();
	}

	public void inctementRepeateIndicator() {
		++repeateIndicator;
		regTime = System.currentTimeMillis();
	}

	public long obtainRepTime() {
		return obtainRepTime(0);
	}

	public long obtainRepTime(long timezoneOffset) {
		return regTime + WordController.timeDeltaArray[box] + timezoneOffset;
	}

	public String obtainRepTimeString() {
		return obtainRepTimeString(0);
	}

	public String obtainRepTimeString(long timezoneOffset) {
		return WordController.dateTimeFormat.format(new Date(obtainRepTime(timezoneOffset)));
	}

	public String obtainRepDateString() {
		return obtainRepDateString(0);
	}

	public String obtainRepDateString(long timezoneOffset) {
		return WordController.dateFormat.format(new Date(obtainRepTime(timezoneOffset)));
	}

}
