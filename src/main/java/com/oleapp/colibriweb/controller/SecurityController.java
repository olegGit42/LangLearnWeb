package com.oleapp.colibriweb.controller;

import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.oleapp.colibriweb.dao.impl.PostgresAppStatisticDAO;
import com.oleapp.colibriweb.dao.impl.PostgresUserDAO;
import com.oleapp.colibriweb.dao.impl.PostgresWordDAO;
import com.oleapp.colibriweb.dao.interfaces.IAppStatisticDAO;
import com.oleapp.colibriweb.dao.interfaces.IUserDAO;
import com.oleapp.colibriweb.model.User;
import com.oleapp.colibriweb.model.Word;
import com.oleapp.colibriweb.service.AppSettings;

import lombok.Data;

@Controller
@SessionAttributes({ "newWord", "bufferWord" })
@Data
public class SecurityController {

	public static final String SUCCESS_ADD_WORD = "s1";
	public static final String SUCCESS_COMMAND = "s2";

	public static final String ERROR_ADD_WORD = "e1";
	public static final String ERROR_EMPTY_FIELD = "e2";
	public static final String ERROR_NO_PLANNED = "e3";
	public static final String ERROR_COMMAND = "e4";

	@Data
	public static class WordStatistics {
		private String allWordsCount;
		private int todayRepeatCount;
		private String repeatDateTime;
		private String box;

		public void refresh(int userId, Locale locale, MessageSource localeSource, long timezoneOffset, Word repWord,
				String defaultUTC) {

			IAppStatisticDAO appStatistic = PostgresAppStatisticDAO.getInstance();

			int allWCOnlyPlanned = appStatistic.getAllWordsCount(userId, true);
			int allWC = appStatistic.getAllWordsCount(userId, false);

			allWordsCount = allWC
					+ (allWCOnlyPlanned > 0 ? (" (" + (allWC - allWCOnlyPlanned) + " / " + allWCOnlyPlanned + ")") : "");

			todayRepeatCount = appStatistic.getTodayWordsRepeatCount(userId, timezoneOffset);

			if (repWord == null) {
				repWord = PostgresWordDAO.getInstance().getNearestRepeatWord(userId, false, timezoneOffset, null);
			}

			if (repWord == null) {
				repeatDateTime = ": " + localeSource.getMessage("no words", null, locale);
				box = "-";
			} else {
				String msg = null;

				if (repWord.obtainRepTime() > System.currentTimeMillis()) {
					msg = " " + localeSource.getMessage("will be", null, locale) + ": ";
				} else {
					msg = " " + localeSource.getMessage("was supposed to be", null, locale) + ": ";
				}

				repeatDateTime = msg + (repWord.getBox() < 2 ? repWord.obtainRepTimeString(timezoneOffset) + defaultUTC
						: repWord.obtainRepDateString(timezoneOffset));
				box = repWord.getBox() + " | "
						+ localeSource.getMessage(WordController.repeatPeriodArray[repWord.getBox()], null, locale);
			}
		}
	}

	@Autowired
	private MessageSource localeSource;

	@ModelAttribute("newWord")
	public Word createNewWord() {
		return Word.getNewInstance();
	}

	@ModelAttribute("bufferWord")
	public StringBuilder createBufferWord() {
		return new StringBuilder();
	}

	public int obtainUserId(String userName) {
		return PostgresUserDAO.getInstance().getUser(userName).getId();
	}

