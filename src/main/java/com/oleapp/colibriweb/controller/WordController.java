package com.oleapp.colibriweb.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class WordController {
	public static final long[] timeDeltaArray;
	public static final String[] repeatPeriodArray;

	public static final int MAX_BOX = 10;

	public static final long minute_ms = 60_000L;
	public static final long hour_ms = minute_ms * 60;
	public static final long day_ms = hour_ms * 24;
	public static final long week_ms = day_ms * 7;
	public static final long month_ms = day_ms * 30;
	public static final long month_6_ms = month_ms * 6;
	public static final long year_ms = month_6_ms * 2;

	public static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static final DateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	static {
		timeDeltaArray = new long[MAX_BOX + 1];
		timeDeltaArray[0] = minute_ms * 2;
		timeDeltaArray[1] = minute_ms * 10;
		timeDeltaArray[2] = day_ms;
		timeDeltaArray[3] = day_ms * 3;
		timeDeltaArray[4] = week_ms;
		timeDeltaArray[5] = week_ms * 2;
		timeDeltaArray[6] = month_ms;
		timeDeltaArray[7] = month_ms * 2;
		timeDeltaArray[8] = month_6_ms;
		timeDeltaArray[9] = year_ms;
		timeDeltaArray[10] = year_ms * 2;

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
	}

}
