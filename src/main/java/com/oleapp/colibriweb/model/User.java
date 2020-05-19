package com.oleapp.colibriweb.model;

import java.io.Serializable;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.oleapp.colibriweb.service.AppSettings;

import lombok.Data;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class User implements Serializable {
	private static final long serialVersionUID = -4476017379429717808L;

	public static final String GUEST = "guest";

	private int id;
	private String authorizationToken;
	private String authorizationTokenBuffer;
	private String userName;
	private String userPasswordHash;
	private boolean autoEnter;

	public static User getNewInstance() {
		return AppSettings.context.getBean("user", User.class);
	}

}
