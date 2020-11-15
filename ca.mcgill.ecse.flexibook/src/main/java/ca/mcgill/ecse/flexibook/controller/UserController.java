package ca.mcgill.ecse.flexibook.controller;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;
import java.sql.Time;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.model.*;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;


/**
 * @author David Whiteside
 *
 * Class handles the Login and Logout of the current user for the application. The class also allows the current user to view the appointment calendar.
 * */
public class UserController {

    private static String ownerName = "owner";

    public UserController() {

    }

    /** Sets the current user for the flexibook application to be the user logging in.
     * @param username Username of the account the client is attempting to log into.
     * @param password Password of the account the client is attempting to log into.
     * @throws InvalidInputException Throws an error if the account does not exist, if the password is incorrect, or if someone is already logged in.
     */
    public static void login(String username, String password) throws InvalidInputException {
        String error = "";
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();

        if (username.equals(ownerName) && password.equals(ownerName) && !flexibook.hasOwner()) {
            Owner newOwner = new Owner(username, password, flexibook);
            flexibook.setOwner(newOwner);
            FlexiBookApplication.setCurrentUser(newOwner);
            return;
        }

        if (!User.hasWithUsername(username)) {
            error += "Username/password not found";
        } else if (!password.equals(User.getWithUsername(username).getPassword())) {
            error += "Username/password not found";
        }

        if (error.length() > 0) {
            throw new InvalidInputException(error.trim());
        }

        try {
            User loggedInUser = User.getWithUsername(username);
            String currentUserPassword = loggedInUser.getPassword();
            FlexiBookApplication.setCurrentUser(loggedInUser);
            FlexiBookPersistence.save(flexibook);
        } catch (RuntimeException e) {
            if (e.getMessage().trim().equals("Cannot create due to duplicate name. See http://manual.umple.org?RE003ViolationofUniqueness.html")) {
                error = "A user is currently logged into the application";
            }
            throw new InvalidInputException(error.trim());
        }
    }

    /**Method to logout.
     * @throws InvalidInputException Logs out the current user by setting the current user for the flexibook instance to be null. Throws an exception if no one is currently logged in.
     */
    public static void logout() throws InvalidInputException {
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        if (FlexiBookApplication.getCurrentUser() != null) {
            FlexiBookApplication.setCurrentUser(null);
            FlexiBookPersistence.save(flexibook);
        } else {
            throw new InvalidInputException(("The user is already logged out").trim());
        }
    }

