package ca.mcgill.ecse.flexibook.controller;

import java.sql.Array;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.model.Appointment;
import ca.mcgill.ecse.flexibook.model.BookableService;
import ca.mcgill.ecse.flexibook.model.BusinessHour;
import ca.mcgill.ecse.flexibook.model.ComboItem;
import ca.mcgill.ecse.flexibook.model.Customer;
import ca.mcgill.ecse.flexibook.model.FlexiBook;
import ca.mcgill.ecse.flexibook.model.Owner;
import ca.mcgill.ecse.flexibook.model.Service;
import ca.mcgill.ecse.flexibook.model.ServiceCombo;
import ca.mcgill.ecse.flexibook.model.TimeSlot;
import ca.mcgill.ecse.flexibook.model.User;
import ca.mcgill.ecse.flexibook.util.SystemTime;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;


/**
 * Appointment Controller
 *
 * @author Sandy
 */
public class AppointmentController {

    /**
     * Method that allows a customer to schedule an appointment.
     *
     * @param startDateString Start date of the desired appointment (String).
     * @param serviceName Name of the bookable service (String).
     * @param optionalServices List of desired optional services for a Service Combo (doesn't include mandatory ones) (String).
     * @param startTimeString Start Time of the desired appointment.
     * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the FlexiBook application.
     * @author Sandy
     */
    public static void makeAppointment(String startDateString, String serviceName, List<String> optionalServices, String startTimeString) throws InvalidInputException {
        FlexiBook flexibook = FlexiBookApplication.getFlexibook(); //getFlexibook
        User currentUser = FlexiBookApplication.getCurrentUser(); // get current user of app
        Date startDate = Date.valueOf(startDateString);  //String -> Date

        Time startTime = Time.valueOf(startTimeString + ":00");    //String -> Time  //time must be in format HH:MM:00

        //1) Check user

        //User = Owner
        if (currentUser instanceof Owner) {
            throw new InvalidInputException("An owner cannot make an appointment");
        }

        //User = Customer
        Customer customer = (Customer) currentUser;

        //2) Check if business exists

        //CASE: Business doesn't exist
        if (flexibook.getBusiness() == null) {
            throw new InvalidInputException("The business does not exist.");
        }

        //3) Check DATE
        //GET CURRENT DATE/TIME
        Calendar currDay = SystemTime.getSystemCalendar();
        Calendar desiredAppointmentCalendar = dateTime2Calendar(startDate, startTime);
        if (desiredAppointmentCalendar.before(currDay)) {
            System.out.println("There are no available slots for " + serviceName + " on " + startDateString + " at " + startTimeString);
            throw new InvalidInputException("There are no available slots for " + serviceName + " on " + startDateString + " at " + startTimeString);
        }

        //4) BookableService
        BookableService bookableService = findBookableService(serviceName); //search for BookableService with serviceName

        if (bookableService == null) {
            throw new InvalidInputException(serviceName + " is not a BookableService");
        }
        //5) TimeSlot
        TimeSlot timeSlot = null;
        Date endDate = startDate; //the end date is always the same as startDate

        //CASE 5A: 1 single SERVICE
        if (bookableService instanceof Service) {
            Service service = (Service) bookableService;
            int addDuration = service.getDuration();
            Time endTime = sumOfTime(startDate, startTime, addDuration); // calculate "endTime = startTime+addDuration" message to myself:sumOfTime method has been tested SumOfTime.java class
            timeSlot = new TimeSlot(startDate, startTime, endDate, endTime, flexibook);  //create TimeSlot
            //check if timeslot is NOT on holidays, vacations and closing hours an no overlaps with other appointment
            if (isBookable(timeSlot, bookableService, false, null)) {
                //no overlaps, no conflict
                //6) add appointment to FlexiBook
                flexibook.addAppointment(customer, service, timeSlot);
            } else {
                throw new InvalidInputException("There are no available slots for " + serviceName + " on " + startDateString + " at " + startTimeString);
            }
        }

        // 5B) SERVICE COMBO
        // Chosen combo items must be in the booked service combo
        if (bookableService instanceof ServiceCombo) {
            ServiceCombo existingServiceCombo = findServiceCombo(serviceName);
            if (findServiceCombo(serviceName) == null) {
                throw new InvalidInputException(bookableService + " is not found.");
            }

            ServiceCombo myServiceCombo = (ServiceCombo) bookableService;
			/* Customer doesn't need to mention mandatory, only specifies optionalServices (chosenItems). It is implied: mandatory automatically added for appointment.
			So need to go through all comboItems of existing combo. */
            int totalDuration = 0;
            ArrayList<ComboItem> comboItem2Add = new ArrayList<ComboItem>();
            //Iterate through list of existingServiceCombo, ADD to comboItem2Add: mandatory|| optionalServices chosen by customer
            List<ComboItem> existingComboItems = existingServiceCombo.getServices();//get list of EXISTING comboItems from existing service combo
            //loop through list of comboItems from the existing ServiceCombo.
            /**** Add mandatory services ****/
            for (ComboItem currExistingComboItem : existingComboItems) { //The comboItems are ordered so need to loop through comboItems first
                String currExistingComboItemStr = currExistingComboItem.getService().getName(); //get the name of service of existing comboItem
                //currExistingComboItem part of optional services (chosen by customer || currExistingComboItem = mandatory ===> ADD
                if (currExistingComboItem.getMandatory() == true) {
                    //found matching combo item
                    ComboItem itemToBeAdded = currExistingComboItem;
                    comboItem2Add.add(itemToBeAdded);//add matching comboItem into our comboItem2Add list
                    int duration = currExistingComboItem.getService().getDuration();//get duration of this item
                    totalDuration += duration; // update total duration
                    //break; //same serviceName, go out to check if another comboItem is part of the chosen optionalService
                }
            }

            /**ADD OPTIONAL SERVICES IF APPLIED***/
            if (optionalServices != null) {
                //CASE: chosen optionalServices ARE NOT from existingServiceCombo
                if (!(isValidCombo(existingServiceCombo, optionalServices))) {  //optionalServices = chosen by customer
                    throw new InvalidInputException("At least one of the chosen combo items are not from the booked service combo.");
                }
                for (ComboItem currExistingComboItem : existingComboItems) { //The comboItems are ordered so need to loop through comboItems first
                    String currExistingComboItemStr = currExistingComboItem.getService().getName(); //get the name of service of existing comboItem
                    //currExistingComboItem part of optional services (chosen by customer || currExistingComboItem = mandatory ===> ADD
                    if (optionalServices.contains(currExistingComboItemStr)) {
                        //found matching combo item
                        ComboItem itemToBeAdded = currExistingComboItem;
                        comboItem2Add.add(itemToBeAdded);//add matching comboItem into our comboItem2Add list
                        int duration = currExistingComboItem.getService().getDuration();//get duration of this item
                        totalDuration += duration; // update total duration
                        //break; //same serviceName, go out to check if another comboItem is part of the chosen optionalService
                    }
                }
            }
            //after adding all optionalServices into comboItem2Add

            //TIMESLOT
            //calculate endTime
            Time endTime = sumOfTime(startDate, startTime, totalDuration); // "endTime = startTime+totalDuration"
            //create TimeSlot
            timeSlot = new TimeSlot(startDate, startTime, endDate, endTime, flexibook);  //create TimeSlot
            //check if timeslot is NOT on holidays, vacations and closing hours an no overlaps with other appointment
            if (isBookable(timeSlot, bookableService, false, null)) {
                //no overlaps, no conflict
                //6) add appointment to FlexiBook
                Appointment appointment = new Appointment(customer, myServiceCombo, timeSlot, flexibook);
                flexibook.addAppointment(appointment);
                for (ComboItem comboItem : comboItem2Add) {
                    appointment.addChosenItem(comboItem);
                }
            } else {
                throw new InvalidInputException("There are no available slots for " + serviceName + " on " + startDateString + " at " + startTimeString);
            }
        }
	try {
		FlexiBookPersistence.save(flexibook);
	} catch (RuntimeException e) {
        	throw new InvalidInputException(e.getMessage());
    	}
    }

