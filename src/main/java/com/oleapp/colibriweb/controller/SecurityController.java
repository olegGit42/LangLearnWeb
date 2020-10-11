package com.oleapp.colibriweb.controller;

import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
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

import lombok.Data;

@Controller
@SessionAttributes({ "newWord", "bufferWord" })
@Data
public class SecurityController {

	@Data
	public static class WordStatistics {
		private int allWordsCount;
		private int todayRepeatCount;
		private String repeatDateTime;
		private String box;

		public void refresh(int userId, Locale locale, MessageSource localeSource, long timezoneOffset,
				List<String> waitingWordList) {

			IAppStatisticDAO appStatistic = PostgresAppStatisticDAO.getInstance();

			allWordsCount = appStatistic.getAllWordsCount(userId);

			todayRepeatCount = appStatistic.getTodayWordsRepeatCount(userId);

			Word repWord = PostgresWordDAO.getInstance().getNearestRepeatWord(userId, false, timezoneOffset, waitingWordList);
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

				repeatDateTime = msg + (repWord.getBox() < 2 ? repWord.obtainRepTimeString(timezoneOffset)
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
	public ModelAndView userPage(@RequestParam(value = "error_add", required = false) String error_add,
			@RequestParam(value = "error_empty_field", required = false) String error_empty_field,
			@RequestParam(value = "success_add_word", required = false) String success_add_word,
			@RequestParam(value = "show_word", required = false) String show_word, @ModelAttribute("newWord") Word newWord,
			@ModelAttribute("wordStat") WordStatistics wordStat, @ModelAttribute StringBuilder bufferWord, Principal user,
			Locale locale, HttpSession session) {

		ModelAndView model = new ModelAndView();
		model.addObject("username", user.getName());

		if (error_add != null) {
			model.addObject("error_add_word", localeSource.getMessage("error_add_word", null, locale));
		}

		if (error_empty_field != null) {
			model.addObject("error_empty_field", localeSource.getMessage("error_empty_field", null, locale));
		}

		if (success_add_word != null) {
			model.addObject("success_add_word", localeSource.getMessage("success_add_word", null, locale));
		}

		int userId = obtainUserId(user.getName());

		/*
		 * Long timezoneOffset = (Long) session.getAttribute("timezoneOffset");
		 * 
		 * if (timezoneOffset == null) { timezoneOffset = 0L; }
		 */

		// dev stub
		Long timezoneOffset = 0L;

		Word repWord;

		if (show_word == null) {
			repWord = PostgresWordDAO.getInstance().getNearestRepeatWord(userId, true, timezoneOffset, null);
		} else {
			repWord = PostgresWordDAO.getInstance().getWord(bufferWord.toString(), userId);
			bufferWord.setLength(0);
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

		if (repWord == null) {
			repWord = Word.getNewInstance();
			waitingWordList = null;
		}

		wordStat.refresh(userId, locale, localeSource, timezoneOffset, waitingWordList);

		if (show_word == null) {
			repWord.setTranslate(null);
		}
		model.addObject("repWord", repWord);

		model.setViewName("/auth/user");

		return model;
	}

	@RequestMapping(value = "/auth/user/add_new_word", method = RequestMethod.POST)
	public String addWord(@ModelAttribute("newWord") Word newWord, Principal user) {

		if (!newWord.getWord().trim().equals("") && !newWord.getTranslate().trim().equals("")) {

			IUserDAO userDAO = PostgresUserDAO.getInstance();
			User authUser = userDAO.getUser(user.getName());

			if (PostgresWordDAO.getInstance().insert(Word.getNewInstance().afterInitNewWord(newWord.getWord().trim(),
					newWord.getTranslate().trim(), authUser.getId()), authUser.getId())) {

				authUser.setAuthorizationToken(null);
				authUser.setAuthorizationTokenBuffer(null);

				userDAO.update(authUser);

				newWord.setWord("");
				newWord.setTranslate("");

				return "redirect:/auth/user?success_add_word=true";
			} else {
				return "redirect:/auth/user?error_add=true";
			}

		} else {
			return "redirect:/auth/user?error_empty_field=true";
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
		boolean isFillWord = !(repWord.getWord() == null || repWord.getWord().equals(""));
		boolean isFillTranslate = !(repWord.getTranslate() == null || repWord.getTranslate().equals(""));
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
			Principal user) {

		int userId = obtainUserId(user.getName());
		List<Word> wordList = PostgresWordDAO.getInstance().getForgettableWords(userId);
		StringBuilder wordSB = new StringBuilder();

		wordList.forEach(w -> {
			if (show_translate != null && w.getWord().equals(show_translate)) {
				wordSB.append("<h3 id=\"translation\"><a href=\"forgettable\"><u>" + w.getRepeateIndicator() + " - " + w.getWord()
						+ " - " + w.getTranslate() + "</u></a></h3>");
			} else {
				wordSB.append("<p title=\"" + w.getTranslate() + "\"><a href=\"?show_translate=" + w.getWord() + "\">"
						+ w.getRepeateIndicator() + " - " + w.getWord() + "</a></p>");
			}
		});

		ModelAndView model = new ModelAndView();
		model.addObject("username", user.getName());
		model.setViewName("/auth/forgettable");
		model.addObject("wordList", wordSB);

		return model;
	}

	@RequestMapping(value = "/auth/dictionary", method = RequestMethod.GET)
	public ModelAndView dictionaryPage(Principal user, HttpSession session) {

		/*
		 * Long timezoneOffset = (Long) session.getAttribute("timezoneOffset");
		 * 
		 * if (timezoneOffset == null) { timezoneOffset = 0L; }
		 */

		// dev stub
		Long timezoneOffset = 0L;

		final Long timezoneOffsetFinal = timezoneOffset;

		int userId = obtainUserId(user.getName());
		List<Word> wordList = PostgresWordDAO.getInstance().getUserWords(userId);
		StringBuilder wordSB = new StringBuilder();
		wordList.forEach(w -> wordSB.append(
				"<p>" + w.obtainRepDateString(timezoneOffsetFinal) + " - " + w.getWord() + " - " + w.getTranslate() + "</p>"));

		ModelAndView model = new ModelAndView();
		model.addObject("username", user.getName());
		model.setViewName("/auth/dictionary");
		model.addObject("wordList", wordSB);

		return model;
	}

	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public ModelAndView login(@RequestParam(value = "error", required = false) String error, Locale locale) {

		ModelAndView model = new ModelAndView();
		if (error != null) {
			model.addObject("error", localeSource.getMessage("invalid_name_pas", null, locale));
		}

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

	public static long getGMTSignedZone(String timezoneOffset) {
		timezoneOffset = timezoneOffset == null ? "" : timezoneOffset.trim();
		if (!timezoneOffset.equals("")) {
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
		return 0;
	}

}
