package com.oleapp.colibriweb.model;

import java.io.Serializable;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
	private Set<Integer> tagIdSet;

	public static Word getNewInstance() {
		return AppSettings.context.getBean("word", Word.class);
	}

}