    /** This method allows the customer to update his/her appointment. The customer may update his/her appointment to various time slots.
     * The customer may update her appointment up until the day before the appointment date.
     * If the appointment was a ServiceCombo, the customer can edit the optional combo items (add/remove).
     * @param bookingServiceName Name of the bookable service of the old appointment that the customer wants to update.
     * @param oldStartDateString Date of the old appointment that the customer wants to update(String).
     * @param oldStartTimeString Time of the old appointment that the customer wants to update(String).
     * @param newStartDateString Desired new start Date for the old appointment that the customer wants to update (String).
     * @param newStartTimeString Desired new start Time for the old appointment that the customer wants to update (String).
     * @param addOptionalServices List of optional services that the customer wants to add to the old appointment (List<String>).
     * @param removeOptionalServices List of optional services that the customer wants to remove to the old appointment (List<String>).
     * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the FlexiBook application.
     */
    public static void updateAppointment(String bookingServiceName, String oldStartDateString, String oldStartTimeString, String newStartDateString, String newStartTimeString, List<String> addOptionalServices, List<String> removeOptionalServices) throws InvalidInputException {
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        User currentUser = FlexiBookApplication.getCurrentUser();
        String currUserName = currentUser.getUsername();

        Date oldStartDate = Date.valueOf(oldStartDateString);  //String -> Date
        Time oldStartTime = java.sql.Time.valueOf(oldStartTimeString + ":00");    //String -> Time

        Date newStartDate = Date.valueOf(newStartDateString);  //String -> Date
        Time newStartTime = java.sql.Time.valueOf(newStartTimeString + ":00");    //String -> Time

        //1) Check user

        //CASE 1A: User = Owner
        if (currentUser instanceof Owner) {
            throw new InvalidInputException("Error: An owner cannot update a customer's appointment");
        }
        //CASE 1B: User = Customer
        Customer customer = (Customer) currentUser;
        Appointment oldAppointment = findAppointment(bookingServiceName, oldStartDate, oldStartTime);//find old appointment
        TimeSlot oldTimeSlot = oldAppointment.getTimeSlot();

        //CASE: User = another customer
        if (!(currUserName.equals(oldAppointment.getCustomer().getUsername()))) {  //compare usernames
            throw new InvalidInputException("Error: A customer can only update their own appointments");
        }

        //The customer may update her appointment up until the day before the appointment date.
        //2) Check if TODAY is one day before the appointment date.


        Date newEndDate = newStartDate; // end date is same as start date (appointment is on same day)
        //3) Check type of BOOKINGSERVICE
        BookableService bookableService = oldAppointment.getBookableService(); //get the bookable service

        //CASE 3A: 1 single Service
        //can only change the start time or/and start date
        if (bookableService instanceof Service) {
            Service service = (Service) bookableService;
            int duration = service.getDuration();
            Time newEndTime = sumOfTime(newStartDate, newStartTime, duration);    //calculate new endTime by adding duration to startTime
            TimeSlot newTimeSlot = new TimeSlot(newStartDate, newStartTime, newEndDate, newEndTime, flexibook);
            //4) Check if newStartTime and newDate can be fit into schedule
            if (!oldAppointment.updateAppointment(bookableService, oldTimeSlot, newTimeSlot, null, null)) {
                throw new InvalidInputException("There are no available slots for " + bookingServiceName + " on " + newStartDateString + " at " + newStartTimeString);
            }
            //5) Set old appointment with new time slot
        }
        //CASE 3B: ServiceCombo     //can change startTime/startDate and/or add/remove optionalServices BUT NOT remove mandatory
        if (bookableService instanceof ServiceCombo) {
            ServiceCombo serviceCombo = (ServiceCombo) bookableService; //serviceCombo from old appointment
            int existingTotalDuration = getTotalDurationExistingAppt(oldAppointment); //duration of existing ServiceCombo
            //CASE 3B-1: Change startTime or/and startDate
            //if startTime != oldStartTime OR startDate != oldStartDate ==> there's a change of time and/or date
            // !A ||!B
            if ((!newStartTime.equals(oldAppointment.getTimeSlot().getStartTime())) || !newStartDate.equals(oldAppointment.getTimeSlot().getStartDate())) {
                Time newEndTime = sumOfTime(newStartDate, newStartTime, existingTotalDuration);
                TimeSlot newTimeSlot = new TimeSlot(newStartDate, newStartTime, newEndDate, newEndTime, flexibook);
                //4) Check if newStartTime and newDate can be fit into schedule
                if (!oldAppointment.updateAppointment(bookableService, oldTimeSlot, newTimeSlot, null, null)) {
                    throw new InvalidInputException("There are no available slots for " + bookingServiceName + " on " + newStartDateString + " at " + newStartTimeString);
                }
                //5) Set old appointment with updated time slot
            }
            //CASE 3B-2: add/remove optionalServices
            List<ComboItem> oldChosenItemsList = oldAppointment.getChosenItems();
            ArrayList<ComboItem> chosenItem2remove = new ArrayList<>();
            int newTotalDuration = getTotalDurationExistingAppt(oldAppointment);
            //REMOVE
            if (removeOptionalServices != null) {
                for (String removeOptionalService : removeOptionalServices) {
                    for (ComboItem oldChosenItem : oldChosenItemsList) {    //go through each comboItem of list
                        String oldServiceName = oldChosenItem.getService().getName();
                        if (oldServiceName.equals(removeOptionalService)) {  //if same name = same service
                            //check if the removeOptionalService is mandatory, if yes, CAN'T remove
                            if (oldChosenItem.getMandatory() == false) {
                                newTotalDuration -= oldChosenItem.getService().getDuration();
                                chosenItem2remove.add(oldChosenItem);
                                break;
                            } else {
                                throw new InvalidInputException("Cannot remove mandatory service");
                            }
                        }
                    }
                }

                //calculate new TIMESLOT
                Time newEndTime = sumOfTime(newStartDate, newStartTime, newTotalDuration);
                TimeSlot newTimeSlot = new TimeSlot(newStartDate, newStartTime, newEndDate, newEndTime, flexibook);
                //5) Set old appointment with new time slot
                oldAppointment.updateAppointment(bookableService, oldTimeSlot, newTimeSlot, null, chosenItem2remove);
            }
            //create another copy of ServiceCombo in case the serviceCombo can't be fit into schedule, we don't want to modify the existing serviceCombo
            //it also the updated version of ServiceCombo

            //ADD
            if (addOptionalServices != null) {
                //CASE: add service does not fit in available slot
                ArrayList<ComboItem> itemsToAdd = new ArrayList<ComboItem>();
                //loop through list of comboItems from the existing ServiceCombo. //The comboItems are ordered so need to loop through comboItems first
                for (ComboItem currComboItem : serviceCombo.getServices()) { //ORGINAL
                    String currComboService = currComboItem.getService().getName(); //get the name of service of existing comboItem
                    //loop through list of optionalServices to be added
                    for (String addOptionalService : addOptionalServices) { //if same name = same service
                        if (currComboService.equals(addOptionalService)) { //compare String names
                            ComboItem item2BeAdded = currComboItem;
                            int addDuration = currComboItem.getService().getDuration(); //duration to add if we add existingComboItem
                            newTotalDuration += addDuration;
                            itemsToAdd.add(item2BeAdded);//add matching comboItem into our serviceCombo
                            break; //same serviceName, go out to check if another comboItem is part of addOptionalServices
                        }
                    }
                }
                Time newEndTime = sumOfTime(newStartDate, newStartTime, newTotalDuration);    //update new end time
                TimeSlot newTimeSlot = new TimeSlot(newStartDate, newStartTime, newEndDate, newEndTime, flexibook);

                //4) Check if newStartTime and newDate can be fit into schedule
                if (!oldAppointment.updateAppointment(bookableService, oldTimeSlot, newTimeSlot, itemsToAdd, null)) {
                    throw new InvalidInputException("additional extensions service does not fit in available slot");
                }
            }
        }
	try {
		FlexiBookPersistence.save(flexibook);
	} catch (RuntimeException e) {
        	throw new InvalidInputException(e.getMessage());
    	}
    }

