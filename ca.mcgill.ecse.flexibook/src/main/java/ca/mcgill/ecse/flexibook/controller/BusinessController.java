package ca.mcgill.ecse.flexibook.controller;

import static java.util.regex.Pattern.matches;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.model.*;
import ca.mcgill.ecse.flexibook.util.SystemTime;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class BusinessController {

    // Private methods

    /** Get the day of week as enum value from string
     * @author Tyler
     * @param day The day of the week to match.
     */
    private static BusinessHour.DayOfWeek getDayOfWeek(String day) {
        BusinessHour.DayOfWeek dayOfWeek = BusinessHour.DayOfWeek.Monday; // default just in case
        for (BusinessHour.DayOfWeek d : BusinessHour.DayOfWeek.values()) {
            if (d.name().equals(day)) {
                dayOfWeek = d;
                break;
            }
        }
        return dayOfWeek;
    }

    /** Validate email using regex pattern
     * @author Tyler
     * @param email The email to validate.
     * @throws InvalidInputException Throws exception when email is invalid.
     */
    private static void validateEmail(String email) throws InvalidInputException {
        if (!matches("[A-Za-z0-9._+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", email)) throw new InvalidInputException("Invalid email");
    }

    // End private methods


    // Start public methods

    /** Create a new business in Flexibook after verifying arguments
     * @author Tyler
     * @param name The business name.
     * @param address The business address.
     * @param phoneNumber The business phone number.
     * @param email The business email.
     * @throws InvalidInputException Throws exception when user is not the owner.
     */
    public static void createBusiness(String name, String address, String phoneNumber, String email) throws InvalidInputException {

        // if user is owner
        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to set up business information");

        validateEmail(email);

        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        flexibook.setBusiness(new Business(name, address, phoneNumber, email, flexibook));
        try{
            FlexiBookPersistence.save(flexibook);
          } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
          }

    }

    /** Update info for an existing business in Flexibook
     * @author Tyler
     * @param name The business name to update.
     * @param address The business address to update.
     * @param phoneNumber The business phone number to update.
     * @param email The business email to update.
     * @throws InvalidInputException Throws exception when user is not the owner.
     */
    public static void updateBusiness(String name, String address, String phoneNumber, String email) throws InvalidInputException {

        // if user is owner
        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to update business information");
        validateEmail(email);

        Business business = FlexiBookApplication.getFlexibook().getBusiness();
        business.setName(name);
        business.setAddress(address);
        business.setPhoneNumber(phoneNumber);
        business.setEmail(email);
        try {
            FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		    FlexiBookPersistence.save(flexibook);
	    } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
        }

    }

    /** Transfer object for business
     * @author Tyler
     */
    public static TOBusiness getBusinessInfo() {
        Business business = FlexiBookApplication.getFlexibook().getBusiness();
        return new TOBusiness(business.getName(), business.getAddress(), business.getPhoneNumber(), business.getEmail());
    }

    /** Add business hours for business in Flexibook
     * @author Tyler
     * @param day The day on which to add the business hours.
     * @param newStartTime The new business hours' start time.
     * @param newEndTime The new business hours' end time.
     * @throws InvalidInputException Thrown when the user inputs do not meet the requirements set by the flexiBook application.
     */
    public static void addHours(String day, String newStartTime, String newEndTime) throws InvalidInputException {

        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to update business information");

        // Parse strings
        BusinessHour.DayOfWeek dayOfWeek = getDayOfWeek(day);
        Time startTime = Time.valueOf(newStartTime + ":00"); // to get format HH:mm:ss
        Time endTime = Time.valueOf(newEndTime + ":00");

        // check if end time is before start time
        if (startTime.compareTo(endTime) > 0) throw new InvalidInputException("Start time must be before end time");

        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        Business business = flexibook.getBusiness();
        List<BusinessHour> businessHours = business.getBusinessHours();

        // check if new hours overlap with current hours
        for (BusinessHour h : businessHours) {
            // if on same day
            if (h.getDayOfWeek().equals(dayOfWeek)) {
                // if new start time is before existing end time AND new end time is after existing start time
                if (startTime.compareTo(h.getEndTime()) < 0 && endTime.compareTo(h.getStartTime()) > 0)
                    throw new InvalidInputException("The business hours cannot overlap");
            }
        }

        // Passed checks, so add hours
        business.addBusinessHour(new BusinessHour(dayOfWeek, startTime, endTime, flexibook));
        try {
		    FlexiBookPersistence.save(flexibook);
	    } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }

    /** Update an existing business hours for business in Flexibook
     * @author Tyler
     * @param currentDay The day on which to modify the business hours.
     * @param currentStartTime The current start time of the business hours to modify.
     * @param newDay The new business hours' day.
     * @param newStartTime The new business hours' start time.
     * @param newEndTime The new business hours' end time.
     * @throws InvalidInputException Thrown when the user inputs do not meet the requirements set by the flexiBook application.
     */
    public static void updateHours(String currentDay, String currentStartTime, String newDay, String newStartTime, String newEndTime) throws InvalidInputException {

        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to update business information");

        // Parse strings
        BusinessHour.DayOfWeek currentDayOfWeek = getDayOfWeek(currentDay);
        BusinessHour.DayOfWeek newDayOfWeek = getDayOfWeek(newDay);
        Time cStartTime = Time.valueOf(currentStartTime + ":00"); // to get format HH:mm:ss
        Time startTime = Time.valueOf(newStartTime + ":00");
        Time endTime = Time.valueOf(newEndTime + ":00");

        // check if end time is before start time
        if (startTime.compareTo(endTime) > 0) throw new InvalidInputException("Start time must be before end time");

        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        Business business = flexibook.getBusiness();
        List<BusinessHour> businessHours = business.getBusinessHours();

        // check if new hours overlap with current hours
        for (BusinessHour h : businessHours) {
            // if on same day
            if (h.getDayOfWeek().equals(newDayOfWeek)) {
                // if new start time is before existing end time AND new end time is after existing start time
                if (startTime.compareTo(h.getEndTime()) < 0 && endTime.compareTo(h.getStartTime()) > 0)
                    // if time slot is the one that should be updated, update it
                    if (h.getDayOfWeek().equals(currentDayOfWeek) && h.getStartTime().equals(cStartTime)) {
                        h.setStartTime(startTime);
                        h.setEndTime(endTime);
                        break;
                    } else throw new InvalidInputException("The business hours cannot overlap");
            }
        }

        // got past validation, so update
        for (BusinessHour b : businessHours) {
            if (b.getDayOfWeek().equals(currentDayOfWeek) && b.getStartTime().equals(startTime)) {
                b.setDayOfWeek(newDayOfWeek);
                b.setStartTime(startTime);
                b.setEndTime(endTime);
                break;
            }
        }
        try {
		    FlexiBookPersistence.save(flexibook);
	    } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }

    /** Remove an existing business hours for business in Flexibook
     * @author Tyler
     * @param currentDay The current day for the business hours to remove.
     * @param currentStartTime The current start time for the business hours to remove.
     * @throws InvalidInputException Thrown when the user inputs do not meet the requirements set by the flexiBook application.
     */
    public static void removeHours(String currentDay, String currentStartTime) throws InvalidInputException {

        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to update business information");

        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        Business business = flexibook.getBusiness();
        List<BusinessHour> businessHours = business.getBusinessHours();

        // Parse strings
        BusinessHour.DayOfWeek dayOfWeek = getDayOfWeek(currentDay);
        Time startTime = Time.valueOf(currentStartTime + ":00"); // to get format HH:mm:ss

        for (BusinessHour b : businessHours) {
            if (b.getDayOfWeek().equals(dayOfWeek) && b.getStartTime().equals(startTime)) {
                flexibook.removeHour(b);
                break;
            }
        }
        try {
		    FlexiBookPersistence.save(flexibook);
	    } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }

    /** Get a list of a business's hours
     * @author Tyler
     * @return Returns a list of TOBusinessHour objects
     */
    public static ArrayList<TOBusinessHour> getBusinessHours() {
        ArrayList<TOBusinessHour> hours = new ArrayList<>();
        for (BusinessHour hour : FlexiBookApplication.getFlexibook().getHours()) {
            hours.add(new TOBusinessHour(hour.getDayOfWeek().toString(), hour.getStartTime().toString().substring(0,5), hour.getEndTime().toString().substring(0,5)));
        }
        return hours;
    }

    /** Add a new time slot to business in Flexibook
     * @author Tyler
     * @param type The type of time slot - either a holiday or a vacation.
     * @param startDateString The start date of the time slot
     * @param startTimeString The start time of the time slot
     * @param endDateString The end date of the time slot
     * @param endTimeString The end time of the time slot
     * @throws InvalidInputException Thrown when the user inputs do not meet the requirements set by the flexiBook application
     */
    public static void addTimeSlot(String type, String startDateString, String startTimeString, String endDateString, String endTimeString) throws InvalidInputException {

        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to update business information");

        LocalDateTime currentDate = LocalDateTime.parse(SystemTime.getSystemTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
        LocalDateTime parsedStartDate = LocalDateTime.parse(startDateString + "+" + startTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
        LocalDateTime parsedEndDate = LocalDateTime.parse(endDateString + "+" + endTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));

        // check if end time is before start time
        if (parsedStartDate.compareTo(parsedEndDate) > 0) throw new InvalidInputException("Start time must be before end time");

        // If date/time has passed
        if (parsedStartDate.compareTo(currentDate) < 0) {
            if (type.equals("vacation")) throw new InvalidInputException("Vacation cannot start in the past");
            else throw new InvalidInputException("Holiday cannot start in the past");
        }

        // Compare holidays and vacations
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        Business business = flexibook.getBusiness();
        List<TimeSlot> holidays = business.getHolidays();
        List<TimeSlot> vacations = business.getVacation();
        LocalDateTime testStartDate, testEndDate;

        // check overlap in holidays
        for (TimeSlot h : holidays) {
            // Note: h.getStartDate() + "+" + h.getStartTime() returns a string "yyyy-MM-dd+HH:mm:ss" including seconds
            testStartDate = LocalDateTime.parse(h.getStartDate() + "+" + h.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            testEndDate = LocalDateTime.parse(h.getEndDate() + "+" + h.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            // if new start date/time is before existing end date/time AND new end date/time is after existing start date/time
            if (parsedStartDate.compareTo(testEndDate) < 0 && parsedEndDate.compareTo(testStartDate) > 0) {
                if (type.equals("vacation")) throw new InvalidInputException("Holiday and vacation times cannot overlap");
                else throw new InvalidInputException("Holiday times cannot overlap");
            }
        }

        // check overlap in vacations
        for (TimeSlot v : vacations) {
            // Note: h.getStartDate() + "+" + h.getStartTime() returns a string "yyyy-MM-dd+HH:mm:ss" including seconds
            testStartDate = LocalDateTime.parse(v.getStartDate() + "+" + v.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            testEndDate = LocalDateTime.parse(v.getEndDate() + "+" + v.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            // if new start date/time is before existing end date/time AND new end date/time is after existing start date/time
            if (parsedStartDate.compareTo(testEndDate) < 0 && parsedEndDate.compareTo(testStartDate) > 0) {
                if (type.equals("vacation")) throw new InvalidInputException("Vacation times cannot overlap");
                else throw new InvalidInputException("Holiday and vacation times cannot overlap");
            }
        }

        // Passed checks, so assign Timeslot
        TimeSlot timeSlot = new TimeSlot(Date.valueOf(startDateString), Time.valueOf(startTimeString + ":00"), Date.valueOf(endDateString), Time.valueOf(endTimeString + ":00"), flexibook);
        if (type.equals("vacation")) business.addVacation(timeSlot);
        else business.addHoliday(timeSlot);
        
        try {
		    FlexiBookPersistence.save(flexibook);
	    } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
        }

    }

    /** Update an existing time slot to business in Flexibook
     * @author Tyler
     * @param type The type of time slot - either a holiday or a vacation.
     * @param timeSlotDate The original start date of the time slot
     * @param timeSlotTime The original start time of the time slot
     * @param newStartDate The start date of the updated time slot
     * @param newStartTime The start time of the updated time slot
     * @param newEndDate The end date of the time updated slot
     * @param newEndTime The end time of the time updated slot
     * @throws InvalidInputException Thrown when the user inputs do not meet the requirements set by the flexiBook application
     */
    public static void updateTimeSlot(String type, String timeSlotDate, String timeSlotTime, String newStartDate, String newStartTime, String newEndDate, String newEndTime) throws InvalidInputException {

        // if user is not owner
        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to update business information");

        LocalDateTime currentDate = LocalDateTime.parse(SystemTime.getSystemTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
        LocalDateTime parsedTimeSlotDate = LocalDateTime.parse(timeSlotDate + "+" + timeSlotTime, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
        LocalDateTime parsedStartDate = LocalDateTime.parse(newStartDate + "+" + newStartTime, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
        LocalDateTime parsedEndDate = LocalDateTime.parse(newEndDate + "+" + newEndTime, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));

        // check if end time is before start time
        if (parsedStartDate.compareTo(parsedEndDate) > 0) throw new InvalidInputException("Start time must be before end time");

        // If date/time has passed
        if (parsedStartDate.compareTo(currentDate) < 0) {
            if (type.equals("vacation")) throw new InvalidInputException("Vacation cannot start in the past");
            else throw new InvalidInputException("Holiday cannot be in the past");
        }

        // Compare holidays and vacations
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        Business business = flexibook.getBusiness();
        List<TimeSlot> holidays = business.getHolidays();
        List<TimeSlot> vacations = business.getVacation();
        LocalDateTime testStartDate, testEndDate;

        // check overlap in holidays
        for (TimeSlot h : holidays) {
            // Note: h.getStartDate() + "+" + h.getStartTime() returns a string "yyyy-MM-dd+HH:mm:ss" including seconds
            testStartDate = LocalDateTime.parse(h.getStartDate() + "+" + h.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            testEndDate = LocalDateTime.parse(h.getEndDate() + "+" + h.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            // if new start date/time is before existing end date/time AND new end date/time is after existing start date/time
            if (parsedStartDate.compareTo(testEndDate) < 0 && parsedEndDate.compareTo(testStartDate) > 0) {
                // if time slot is the one that should be updated, update it
                if (type.equals("holiday") && testStartDate.equals(parsedTimeSlotDate)) {
                    h.setStartDate(Date.valueOf(newStartDate));
                    h.setStartTime(Time.valueOf(newStartTime));
                    h.setEndDate(Date.valueOf(newEndDate));
                    h.setEndTime(Time.valueOf(newEndTime));
                    break;
                } else {
                    if (type.equals("vacation")) throw new InvalidInputException("Holiday and vacation times cannot overlap");
                    else throw new InvalidInputException("Holiday times cannot overlap");
                }
            }
        }

        // check overlap in vacations
        for (TimeSlot v : vacations) {
            // Note: h.getStartDate() + "+" + h.getStartTime() returns a string "yyyy-MM-dd+HH:mm:ss" including seconds
            testStartDate = LocalDateTime.parse(v.getStartDate() + "+" + v.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            testEndDate = LocalDateTime.parse(v.getEndDate() + "+" + v.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss"));
            // if new start date/time is before existing end date/time AND new end date/time is after existing start date/time
            if (parsedStartDate.compareTo(testEndDate) < 0 && parsedEndDate.compareTo(testStartDate) > 0) {
                // if time slot is the one that should be updated, update it
                if (type.equals("vacation") && testStartDate.equals(parsedTimeSlotDate)) {
                    v.setStartDate(Date.valueOf(newStartDate));
                    v.setStartTime(Time.valueOf(newStartTime));
                    v.setEndDate(Date.valueOf(newEndDate));
                    v.setEndTime(Time.valueOf(newEndTime));
                    break;
                } else {
                    if (type.equals("vacation")) throw new InvalidInputException("Vacation times cannot overlap");
                    else throw new InvalidInputException("Holiday and vacation times cannot overlap");
                }
            }
        }

        // got past validation, so update
        for (TimeSlot t : ((type.equals("vacation")) ? flexibook.getBusiness().getVacation() : flexibook.getBusiness().getHolidays())) {
            if (t.getStartDate().equals(Date.valueOf(timeSlotDate)) && t.getStartTime().equals(Time.valueOf(timeSlotTime + ":00"))) {
                t.setStartDate(Date.valueOf(newStartDate));
                t.setStartTime(Time.valueOf(newStartTime + ":00"));
                t.setEndDate(Date.valueOf(newEndDate));
                t.setEndTime(Time.valueOf(newEndTime + ":00"));
                break;
            }
        }
        
        try {
		    FlexiBookPersistence.save(flexibook);
	    } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
        }

    }

    /** Remove an existing time slot to business in Flexibook
     * @author Tyler
     * @param type The type of time slot - either a holiday or a vacation.
     * @param startDate The start date of the time slot to remove
     * @param startTime The start time of the time slot to remove
     * @throws InvalidInputException Thrown when the user inputs do not meet the requirements set by the flexiBook application
     */
    public static void removeTimeSlot(String type, String startDate, String startTime) throws InvalidInputException {

        // if user is not owner
        if (FlexiBookApplication.getCurrentUser() instanceof Customer) throw new InvalidInputException("No permission to update business information");

        // parse strings
        Date parsedStartDate = Date.valueOf(startDate);
        Time parsedStartTime = Time.valueOf(startTime + ":00");

        FlexiBook flexibook = FlexiBookApplication.getFlexibook();

        for (TimeSlot t : ((type.equals("vacation")) ? flexibook.getBusiness().getVacation() : flexibook.getBusiness().getHolidays())) {
            if (t.getStartDate().equals(parsedStartDate) && t.getStartTime().equals(parsedStartTime)) {
                flexibook.removeTimeSlot(t);
                break;
            }
        }
        try {
		    FlexiBookPersistence.save(flexibook);
	    } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
        }

    }


}
