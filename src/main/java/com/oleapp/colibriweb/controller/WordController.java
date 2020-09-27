package com.oleapp.colibriweb.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class WordController {
	public static final long[] timeDeltaArray;

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
	}

}