    /**This function creates and returns an arraylist of available time slots relative to the viewType.
     * @param viewType - an indicator of either week or day to be viewed
     * @param aStartDate - the starting date for querying;if a week its a sunday
     * @throws InvalidInputException Throws an error if the date is invalid
     */
    public static List<TimeSlot> viewAppointments(String viewType, String aStartDate) throws InvalidInputException {
        String error = "";
        Date beginningDate = null;
        try {
            beginningDate = Date.valueOf(aStartDate);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException((aStartDate + " is not a valid date.").trim());
        }
        if (beginningDate == null){
            throw new InvalidInputException((aStartDate + " is not a valid date.").trim());
        }
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        Calendar cal = Calendar.getInstance();
        cal.setTime(beginningDate);

        //day = 1 corresponds to first day of month, following code is to get it to match with enums
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        int month = cal.get(Calendar.MONTH);

        List<BusinessHour> businesshours = flexibook.getHours();//need more visibility into this
        List<Appointment> allAppointments = flexibook.getAppointments();//flexibook.getAppointments();
        List<Appointment> currentAppointments = new ArrayList<Appointment>();
        List<TimeSlot> unavailableTimeSlots = new ArrayList<TimeSlot>();
        List<TimeSlot> availableTimeSlots = new ArrayList<TimeSlot>();


        switch (viewType) {
            case "day":
                BusinessHour currentDayHours = flexibook.getBusiness().getBusinessHour(weekDay - 2);
                Time dayStartTime = currentDayHours.getStartTime();
                Time dayEndTime = currentDayHours.getEndTime();

                for (int i = 0; i < allAppointments.size(); i++) {
                    TimeSlot currentSlot = allAppointments.get(i).getTimeSlot();
                    Date startDate = currentSlot.getStartDate();
                    cal.setTime(startDate);
                    if (cal.get(Calendar.DAY_OF_WEEK) == weekDay && cal.get(Calendar.MONTH) == month) {
                        if (i == 0) {
                            availableTimeSlots.add(new TimeSlot(startDate, dayStartTime, startDate, allAppointments.get(i).getTimeSlot().getStartTime(), flexibook));
                        } else {
                            availableTimeSlots.add(new TimeSlot(startDate, allAppointments.get(i - 1).getTimeSlot().getEndTime(), startDate, allAppointments.get(i).getTimeSlot().getStartTime(), flexibook));
                        }
                    } else {
                        availableTimeSlots.add(new TimeSlot(startDate, allAppointments.get(i - 1).getTimeSlot().getEndTime(), startDate, dayEndTime, flexibook));
                    }
                }
                for (Appointment temp : allAppointments) {
                    TimeSlot currentSlot = temp.getTimeSlot();
                    Date startDate = currentSlot.getStartDate();
                    cal.setTime(startDate);
                    if (cal.get(Calendar.DAY_OF_WEEK) == weekDay && cal.get(Calendar.MONTH) == month) {
                        BookableService serviceForAppointment = temp.getBookableService();
                        if (serviceForAppointment.getClass() == ServiceCombo.class) {
                            List<ComboItem> services = ((ServiceCombo) serviceForAppointment).getServices();
                            for (ComboItem service : services) {
                                if (service.getService().getDowntimeDuration() > 0) {
                                    Time temp1 = currentSlot.getStartTime();
                                    cal.setTime(temp1);
                                    cal.add(Calendar.MINUTE, service.getService().getDowntimeStart());
                                    Time startTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                    cal.setTime(currentSlot.getStartTime());
                                    cal.add(Calendar.MINUTE, service.getService().getDuration());
                                    Time endTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                    availableTimeSlots.add(new TimeSlot(startDate, startTime, startDate, endTime, flexibook));
                                }
                            }
                        } else {
                            Service currentService = (Service) serviceForAppointment;
                            if (currentService.getDowntimeStart() > 0) {
                                cal.setTime(currentSlot.getStartTime());
                                cal.add(Calendar.MINUTE, currentService.getDowntimeStart());
                                Time startTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                cal.setTime(currentSlot.getStartTime());
                                cal.add(Calendar.MINUTE, currentService.getDuration());
                                Time endTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                availableTimeSlots.add(new TimeSlot(startDate, startTime, startDate, endTime, flexibook));
                            }
                        }
                    }
                }
                break;
            case "week":
                //for loop only needs to go through the work week
                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(beginningDate);
                cal.add(Calendar.DAY_OF_MONTH,1);
                for (int i = 1; i <= businesshours.size(); i++) {
                    weekDay = i;
                    tempCal.add(Calendar.DAY_OF_MONTH,weekDay);
                    BusinessHour currentDayHours2 = flexibook.getBusiness().getBusinessHour(0);
                    Time dayStartTime2 = currentDayHours2.getStartTime();
                    Time dayEndTime2 = currentDayHours2.getEndTime();

                    for (int k = 0; k < allAppointments.size(); k++) {
                        TimeSlot currentSlot = allAppointments.get(k).getTimeSlot();
                        Date startDate = currentSlot.getStartDate();
                        cal.setTime(startDate);
                        if (cal.get(Calendar.MONTH) == tempCal.get(Calendar.MONTH)&&cal.get(Calendar.DAY_OF_MONTH) == tempCal.get(Calendar.DAY_OF_MONTH)) {
                            if (k == 0) {
                                availableTimeSlots.add(new TimeSlot(startDate, dayStartTime2, startDate, allAppointments.get(k).getTimeSlot().getStartTime(), flexibook));
                            } else {
                                availableTimeSlots.add(new TimeSlot(startDate, allAppointments.get(k - 1).getTimeSlot().getEndTime(), startDate, allAppointments.get(k).getTimeSlot().getStartTime(), flexibook));
                            }
                        } else {
                            availableTimeSlots.add(new TimeSlot(startDate, allAppointments.get(k - 1).getTimeSlot().getEndTime(), startDate, dayEndTime2, flexibook));
                        }
                    }
                    for (Appointment temp : allAppointments) {
                        TimeSlot currentSlot = temp.getTimeSlot();
                        Date startDate = currentSlot.getStartDate();
                        cal.setTime(startDate);
                        if (cal.get(Calendar.MONTH) == tempCal.get(Calendar.MONTH)&&cal.get(Calendar.DAY_OF_MONTH) == tempCal.get(Calendar.DAY_OF_MONTH)) {
                            BookableService serviceForAppointment = temp.getBookableService();
                            if (serviceForAppointment.getClass() == ServiceCombo.class) {
                                List<ComboItem> services = ((ServiceCombo) serviceForAppointment).getServices();
                                for (ComboItem service : services) {
                                    if (service.getService().getDowntimeDuration() > 0) {
                                        Time temp1 = currentSlot.getStartTime();
                                        cal.setTime(temp1);
                                        cal.add(Calendar.MINUTE, service.getService().getDowntimeStart());
                                        Time startTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                        cal.setTime(currentSlot.getStartTime());
                                        cal.add(Calendar.MINUTE, service.getService().getDuration());
                                        Time endTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                        availableTimeSlots.add(new TimeSlot(startDate, startTime, startDate, endTime, flexibook));
                                    }
                                }
                            } else {
                                Service currentService = (Service) serviceForAppointment;
                                if (currentService.getDowntimeStart() > 0) {
                                    cal.setTime(currentSlot.getStartTime());
                                    cal.add(Calendar.MINUTE, currentService.getDowntimeStart());
                                    Time startTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                    cal.setTime(currentSlot.getStartTime());
                                    cal.add(Calendar.MINUTE, currentService.getDuration());
                                    Time endTime = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0);
                                    availableTimeSlots.add(new TimeSlot(startDate, startTime, startDate, endTime, flexibook));
                                }
                            }
                        }
                    }
                    break;
                }


        }
        return availableTimeSlots;

    }
}
