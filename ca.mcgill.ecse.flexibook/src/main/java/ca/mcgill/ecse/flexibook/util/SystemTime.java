package ca.mcgill.ecse.flexibook.util;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar; //use for appointment

public class SystemTime {


    public static boolean testing = false;
    public static String testDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
	public static String originalTestDateTimeString;//use for appointment

    /** Set whether the app is being tested
     * @author Tyler
     */
    public static void setTesting(boolean testing) {
        SystemTime.testing = testing;
    }

    /** Set test date to string with format "yyyy-MM-dd+HH:mm"
     * @author Tyler
     */
    public static void setTestDateTime(String testDateTime) {
        SystemTime.testDateTime = testDateTime;
        SystemTime.originalTestDateTimeString = testDateTime; // set to be the exact same String
    }

    /** Get the system time or test time
     * @author Tyler
     */
    public static String getSystemTime() {
        return testing ? testDateTime : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
    }

	/** Get the system date from date-time
	 * @author Tyler
	 */
	public static Date getSystemDate() {
		return Date.valueOf(getSystemTime().split("\\+")[0]);
	}

    /*******for Appointment**********/
	/** Method that returns a calendar. If we are running a test, the method returns a calendar with the date and time set by the test. If we are executing the system (not running a test), 
	 * the calendar of the system is returned.
	 * @author Sandy
	 * @return calendar
	 */
	public static Calendar getSystemCalendar() {
		//DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE;  //EX.: system's time and date is "2020-12-01+09:00" 
		//LocalDateTime testerLocalDateTime = LocalDateTime.parse(originalTestDateTimeString, formatter); //tester localDateTime
		LocalDateTime testerLocalDateTime = LocalDateTime.parse(originalTestDateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm")); //tester localDateTime
		if(testing==true) {
			return localDateTimeToCalendar(testerLocalDateTime); //tester calendar
		}else {
			return localDateTimeToCalendar(LocalDateTime.now()); //NOT testing ==> get present calendar
		}
	}
	/** Method that converts a LocalDateTime object into a Calendar. 
	 * 
	 * @param localDateTime The time generated from the local computer running the application.
	 * @return calendar
	 */
	public static Calendar localDateTimeToCalendar(LocalDateTime localDateTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(localDateTime.getYear(), localDateTime.getMonthValue()-1, localDateTime.getDayOfMonth(),
				localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
		return calendar;
	}
	/**
	 * Method that make sure that the time in a date is set to 0:00:00.
	 *
	 * @param date A value of the Day of the month and year.
	 * @return cleanedDate
	 * @author tutor
	 */
	public static Date cleanDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		java.util.Date tempCleanedDate = cal.getTime();
		java.sql.Date cleanedDate = new java.sql.Date(tempCleanedDate.getTime());
		return cleanedDate;
	}

	/**
	 * This method returns a Date cleanedDate with the appropriate input date and input time.
	 *
	 * @param date A value of the day of the month and year.
	 * @param time A value of the time of day.
	 * @return cleanedDate (Correct Date with time set to input time)
	 * @author Sandy
	 */
	public static Date cleanDate(Date date, Time time) {
		date = cleanDate(date); //make sure that time of date is set to 0:00:00
		//DATE->CALENDAR
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime());
		//TIME->CALENDAR
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(time.getTime());
		//Extract h,m,s,ms from TIME and set the time of DATECALENDAR
		int h = cal2.get(Calendar.HOUR_OF_DAY);
		int m = cal2.get(Calendar.MINUTE);
		int s = cal2.get(Calendar.SECOND);
		int ms = cal2.get(Calendar.MILLISECOND);

		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		cal.set(Calendar.SECOND, s);
		cal.set(Calendar.MILLISECOND, ms);

		java.util.Date tempCleanedDate = cal.getTime();
		java.sql.Date cleanedDate = new java.sql.Date(tempCleanedDate.getTime());
		return cleanedDate;  //new cleaned date (Correct Date with time set to input time)
	}

	/**
	 * This method merge java.sql.Date and java.sql.Time into java.util.Calendar. A calendar with the right Date and Time is returned.
	 *
	 * @param date A value of the day of the month and year.
	 * @param time A value of the time of day.
	 * @return cal
	 * @author Sandy
	 */
	public static Calendar dateTime2Calendar(Date date, Time time) {
		date = cleanDate(date); //make sure time in a date is set to 0:00:00.
		//DATE -> CALENDAR
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime());
		//TIME->CALENDAR
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(time.getTime());
		//extract h,m,s,ms from cal2
		int h = cal2.get(Calendar.HOUR_OF_DAY);
		int m = cal2.get(Calendar.MINUTE);
		int s = cal2.get(Calendar.SECOND);
		int ms = cal2.get(Calendar.MILLISECOND);
		//SET time
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		cal.set(Calendar.SECOND, s);
		cal.set(Calendar.MILLISECOND, ms);
		cal.getTime();
		return cal;
	}

	public static Calendar downtime2Cal(Calendar appStartCal, int intDowntimeStart) {
		appStartCal.add(Calendar.MINUTE, intDowntimeStart);
		appStartCal.getTime();
		return appStartCal;
	}

	/**
	 * This method takes as input a Time time, adds the input int duration (minutes) to it and returns a Time finalTime.
	 *
	 * @param date A value of the day of the month and year.
	 * @param time A value of the time of day.
	 * @param addMin A duration in minutes to be added to the provided time.
	 * @return finalTime: Time
	 * @author Sandy
	 */
	public static Time sumOfTime(Date date, Time time, int addMin) {


		Date cleanedDate = cleanDate(date, time);

		//Date -> calendar
		Calendar cal = Calendar.getInstance();
		cal.setTime(cleanedDate);

		//add minutes
		cal.add(Calendar.MINUTE, addMin);
		//Calendar -> Date
		java.util.Date tempCleanedDate = cal.getTime();
		java.sql.Date dateWithFinalTime = new java.sql.Date(tempCleanedDate.getTime());
		//return Time
		return new Time(dateWithFinalTime.getTime());

	}

	/*******END for Appointment**********/
}
