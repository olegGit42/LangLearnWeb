package com.oleapp.colibriweb.controller;

import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import com.oleapp.colibriweb.dao.interfaces.IUserDAO;
import com.oleapp.colibriweb.model.User;
import com.oleapp.colibriweb.model.Word;

import lombok.Data;

@Controller
@SessionAttributes("newWord")
@Data
public class SecurityController {

	@Data
	public static class RepeatStatistics {
		private int todayCount;
		private String repeatDateTime;

		public void refresh(int userId) {
			todayCount = PostgresAppStatisticDAO.getInstance().getTodayWordsRepeatCount(userId);

			Word repWord = PostgresWordDAO.getInstance().getNearestRepeatWord(userId, false);
			if (repWord == null) {
				repeatDateTime = "-";
			} else {
				repeatDateTime = WordController.dateTimeFormat
						.format(new Date(repWord.getRegTime() + WordController.timeDeltaArray[repWord.getBox()]));
			}
		}
	}

	@Autowired
	private MessageSource localeSource;

	@ModelAttribute("newWord")
	public Word createNewWord() {
		return Word.getNewInstance();
	}

	public int obtainUserId(String userName) {
		return PostgresUserDAO.getInstance().getUser(userName).getId();
	}

	@RequestMapping(value = "/auth/user", method = RequestMethod.GET)
	public ModelAndView userPage(@RequestParam(value = "error_add", required = false) String error_add,
			@RequestParam(value = "error_empty_field", required = false) String error_empty_field,
			@RequestParam(value = "success_add_word", required = false) String success_add_word,
			@RequestParam(value = "show_word", required = false) String show_word, @ModelAttribute("newWord") Word newWord,
			@ModelAttribute("repStat") RepeatStatistics repStat, Principal user, Locale locale) {

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
		repStat.refresh(userId);
		Word repWord = PostgresWordDAO.getInstance().getNearestRepeatWord(userId, true);

		Date date = new Date();
		date.setTime(System.currentTimeMillis() + WordController.minute_ms);
		long nowDateTime = 0;
		try {
			date = WordController.dateTimeFormat.parse(WordController.dateTimeFormat.format(date));
			nowDateTime = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (repWord == null || (repWord.getBox() < 1 && repWord.obtainRepTime() > nowDateTime)) {
			repWord = Word.getNewInstance();
		}

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
	public String showWord(@ModelAttribute("repWord") Word repWord, Principal user) {
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

}