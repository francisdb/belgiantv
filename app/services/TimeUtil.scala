package services

import java.util.Calendar
import java.util.Date

object TimeUtil {
	def merge(date:Date, time:Date) {
		val hourCal = Calendar.getInstance()
		hourCal.setTime(time)
		val cal = Calendar.getInstance()
		cal.setTime(date)
		cal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY))
		cal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE))
		cal.set(Calendar.SECOND, hourCal.get(Calendar.SECOND))
		cal.set(Calendar.MILLISECOND, hourCal.get(Calendar.MILLISECOND))
		cal.getTime()
	}
}