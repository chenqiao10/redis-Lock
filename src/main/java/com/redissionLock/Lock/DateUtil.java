package com.redissionLock.Lock;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public static Date getDateAddMillSecond(Date date, int millSecond) {
		Calendar cal = Calendar.getInstance();
		if (null != date) {// 没有 就取当前时间
			cal.setTime(date);
		}
		cal.add(Calendar.MILLISECOND, millSecond);
		return cal.getTime();
	}

}
