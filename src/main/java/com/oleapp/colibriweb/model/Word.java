package com.oleapp.colibriweb.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oleapp.colibriweb.controller.WordController;
import com.oleapp.colibriweb.dao.impl.PostgresWordDAO;
import com.oleapp.colibriweb.service.AppSettings;

import lombok.Data;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class Word implements Serializable {
	private static final long serialVersionUID = -1943399776402594085L;

	public static final long PLANNED_TIME = 10_000_000_000_000L;

	private int id;
	private String word;
	private String translate;
	private long regTime;
	private long creationTime;
	private int box;
	private int repeateIndicator;
	private Set<Integer> tagIdSet = new HashSet<>();
	@JsonIgnore
	private boolean isPlanned = false;

	public static Word getNewInstance() {
		return AppSettings.context.getBean("word", Word.class);
	}

	public Word afterInitNewWord(String word, String translate, int userId, boolean isPlannedWord) {
		final long time = System.currentTimeMillis();
		this.id = PostgresWordDAO.getInstance().incrementAndGetMaxWordId(userId);
		this.regTime = (isPlannedWord ? PLANNED_TIME : time);
		this.creationTime = time;
		this.word = word;
		this.translate = translate;

		return this;
	}

	public void setNewBoxAndUpdDate(int box) {
		if (box < 0) {
			box = 0;
		}
		if (box > WordController.MAX_BOX) {
			box = WordController.MAX_BOX;
		}
		setBox(box);
		inctementRepeateIndicator();
	}

	public void inctementRepeateIndicator() {
		++repeateIndicator;
		regTime = System.currentTimeMillis();
	}

	public void startRepeatPlanned() {
		this.box = 0;
		this.repeateIndicator = (-1);
		this.regTime = System.currentTimeMillis() - WordController.hour_ms;
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

	public boolean getIsPlanned() {
		return isPlanned;
	}

	public void setIsPlanned(boolean isPlanned) {
		this.isPlanned = isPlanned;
	}

}
