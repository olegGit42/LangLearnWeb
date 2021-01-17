package com.oleapp.colibriweb.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.oleapp.colibriweb.dao.impl.PostgresWordDAO;
import com.oleapp.colibriweb.model.Word;

public class WordController {
	public static final long[] timeDeltaArray;
	public static final String[] repeatPeriodArray;

	public static final int MAX_BOX = 12;

	public static final long minute_ms = 60_000L;
	public static final long hour_ms = minute_ms * 60;
	public static final long day_ms = hour_ms * 24;
	public static final long week_ms = day_ms * 7;
	public static final long month_ms = day_ms * 30;
	public static final long month_6_ms = month_ms * 6;
	public static final long year_ms = month_6_ms * 2;

	public static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static final DateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	public static enum Command {

		ADD("[add]"), REPLACE("[rep]"), DELETE("[del]"), FORGOT("[fgt]"), SHOW("[shw]"), NEXT("[nxt]"), BACK("[bck]");

		public final String command;

		private Command(String command) {
			this.command = command;
		}

	}

	static {
		timeDeltaArray = new long[MAX_BOX + 1];
		timeDeltaArray[0] = minute_ms * 2;
		timeDeltaArray[1] = minute_ms * 10;
		timeDeltaArray[2] = day_ms;
		timeDeltaArray[3] = day_ms * 3;
		timeDeltaArray[4] = day_ms * 5;
		timeDeltaArray[5] = week_ms;
		timeDeltaArray[6] = week_ms * 2;
		timeDeltaArray[7] = week_ms * 3;
		timeDeltaArray[8] = month_ms;
		timeDeltaArray[9] = month_ms * 2;
		timeDeltaArray[10] = month_6_ms;
		timeDeltaArray[11] = year_ms;
		timeDeltaArray[12] = year_ms * 2;

		repeatPeriodArray = new String[MAX_BOX + 1];
		repeatPeriodArray[0] = "period_box_0";
		repeatPeriodArray[1] = "period_box_1";
		repeatPeriodArray[2] = "period_box_2";
		repeatPeriodArray[3] = "period_box_3";
		repeatPeriodArray[4] = "period_box_4";
		repeatPeriodArray[5] = "period_box_5";
		repeatPeriodArray[6] = "period_box_6";
		repeatPeriodArray[7] = "period_box_7";
		repeatPeriodArray[8] = "period_box_8";
		repeatPeriodArray[9] = "period_box_9";
		repeatPeriodArray[10] = "period_box_10";
		repeatPeriodArray[11] = "period_box_11";
		repeatPeriodArray[12] = "period_box_12";
	}

	public static boolean doCommand(int userId, Word word, Command command, String translate) {

		if (command == null) {
			return false;
		}

		try {

			switch (command) {
			case ADD:
				translate = translate.replace(Command.ADD.command, "").trim();
				if (translate.isEmpty()) {
					return false;
				}
				word.setTranslate(word.getTranslate() + ", " + translate);
				PostgresWordDAO.getInstance().update(word, userId);
				return true;
			case REPLACE:
				translate = translate.replace(Command.REPLACE.command, "").trim();
				if (translate.isEmpty()) {
					return false;
				}
				word.setTranslate(translate);
				PostgresWordDAO.getInstance().update(word, userId);
				return true;
			case FORGOT:
				word.setNewBoxAndUpdDate(0);
				PostgresWordDAO.getInstance().update(word, userId);
				return true;
			case DELETE:
				PostgresWordDAO.getInstance().delete(word.getId(), userId);
				return true;
			case SHOW:
				return true;
			case BACK:
				word.setNewBoxAndUpdDate(word.getBox() - 1);
				PostgresWordDAO.getInstance().update(word, userId);
				return true;
			case NEXT:
				word.setNewBoxAndUpdDate(word.getBox() + 1);
				PostgresWordDAO.getInstance().update(word, userId);
				return true;
			default:
				return false;
			}

		} catch (Exception e) {
			return false;
		}

	}

	public static Command commandResolver(String command) {

		try {

			Command cmd = null;

			for (Command c : Command.values()) {
				if (command.startsWith(c.command)) {
					cmd = c;
				}
			}

			return cmd;
		} catch (Exception e) {
			return null;
		}

	}

}
