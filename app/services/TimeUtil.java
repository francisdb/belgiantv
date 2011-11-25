package services;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	
	private TimeUtil() {
		throw new UnsupportedOperationException("Utility class");
	}
	
	public static Date merge(final Date date, final Date time) {
		Calendar hourCal = Calendar.getInstance();
		hourCal.setTime(time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, hourCal.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, hourCal.get(Calendar.MILLISECOND));
		return cal.getTime();
	}
}