	@RequestMapping(value = "/auth/user", method = RequestMethod.GET)
	public ModelAndView userPage(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "success", required = false) String success,
			@RequestParam(value = "show_word", required = false) String show_word, @ModelAttribute("newWord") Word newWord,
			@ModelAttribute("wordStat") WordStatistics wordStat, @ModelAttribute StringBuilder bufferWord,
			@RequestParam(value = "refresh", required = false) String refresh, Principal user, Locale locale,
			HttpSession session) {

		ModelAndView model = new ModelAndView();
		model.addObject("username", user.getName());
		model.addObject("appurl", AppSettings.APP_URL);

		if (error != null) {
			switch (error) {
			case ERROR_ADD_WORD:
				model.addObject("error", localeSource.getMessage("error_add_word", null, locale));
				break;
			case ERROR_EMPTY_FIELD:
				model.addObject("error", localeSource.getMessage("error_empty_field", null, locale));
				break;
			case ERROR_NO_PLANNED:
				model.addObject("error", localeSource.getMessage("error_no_planned", null, locale));
				break;
			case ERROR_COMMAND:
				model.addObject("error", localeSource.getMessage("ERROR_COMMAND", null, locale));
				break;
			default:
				break;
			}
		}

		if (success != null) {
			switch (success) {
			case SUCCESS_ADD_WORD:
				model.addObject("success", localeSource.getMessage("success_add_word", null, locale));
				break;
			case SUCCESS_COMMAND:
				model.addObject("success", localeSource.getMessage("SUCCESS_COMMAND", null, locale));
				break;
			default:
				break;
			}
		}

		int userId = obtainUserId(user.getName());

		Long timezoneOffset = (Long) session.getAttribute("timezoneOffset");
		String defaultUTC = "";

		if (timezoneOffset == null) {
			timezoneOffset = AppSettings.TIMEZONE_OFFSET;
			defaultUTC = AppSettings.DEFAULT_UTC;
		}

		Word repWord;

		if (show_word == null || bufferWord.toString().isEmpty()) {
			repWord = PostgresWordDAO.getInstance().getNearestRepeatWord(userId, true, timezoneOffset, null);
		} else {
			repWord = PostgresWordDAO.getInstance().getWord(bufferWord.toString(), userId);
		}

		Date date = new Date();
		date.setTime(System.currentTimeMillis() + WordController.minute_ms);
		long nowDateTime = 0;
		try {
			date = WordController.dateTimeFormat.parse(WordController.dateTimeFormat.format(date));
			nowDateTime = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		List<String> waitingWordList = new ArrayList<>();
		while (repWord != null && (repWord.getBox() < 2 && repWord.obtainRepTime() > nowDateTime)) {
			waitingWordList.add(repWord.getWord());
			repWord = PostgresWordDAO.getInstance().getNearestRepeatWord(userId, true, timezoneOffset, waitingWordList);
		}

		wordStat.refresh(userId, locale, localeSource, timezoneOffset, repWord, defaultUTC);

		repWord = repWord == null ? Word.getNewInstance() : repWord;

		if (show_word == null || bufferWord.toString().isEmpty()) {
			repWord.setTranslate(null);
		}

		model.addObject("repWord", repWord);
		model.setViewName("/auth/user");

		bufferWord.setLength(0);

		if (refresh != null && (refresh.equals("true") || refresh.equals("planned"))) {
			newWord.setWord("");
			newWord.setTranslate("");
			newWord.setIsPlanned(false);
			if (refresh.equals("planned")) {
				newWord.setWord(WordController.Command.ADD_PLANNED.command + " 10");
			}
		}

		return model;
	}

	@RequestMapping(value = "/auth/user/add_new_word", method = RequestMethod.POST)
	public String addWord(@ModelAttribute("newWord") Word newWord, @ModelAttribute StringBuilder bufferWord, Principal user) {

		IUserDAO userDAO = PostgresUserDAO.getInstance();
		User authUser = userDAO.getUser(user.getName());

		String word = newWord.getWord().trim();
		String translate = newWord.getTranslate().trim();

		if (translate.isEmpty() && WordController.commandResolver(word) == WordController.Command.ADD_PLANNED) {
			if (WordController.doCommand(authUser.getId(), null, WordController.Command.ADD_PLANNED, word)) {
				newWord.setWord("+" + word);
				return "redirect:/auth/user?success=" + SUCCESS_ADD_WORD;
			} else {
				return "redirect:/auth/user?error=" + ERROR_NO_PLANNED;
			}
		} else if (!word.isEmpty() && !translate.isEmpty()) {

			Word wordForCheck = PostgresWordDAO.getInstance().getWord(word, authUser.getId());

			if (wordForCheck == null && PostgresWordDAO.getInstance().insert(
					Word.getNewInstance().afterInitNewWord(word, translate, authUser.getId(), newWord.getIsPlanned()),
					authUser.getId())) {

				authUser.setAuthorizationToken(null);
				authUser.setAuthorizationTokenBuffer(null);

				userDAO.update(authUser);

				newWord.setWord("");
				newWord.setTranslate("");
				newWord.setIsPlanned(false);

				return "redirect:/auth/user?success=" + SUCCESS_ADD_WORD;
			} else if (wordForCheck != null) {
				String oldTranslate = wordForCheck.getTranslate();
				if (WordController.doCommand(authUser.getId(), wordForCheck, WordController.commandResolver(translate),
						translate)) {

					authUser.setAuthorizationToken(null);
					authUser.setAuthorizationTokenBuffer(null);

					userDAO.update(authUser);

					switch (WordController.commandResolver(translate)) {
					case ADD:
						newWord.setTranslate("ADDED " + translate);
						break;
					case REPLACE:
						newWord.setTranslate("REPLACED " + translate + " -> " + oldTranslate);
						break;
					case FORGOT:
						newWord.setTranslate("FORGOTTEN " + WordController.Command.FORGOT.command);
						break;
					case DELETE:
						newWord.setTranslate("DELETED " + WordController.Command.DELETE.command + " " + oldTranslate);
						break;
					case SHOW:
						bufferWord.setLength(0);
						bufferWord.append(word);
						return "redirect:/auth/user?show_word=true";
					case BACK:
						newWord.setTranslate("BOX - 1 " + WordController.Command.BACK.command);
						break;
					case NEXT:
						newWord.setTranslate("BOX + 1 " + WordController.Command.NEXT.command);
						break;
					default:
						return "redirect:/auth/user?error=" + ERROR_COMMAND;
					}

					return "redirect:/auth/user?success=" + SUCCESS_COMMAND;
				} else {
					return "redirect:/auth/user?error=" + ERROR_COMMAND;
				}
			} else {
				return "redirect:/auth/user?error=" + ERROR_ADD_WORD;
			}

		} else {
			return "redirect:/auth/user?error=" + ERROR_EMPTY_FIELD;
		}
	}

	@RequestMapping(value = "/auth/user/show_word", method = RequestMethod.POST)
	public String showWord(@ModelAttribute("repWord") Word repWord, @ModelAttribute StringBuilder bufferWord, Principal user) {
		bufferWord.setLength(0);
		bufferWord.append(repWord.getWord());
		return "redirect:/auth/user?show_word=true";
	}

	@RequestMapping(value = "/auth/user/remember_word", method = RequestMethod.POST)
	public String rememberWord(@ModelAttribute("repWord") Word repWord, Principal user) {
		saveRepWord(repWord, user, false);
		return "redirect:/auth/user";
	}

	@RequestMapping(value = "/auth/user/forgot_word", method = RequestMethod.POST)
	public String forgotWord(@ModelAttribute("repWord") Word repWord, Principal user) {
		saveRepWord(repWord, user, true);
		return "redirect:/auth/user";
	}

	public void saveRepWord(Word repWord, Principal user, boolean forgot) {
		boolean isFillWord = !(repWord.getWord() == null || repWord.getWord().isEmpty());
		boolean isFillTranslate = !(repWord.getTranslate() == null || repWord.getTranslate().isEmpty());
		if (isFillWord && isFillTranslate) {

			IUserDAO userDAO = PostgresUserDAO.getInstance();
			User authUser = userDAO.getUser(user.getName());
			int userId = authUser.getId();

			authUser.setAuthorizationToken(null);
			authUser.setAuthorizationTokenBuffer(null);

			userDAO.update(authUser);

			Word word = PostgresWordDAO.getInstance().getWord(repWord.getWord(), userId);

			if (forgot) {
				word.setNewBoxAndUpdDate(0);
			} else {
				word.setNewBoxAndUpdDate(word.getBox() + 1);
			}

			PostgresWordDAO.getInstance().update(word, userId);
		}
	}

	@RequestMapping(value = "/auth/admin", method = RequestMethod.GET)
	public String adminPage() {
		return "/auth/admin";
	}

	@RequestMapping(value = "/auth/forgettable", method = RequestMethod.GET)
	public ModelAndView forgettablePage(@RequestParam(value = "show_translate", required = false) final String show_translate,
			@RequestParam(value = "all", required = false) final String all, Principal user, Locale locale) {

		boolean isAll = ((all != null && all.equals("true")) ? true : false);

		int userId = obtainUserId(user.getName());
		List<Word> wordList = PostgresWordDAO.getInstance().getForgettableWords(userId, isAll);
		StringBuilder wordSB = new StringBuilder();
		wordSB.append("<p>" + localeSource.getMessage("Quantity", null, locale) + " " + (wordList == null ? 0 : wordList.size())
				+ "</p>");

		wordList.forEach(w -> {
			if (show_translate != null && w.getWord().equals(show_translate)) {
				wordSB.append("<h3 id=\"translation\"><a href=\"forgettable?all=" + isAll + "\"><u>" + w.getRepeateIndicator()
						+ " - " + localeSource.getMessage("b", null, locale) + w.getBox() + " - " + w.getWord() + " - "
						+ w.getTranslate() + "</u></a></h3>");
			} else {
				wordSB.append("<p><a href=\"?all=" + isAll + "&show_translate=" + w.getWord() + "#translation\">"
						+ w.getRepeateIndicator() + " - " + localeSource.getMessage("b", null, locale) + w.getBox() + " - "
						+ w.getWord() + "</a></p>");
			}
		});

		ModelAndView model = new ModelAndView();
		model.addObject("username", user.getName());
		model.addObject("wordList", wordSB);
		model.addObject("appurl", AppSettings.APP_URL);
		model.setViewName("/auth/forgettable");

		if (isAll) {
			model.addObject("all", "");
		} else {
			model.addObject("all", "?all=true");
		}

		return model;
	}

	@RequestMapping(value = "/auth/dictionary", method = RequestMethod.GET)
	public ModelAndView dictionaryPage(@RequestParam(value = "sort", required = false) final String sort, Principal user,
			Locale locale, HttpSession session) {

		Long timezoneOffset = (Long) session.getAttribute("timezoneOffset");

		if (timezoneOffset == null) {
			timezoneOffset = AppSettings.TIMEZONE_OFFSET;
		}

		final Long timezoneOffsetFinal = timezoneOffset;

		int userId = obtainUserId(user.getName());
		List<Word> wordList = PostgresWordDAO.getInstance().getUserWords(userId);
		StringBuilder wordSB = new StringBuilder();
		wordSB.append("<p>" + localeSource.getMessage("Quantity", null, locale) + " " + (wordList == null ? 0 : wordList.size())
				+ "</p>");

		if (sort != null && sort.equals("date")) {
			wordList.stream().sorted(Comparator.comparingLong(w -> w.obtainRepTime(timezoneOffsetFinal)))
					.forEach(w -> wordSB.append("<p>" + w.obtainRepDateString(timezoneOffsetFinal) + " - " + w.getWord() + " - "
							+ w.getTranslate() + "</p>"));
		} else {
			wordList.forEach(w -> wordSB.append("<p>" + w.obtainRepDateString(timezoneOffsetFinal) + " - " + w.getWord() + " - "
					+ w.getTranslate() + "</p>"));
		}

		ModelAndView model = new ModelAndView();
		model.addObject("username", user.getName());
		model.addObject("wordList", wordSB);
		model.addObject("appurl", AppSettings.APP_URL);
		model.setViewName("/auth/dictionary");

		if (sort != null && sort.equals("date")) {
			model.addObject("sort", "");
		} else {
			model.addObject("sort", "?sort=date");
		}

		return model;
	}

	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public ModelAndView login(@RequestParam(value = "error", required = false) String error, Locale locale) {

		ModelAndView model = new ModelAndView();
		if (error != null) {
			model.addObject("error", localeSource.getMessage("invalid_name_pas", null, locale));
		}

		model.addObject("appurl", AppSettings.APP_URL);
		model.setViewName("login");

		return model;
	}

	@RequestMapping(value = "/errors/accessDenied", method = RequestMethod.GET)
	public ModelAndView accesssDenied(Principal user, Locale locale) {

		ModelAndView model = new ModelAndView();

		model.addObject("accessDenied", localeSource.getMessage("access_denied", null, locale));

		if (user != null) {
			model.addObject("errorMsg", user.getName() + localeSource.getMessage("no_page_access_1", null, locale));
		} else {
			model.addObject("errorMsg", localeSource.getMessage("no_page_access_2", null, locale));
		}

		model.setViewName("/errors/accessDenied");
		return model;
	}

	@RequestMapping(value = "/userTimeZone", method = RequestMethod.GET)
	public ResponseEntity<String> userTimeZone(@RequestParam(value = "timezoneOffset", required = false) String timezoneOffset,
			HttpSession session) {
		if (session.getAttribute("timezoneOffset") == null) {
			session.setAttribute("timezoneOffset", getGMTSignedZone(timezoneOffset));
		}
		return new ResponseEntity<String>(HttpStatus.OK); // 200
	}

	public static Long getGMTSignedZone(String timezoneOffset) {
		timezoneOffset = timezoneOffset == null ? "" : timezoneOffset.trim();
		if (!timezoneOffset.isEmpty()) {
			Integer zMinutes = Integer.valueOf(timezoneOffset);
			if (zMinutes < 0) {
				zMinutes = zMinutes * (-1);
			}

			// hours 0 to 23
			int hours = zMinutes / 60;
			if (hours > 23) {
				hours = hours / 24;
			}

			// minute conversion
			int minutes = zMinutes - (hours * 60);

			return hours * WordController.hour_ms + minutes * WordController.minute_ms;
		}
		return null;
	}

}
