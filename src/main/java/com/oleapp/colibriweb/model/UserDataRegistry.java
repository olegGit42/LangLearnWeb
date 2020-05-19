package com.oleapp.colibriweb.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.oleapp.colibriweb.service.AppSettings;

import lombok.Data;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class UserDataRegistry implements Serializable {
	private static final long serialVersionUID = -6921273139257920727L;

	@Autowired
	private TagRegistry tagRegistry;
	private String appLocale;
	private int userId;
	private String userName;
	private String userPasswordHash;
	private boolean autoEnter;
	private int maxWordID;
	private List<Word> allUserWordsList;

	public static UserDataRegistry getNewInstance() {
		return AppSettings.context.getBean("userDataRegistry", UserDataRegistry.class);
	}

}
