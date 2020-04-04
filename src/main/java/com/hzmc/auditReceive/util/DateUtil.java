package com.hzmc.auditReceive.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author liushuai
 *
 */
public final class DateUtil {
	
	/** date format : yyyy-MM-dd HH:mm:ss */
	public static DateFormat DATEFORMAT_DATABASE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private DateUtil() {
	}
	
	/**
	 * 返回数组，分别为yyyy,mm,dd,hh,mi,ss,weekday
	 * token[0] -- yyyy
	 * token[1] -- mm
	 * token[2] -- dd
	 * token[3] -- hh
	 * token[4] -- mi
	 * token[5] -- ss
	 * token[6] -- weekday
	 * token[6] -- week
	 * @param date 
	 * @return 日期各部分数组
	 */
	public static String[] getTimeToken(Date date) {
		
		DATEFORMAT_DATABASE.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String dateStr = DATEFORMAT_DATABASE.format(date);
		
		String[] token = new String[8];
		String[] parts = dateStr.split(" ");
		String[] ymd = parts[0].split("-");
		String[] hms = parts[1].split(":");
		token[0] = ymd[0]; // yyyy
		token[1] = ymd[1]; // mm
		token[2] = ymd[2]; // dd
		token[3] = hms[0]; // hh
		token[4] = hms[1]; // mi
		token[5] = hms[2]; // ss
		
		Calendar c = Calendar.getInstance();
		//设置时区
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		c.setTime(date);
		
		token[6] = "" + c.get(Calendar.DAY_OF_WEEK); // week day
		token[7] = "" + c.get(Calendar.WEEK_OF_YEAR); // week of year
		return token;
	}

	/**
	 * 把unix时间戳转化为date对象
	 * c++ 汇报的时间精确到微秒
	 * @param time  unix时间戳
	 * @return date
	 */
	public static Date unixTime2Date(Long time){
		Date date = new Date(time/1000);
		return date;
	}

}