    /**
     * Method that calculates the totalDuration of an appointment.
     * @param oldAppointment Exiting appointment.
     * @return totalDurationExistingAppt: Total duration of the existing appointment.
     * @author Sandy
     */
    private static int getTotalDurationExistingAppt(Appointment oldAppointment) {
        int totalDurationExistingAppt = 0;
        List<ComboItem> chosenItems = oldAppointment.getChosenItems(); //chosenItems = mandatory + optionalServices chosen by customer
        for (ComboItem chosenItem : chosenItems) {
            totalDurationExistingAppt += chosenItem.getService().getDuration();
        }
        return totalDurationExistingAppt;
    }

    /**
     * This method merge java.sql.Date and java.sql.Time into java.util.Calendar. A calendar with the right Date and Time is returned.
     *
     * @param date Date of format java.sql.Date.
     * @param time Time of java.sql.Time .
     * @return cal Calendar of java.util.Calendar.
     * @author Sandy
     */
    private static Calendar dateTime2Calendar(Date date, Time time) {
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

    /**
     * Method that allows a customer to cancel his/her appointment. The appointment time slot becomes available for other customers.
     *
     * @param bookingServiceName Name of the bookable service of the appointment that the customer wants to cancel.
     * @param oldStartDateString Start date of the appointment that the customer wants to cancel.
     * @param oldStartTimeString Start time of the appointment that the customer wants to cancel.
     * @throws InvalidInputException: Is thrown when the provided user inputs do not meet the requirements set by the FlexiBook application.
     * @author Sandy
     */
    public static void cancelAppointment(String bookingServiceName, String oldStartDateString, String oldStartTimeString) throws InvalidInputException {
        FlexiBook flexibook = FlexiBookApplication.getFlexibook(); //getFlexibook
        User currentUser = FlexiBookApplication.getCurrentUser(); // get current user of app
        String currUserName = currentUser.getUsername();

        Date oldStartDate = Date.valueOf(oldStartDateString);  //String -> Date
        Time oldStartTime = java.sql.Time.valueOf(oldStartTimeString);    //String -> Time

        //1) Check user

        //CASE 1A: User = Owner
        if (currentUser instanceof Owner) {
            throw new InvalidInputException("An owner cannot cancel an appointment");
        }
        //CASE 1B: User = Customer
        Appointment oldAppointment = findAppointment(bookingServiceName, oldStartDate, oldStartTime);//find old appointment
        //CASE: User = another customer
        Customer c = oldAppointment.getCustomer();
        String oldAppointmentCustomer = c.getUsername();
        if (!(currUserName.equals(oldAppointmentCustomer))) {  //compare usernames
            throw new InvalidInputException("A customer can only cancel their own appointments");
        }

        // The customer may update her appointment up until the day before the appointment date. --> state machine deletes appointment
        if (!oldAppointment.cancelAppointment(findBookableService(bookingServiceName), oldAppointment.getTimeSlot()))
            throw new InvalidInputException("Cannot cancel an appointment on the appointment date");

        if(oldAppointment!=null){
            //flexibook.removeAppointment(oldAppointment);
            oldAppointment.delete();
        }
	try {
		FlexiBookPersistence.save(flexibook);
	} catch (RuntimeException e) {
        	throw new InvalidInputException(e.getMessage());
    	}
    }

    /**
     * Method that allows an owner to cancel an appointment for no show.
     *
     * @param bookingServiceName Name of the bookable service of the appointment that the customer wants to cancel.
     * @param oldStartDateString Start date of the appointment that the customer wants to cancel.
     * @param oldStartTimeString Start time of the appointment that the customer wants to cancel.
     * @throws InvalidInputException: Is thrown when the provided user inputs do not meet the requirements set by the FlexiBook application.
     * @author Sandy
     */
    public static void cancelAppointmentNoShow(String bookingServiceName, String oldStartDateString, String oldStartTimeString) throws InvalidInputException {
        FlexiBook flexibook = FlexiBookApplication.getFlexibook(); //getFlexibook
        User currentUser = FlexiBookApplication.getCurrentUser(); // get current user of app
        String currUserName = currentUser.getUsername();

        Date oldStartDate = Date.valueOf(oldStartDateString);  //String -> Date
        Time oldStartTime = java.sql.Time.valueOf(oldStartTimeString);    //String -> Time

        //1) Check user

        //CASE 1A: User = Customer
        if (currentUser instanceof Customer) {
            throw new InvalidInputException("An customer cannot cancel an appointment for no show.");
        }

        //CASE 1B: User = Owner
        Appointment oldAppointment = findAppointment(bookingServiceName, oldStartDate, oldStartTime); //find old appointment

        //CASE: User = another customer
        Customer c = oldAppointment.getCustomer();
        String oldAppointmentCustomer = c.getUsername();
        BookableService bookingService = oldAppointment.getBookableService();
        //(Date aStartDate, Time aStartTime, Date aEndDate, Time aEndTime, FlexiBook aFlexiBook)
        oldAppointment.cancelForNoShow(bookingService,oldAppointment.getTimeSlot());
	try {
		FlexiBookPersistence.save(flexibook);
	} catch (RuntimeException e) {
        	throw new InvalidInputException(e.getMessage());
    	}
    }

    /**Transfer objects for Appointment.
     * Get all appointments from flexibook. Returns a list of appointments form the flexibook (List<TOAppointment).
     * @return appointments
     * @author Sandy
     */
    public static List<TOAppointment> getAppointments() {
        ArrayList<TOAppointment> appointments = new ArrayList<TOAppointment>();

        for (Appointment appointment : FlexiBookApplication.getFlexibook().getAppointments()) {
            String username = appointment.getCustomer().getUsername();
            String bookableServiceName = appointment.getBookableService().getName();
            String startDate = appointment.getTimeSlot().getStartDate().toString();
            String startTime = appointment.getTimeSlot().getStartTime().toString();

            TOAppointment toAppointment = new TOAppointment(username, bookableServiceName, startDate, startTime);
            BookableService bookableService = appointment.getBookableService();

            if (bookableService instanceof ServiceCombo) {  //ServiceCombo has optionalServices
                List<ComboItem> optionalServices = appointment.getChosenItems();
                for (ComboItem optionalService : optionalServices) {
                    toAppointment.addService(optionalService.getService().getName()); //add String services including madatory
                }
            }
            appointments.add(toAppointment);  //add toAppointment to a list of TOAppointment
        }
        return appointments;
    }

    /**
     * Get all appointments from a customerUsername.
     * @param customerUsername Username of the customer.
     * @return appointments List of appointments from the customer.
     * @author Sandy
     */
    public static List<TOAppointment> getAppointments(String customerUsername) {
        ArrayList<TOAppointment> appointments = new ArrayList<TOAppointment>();
        for (Appointment appointment : FlexiBookApplication.getFlexibook().getAppointments()) {
            String username = appointment.getCustomer().getUsername();

            if (username.equals(customerUsername)) {
                String serviceName = appointment.getBookableService().getName();
                String startDate = appointment.getTimeSlot().getStartDate().toString();
                String startTime = appointment.getTimeSlot().getStartTime().toString();
                TOAppointment toAppointment = new TOAppointment(username, serviceName, startDate, startTime);
                BookableService bookableService = appointment.getBookableService();

                if (bookableService instanceof ServiceCombo) {  //ServiceCombo has optionalServices
                    List<ComboItem> optionalServices = appointment.getChosenItems();
                    for (ComboItem optionalService : optionalServices) {
                        toAppointment.addService(optionalService.getService().getName()); //add String services including mandatory ones
                    }
                }
                appointments.add(toAppointment);
            }
        }
        return appointments;
    }

    /**
     * Method that calculates the total duration of a serviceCombo. It iterates through the list of services from serviceCombo.
     *
     * @param serviceCombo Service Combo that we want to calculate the total duration.
     * @return totalDuration Total duration of the service combo.
     * @author Sandy
     */
    private static int getServiceComboDuration(ServiceCombo serviceCombo) {
        int totalDuration = 0;
        List<ComboItem> comboItems = serviceCombo.getServices(); //get list of services from the comboItems
        for (ComboItem currComboItem : comboItems) {
            int duration = currComboItem.getService().getDuration(); //get duration of this item
            totalDuration += duration; // update total duration
        }
        return totalDuration;
    }

    /**
     * Method that finds a appointment with the bookingServiceName, OldStartDate and OldStartTime.
     * date must be java.sql.Date
     * time must be java.sql.Time
     * To convert:
     * --String -> Date
     * Date startDate = Date.valueOf(startDateString);
     * --String -> Time
     * Time startTime = Time.valueOf(startTimeString);
     *
     * @param bookingServiceName Name of the bookable service
     * @param oldStartDate Date of the appointment to find (java.sql.Date)
     * @param oldStartTime Time of the appointment to find (java.sql.Time)
     * @return foundAppointment Matching appointment
     * @author Sandy
     */
    public static Appointment findAppointment(String bookingServiceName, Date oldStartDate, Time oldStartTime) {
        Appointment foundAppointment = null;
        //Iterate through list of appointments of the flexibook
        for (Appointment currOldAppointment : FlexiBookApplication.getFlexibook().getAppointments()) {
            //get Date of current old appointment
            Date dateOld = currOldAppointment.getTimeSlot().getStartDate();
            String dateOldstr = dateOld.toString();
            Time timeOld = currOldAppointment.getTimeSlot().getStartTime();
            String timeOldStr = timeOld.toString();
            String oldStartTimeStr = oldStartTime.toString();
            //get name booking service  of current old appointment
            String oldStartDateStr = oldStartDate.toString();
            String oldBookingServiceName = currOldAppointment.getBookableService().getName();
            //Check if Date,Time, name same => found appointment
            if (dateOldstr.equals(oldStartDateStr) && timeOldStr.equals(oldStartTimeStr) && oldBookingServiceName.equals(bookingServiceName)) {
                foundAppointment = currOldAppointment;
                break;
            }
        } // if after iterating for loop still can't find matching customer-bookingServiceName, foundAppointment will still be null
        return foundAppointment;
    }


    /**
     * Method that finds a ServiceCombo with the same String serviceName.
     *
     * @param serviceName Name of the bookable service (String).
     * @return foundServiceCombo Matching Service Combo.
     * @author Sandy
     */
    private static ServiceCombo findServiceCombo(String serviceName) {
        ServiceCombo foundServiceCombo = null;
        for (BookableService bookableService : FlexiBookApplication.getFlexibook().getBookableServices()) {
            if (bookableService.getName().equals(serviceName)) {
                foundServiceCombo = (ServiceCombo) bookableService;
                break;
            }
        }
        return foundServiceCombo;
    }

    /**
     * Method that checks if the optionalServices are part of the list of comboItems from the existing serviceCombo.
     *
     * @param existingServiceCombo Service Combo of the optional services.
     * @param optionalServiceNames List of optional services that we want to check the validity.
     * @return isValid A boolean that indicates whether the optional services are part of the service combo.
     * @author Sandy
     */
    private static boolean isValidCombo(ServiceCombo existingServiceCombo, List<String> optionalServiceNames) {
        boolean isValid = true;
        List<ComboItem> comboItems = existingServiceCombo.getServices();
        List<String> comboItemNames = new ArrayList<String>();

        for (ComboItem comboItem : comboItems) {
            comboItemNames.add(comboItem.getService().getName());
        }
        if (!comboItemNames.containsAll(optionalServiceNames)) {
            isValid = false;
        }

        return isValid;
    }

    /**
     * Method to check if a TimeSlot is within business days and hours. Compares Date and Time.
     * If the TimeSlot overlaps Holiday, vacation and outside of business hours and days, the method will return false.
     *
     * @param serviceTimeSlot TimeSlot of the bookable service.
     * @param bookableService BookableService that we want to check the timeslot.
     * @param isUpdate boolean that indicates whether we're currently trying to update an appointment.
     * @param updateTimeSlot The desired new Timeslot after the update.
     * @return true or false boolean that indicates whether the appointment is bookable. "true" if no conflict. "false" if there's a conflict.
     * @author Sandy
     */
    private static boolean isBookable(TimeSlot serviceTimeSlot, BookableService bookableService, boolean isUpdate, TimeSlot updateTimeSlot) {
        //INITIALIZE
        boolean isHoliday = false;   //no conflict with Holiday
        boolean isVacation = false; //no conflict with Vacation
        boolean isOpen = false; // conflict with BusinessHours
        boolean isAppointmentOverlap = false; //conflict with Appointment
        /***BOOKABLESERVICE TIMESLOT***/
        //BOOK START
        Date startBookDate = serviceTimeSlot.getStartDate();  //bookdate
        Time startBookTime = serviceTimeSlot.getStartTime();    //startTime
        Calendar bookStartCal = dateTime2Calendar(startBookDate, startBookTime);  //convert Date&Time==>calendar USE THIS TO COMPARE  Note to myself: tested see DateTime2Calendar.java
        //BOOK END
        Date endBookDate = serviceTimeSlot.getEndDate();
        Time endBookTime = serviceTimeSlot.getEndTime();
        Calendar bookEndCal = dateTime2Calendar(endBookDate, endBookTime);  //convert Date&Time==>calendar USE THIS TO COMPARE

        //BusinessHour: public enum DayOfWeek { Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday }
        // ENUM SYSTEM                        {   0   ,    1      ,    2   ,   3   ,    4    ,    5    ,  6    }
        // CALENDAR SYSTEM                    {   2   ,    3      ,    4   ,   5   ,    6    ,    7    ,  1    }
        //CONCLUSION: Calendar system is 2 int ahead of enum system for MON to SAT. For Sunday, it is 5 int late
        //            int CALENDAR ==> int ENUM
        //			  for intCal 1  --> add 5
        //	     for other intCal   --> subtract 2

        //WEEKDAY   (for later use in CASE 3: BUSINESS HOURS)

        //CALENDAR SYSTEM:
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(serviceTimeSlot.getStartDate());  //get startDate of bookable service and set it to the calendar
        int intCal = calendar.get(Calendar.DAY_OF_WEEK); //in CALENDAR SYSTEM

        //CALENDAR SYSTEM---> ENUM SYSTEM      //note to myself: tested, See CompareWeekDay.java
        //convert intCal -> appropriate nb to compare with enum WeekOfDay later
        if (intCal == 1) {   //SUN 1 (CALENDAR SYSTEM)-> 6 (enum WeekOfDay system)
            intCal += 5; //add 5
        } else {   //Other weekday like Friday 5(CALENDAR SYSTEM) -> 3 (enum WeekOfDay system)
            intCal -= 2; //subtract 2
        }

        /*****END**BOOKABLE SERVICE TIMESLOT***/

        //CASE 1: HOLIDAY
        List<TimeSlot> holidays = FlexiBookApplication.getFlexibook().getBusiness().getHolidays(); //get list of holidays (TimeSlot)
        //iterate through list of holidays
        for (TimeSlot currentHoliday : holidays) {
            //HOLIDAY-START
            Date holidayStartDate = currentHoliday.getStartDate();
            Time holidayStartTime = currentHoliday.getStartTime();
            Calendar holidayStartCal = dateTime2Calendar(holidayStartDate, holidayStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE
            //HOLIDAY-END
            Date holidayEndDate = currentHoliday.getEndDate();
            Time holidayEndTime = currentHoliday.getEndTime();
            Calendar holidayEndCal = dateTime2Calendar(holidayEndDate, holidayEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE

            //CASE 1A: WITHIN HOLIDAYS

            if(!bookStartCal.after(holidayEndCal) && !bookEndCal.before(holidayStartCal)){
                isVacation = true;
                break;
            }
        }

        //CASE 2: WITHIN VACATION
        List<TimeSlot> vacations = FlexiBookApplication.getFlexibook().getBusiness().getVacation();
        for (TimeSlot currentVacation : vacations) {
            //VACATION-START
            Date vacationStartDate = currentVacation.getStartDate();
            Time vacationStartTime = currentVacation.getStartTime();
            Calendar vacationStartCal = dateTime2Calendar(vacationStartDate, vacationStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE

            //VACATION-END
            Date vacationEndDate = currentVacation.getEndDate();
            Time vacationEndTime = currentVacation.getEndTime();
            Calendar vacationEndCal = dateTime2Calendar(vacationEndDate, vacationEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE
            //CASE 1A: WITHIN VACATION DAYS
            if(!bookStartCal.after(vacationEndCal) && !bookEndCal.before(vacationStartCal)){
                isVacation = true;
                break;
            }
        }

        //CASE 3: BUSINESS HOURS
        List<BusinessHour> businessHours = FlexiBookApplication.getFlexibook().getBusiness().getBusinessHours();

        for (BusinessHour currentBusinessHour : businessHours) {

            //CASE 3A: INSIDE WEEKDAY
            int weekDayPosition = currentBusinessHour.getDayOfWeek().ordinal();  //weekday -> int

            if (intCal == weekDayPosition) { // if same weekday //for example SUNDAY (6) for both
                // ENTER if found matching business weekday with booking date's weekday
                //BUSINESS-START
                Date businessStartDate = serviceTimeSlot.getStartDate();  // since same weekday, set businessStartDate to be the same as the booking start date
                Time businessStartTime = currentBusinessHour.getStartTime();
                Calendar businessStartCal = dateTime2Calendar(businessStartDate, businessStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE

                //BUSINESS-END
                Date businessEndDate = serviceTimeSlot.getEndDate();  // since same weekday, set businessEndDate to be the same as the booking end date
                Time businessEndTime = currentBusinessHour.getEndTime();
                Calendar businessEndCal = dateTime2Calendar(businessEndDate, businessEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE

                if (!businessStartCal.after(bookStartCal) && !businessEndCal.before(bookEndCal)) {
                    isOpen = true;
                    break;
                }
            }
        }

        //CASE 4 APPOINTMENT OVERLAPS with OLD (existing) appointment
        List<Appointment> oldAppointments = FlexiBookApplication.getFlexibook().getAppointments();  //get list of appointments in the FlexiBook

        outerloop:
        for (Appointment oldAppointment : oldAppointments) {
            if (isUpdate == true) {  // if we're updating, we need to skip the oldAppointment that is same as appointment2update
//                if (oldAppointment.equals(appointment2update)) {
//                    continue;
//                }
                if (oldAppointment.getTimeSlot().equals(updateTimeSlot)) continue;
            }
            //OLD APPOINTMENT START DATE
            Date appStartDate = oldAppointment.getTimeSlot().getStartDate();
            Time appStartTime = oldAppointment.getTimeSlot().getStartTime();
            Calendar appStartCal = dateTime2Calendar(appStartDate, appStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE

            //OLD APPOINTMENT END DATE
            Date appEndDate = oldAppointment.getTimeSlot().getEndDate();
            Time appEndTime = oldAppointment.getTimeSlot().getEndTime();
            //appEndDate = cleanDate(appEndDate,appEndTime); //USE THIS :)
            Calendar appEndCal = dateTime2Calendar(appEndDate, appEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE
            /*********ENTER DANGER ZONE*******/
			/* DANGER 1:          [    existing app      ]
			                [     book    ] */
            if (bookStartCal.before(appStartCal) && bookEndCal.after(appStartCal)) {
                isAppointmentOverlap = true;
                break outerloop;
            }
			/* DANGER 2:    [    existing app      ]
			              				  [     book      ] */
            if (bookStartCal.before(appEndCal) && bookEndCal.after(appEndCal)) {
                isAppointmentOverlap = true;
                break outerloop;
            }
            //DANGER ZONE 3: bookableService is WITHIN an EXISTING appointment
            if (appStartCal.before(bookStartCal) && appEndCal.after(bookEndCal)) {
                //APPOINTMENT = CASE 4A OR CASE 4B
                BookableService oldBS = oldAppointment.getBookableService();
                if (oldBS instanceof Service) {  //CASE 4A
                    Service s = (Service) oldBS;
                    //CASE 4A1: current appointment = single service
                    int intDowntimeDuration = s.getDowntimeDuration();
                    if (intDowntimeDuration != 0) {

                        int intDowntimeStart = s.getDowntimeStart();
                        Calendar downtimeStartCal = downtime2Cal(appStartCal, intDowntimeStart);

                        int durationStartService2Downtime = intDowntimeStart + intDowntimeDuration; //time elapsed since start of service until end of downtime
                        //Date downtimeEnd = downtime2Date(appEndDate, durationStartService2Downtime); //USE THIS
                        Calendar downtimeEndCal = downtime2Cal(appEndCal, durationStartService2Downtime);

                        //CHECK if can fit booking INTO the DOWNTIME of current existing appointment
                        if (downtimeStartCal.before(bookStartCal) && downtimeEndCal.after(bookEndCal)) {

                            isAppointmentOverlap = false;  //CAN fit booking into the downtime of the appointment, so NO overlap
                            break outerloop;
                        }
                    } // intDowntimeDuration==0
                    isAppointmentOverlap = true; // CAN'T fit booking into downtime, OVERLAP of bs that is a service:(
                    break outerloop;
                } else { //CASE 4B: bs = ServiceCombo
                    //CHECK all DOWNTIME FOR ALL SERVICES FROM THE CHOSEN ITEMS from oldAppointment
                    List<ComboItem> oldChosenItems = oldAppointment.getChosenItems();
                    for (ComboItem oldChosenItem : oldChosenItems) {
                        Service oldServ = oldChosenItem.getService();  //get the service of the current comboItem
                        int intDowntimeStart = oldServ.getDowntimeStart();
                        Calendar downtimeStartCal = downtime2Cal(appStartCal, intDowntimeStart);
                        int intDowntimeEnd = oldServ.getDowntimeDuration() + intDowntimeStart;
                        Calendar downtimeEndCal = downtime2Cal(appEndCal, intDowntimeEnd);
                        //check if can fit booking into downtime of oldServ's downtime
                        if (downtimeStartCal.before(bookStartCal) && downtimeEndCal.after(bookEndCal)) {
                            isAppointmentOverlap = false;  //can fit booking into the downtime of oldServ's downtime, so NO overlap
                            break outerloop;
                        }
                    }
                    isAppointmentOverlap = true;
                    break outerloop;
                }
            } else if (appStartCal.equals(bookStartCal)) { //same start time and date
                isAppointmentOverlap = true;
                break outerloop;
            }
        }
        //CONCLUSION

        //CASE A: TimeSlot is NOT within holidays, vacation and is WITHIN opening days/hours = NO CONFLICT
        if ((isHoliday == false) && (isVacation == false) && (isAppointmentOverlap == false) && (isOpen == true)) {
            return true;
        } else {
            return false; //CASE B: CONFLICT! :(
        }
    }

    private static Calendar downtime2Cal(Calendar appStartCal, int intDowntimeStart) {
        appStartCal.add(Calendar.MINUTE, intDowntimeStart);
        appStartCal.getTime();
        return appStartCal;
    }

    /**
     * Method  finds in the FlexiBook application a BookableService with the same name as input name.
     *
     * @param bookableServiceName Name of the bookable service to be found.
     * @return foundBookableService Matching Bookable Service.
     * @author Sandy
     */
    private static BookableService findBookableService(String bookableServiceName) {
        BookableService foundBookableService = null;
        //iterate through list of BookableServices in the flexibook
        for (BookableService currentBookableService : FlexiBookApplication.getFlexibook().getBookableServices()) {
            if (currentBookableService.getName().equals(bookableServiceName)) {
                foundBookableService = currentBookableService;
                break;
            }
        }
        return foundBookableService;
    }

    /**
     * This method returns a Date cleanedDate with the appropriate input date and input time.
     * @param date Date of type java.sql.Date.
     * @param time Time of type java.sql.Time.
     * @return cleanedDate Correct Date with time set to input time.
     * @author Sandy
     */
    private static Date cleanDate(Date date, Time time) {
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
     * This method returns a Date downtime with the appropriate date and time.
     * It adds the number of minutes.
     *
     * @param date: Start Date (including time, type: java.sql.Date).
     * @param addMin: Number of minutes to add to the date to get the date and time of the downtime (int).
     * @return downtimeDate: Date of the downtime  (including set time).
     * @author Sandy
     */
    private static Date downtime2Date(Date date, int addMin) {
        //assume Date is already cleaned

        // Date -> Calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        //add minutes
        cal.add(Calendar.MINUTE, addMin);
        cal.getTime();
        // Calendar -> Date
        java.util.Date tempCleanedDate = cal.getTime();
        java.sql.Date downtimeDate = new java.sql.Date(tempCleanedDate.getTime());
        return downtimeDate;
    }

    /**
     * This method takes as input a Time time, adds the input int duration (minutes) to it and returns a Time finalTime.
     *
     * @param date Initial Date of type java.sql.Date.
     * @param time Initial time of type java.sql.Time.
     * @param addMin Number of minutes to be added to the initial time (int).
     * @return finalTime Final Time after adding addMin.
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

    /**
     * Method that make sure that the time in a date is set to 0:00:00.
     *
     * @param date Date of type java.sql.Date.
     * @return cleanedDate Date with the time set to 0.
     * @author Tutor
     */
    private static Date cleanDate(Date date) {
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
}
