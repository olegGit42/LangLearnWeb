package com.oleapp.colibriweb.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppSettings {

	public static final String KEY = "WgeD3PXESApMqc2h";
	public static final ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
	public static final String APP_URL = "http://localhost:8080/ColibriWeb/";
	public static final long TIMEZONE_OFFSET = 0;
	public static final String DEFAULT_UTC = "";

}
