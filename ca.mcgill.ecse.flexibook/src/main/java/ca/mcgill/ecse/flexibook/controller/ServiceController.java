package ca.mcgill.ecse.flexibook.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.awt.print.Book;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.model.Appointment;
import ca.mcgill.ecse.flexibook.model.FlexiBook;
import ca.mcgill.ecse.flexibook.model.BookableService;
import ca.mcgill.ecse.flexibook.model.Service;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;

import ca.mcgill.ecse.flexibook.util.SystemTime;


public class ServiceController {

	private static String ownerName = FlexiBookApplication.getFlexibook().getOwner().getUsername();
	public ServiceController(){
        
    }

	/**
	 * Creates a Service is all inputs are valid, else throws an InvalidInputException.
	 * @author Jessie
	 * @param serviceName Name of the service to be created.
	 * @param duration length of time of the service to be created.
	 * @param downtimeDuration Length of the downtime period.
	 * @param downtimeStart What time the downtime begins after the start time of the service.
	 * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.

	 */
    public static void createService(String serviceName, int duration, int downtimeDuration, int downtimeStart) throws InvalidInputException {
    	String error = "";

		// check that owner is logged in
		if (!FlexiBookApplication.getCurrentUser().getUsername().equals(ownerName)) {
			error = "You are not authorized to perform this operation";
			throw new InvalidInputException(error.trim());
		}

    	if (duration <= 0) {
    		error = error + "Duration must be positive";
    	}
    	if (downtimeStart > 0 && downtimeDuration == 0) {
    		error = error + "Downtime duration must be positive";
    	}
    	if (downtimeStart == 0 && downtimeDuration < 0) {
    		error = error + "Downtime duration must be 0";
    	}
    	if (downtimeStart == 0 && downtimeDuration > 0) {
    		error = error + "Downtime must not start at the beginning of the service";
    	}
    	if (downtimeStart < 0) {
    		error = error + "Downtime must not start before the beginning of the service";
    	}
    	if (downtimeStart + downtimeDuration > duration) {
    		error = error + "Downtime must not end after the service";
    	}
    	if (downtimeStart > duration) {
    		error = error + "Downtime must not start after the end of the service";
    	}
    	if (error.length() > 0) {
    		throw new InvalidInputException(error.trim());
    	}
    	
    	FlexiBook flexibook = FlexiBookApplication.getFlexibook();

    	try {
			//careful, order of downtimeDuration and downtimeStart is different from in AddService.feature
			Service service = new Service(serviceName, flexibook, duration, downtimeDuration, downtimeStart);
    		flexibook.addBookableService(service);
		    FlexiBookPersistence.save(flexibook);
    	}
    	catch(RuntimeException e) {
    		if (e.getMessage().trim().equals("Cannot create due to duplicate name. See http://manual.umple.org?RE003ViolationofUniqueness.html")) {
				error = "Service " + serviceName + " already exists";
			}
    		throw new InvalidInputException(error.trim());
    	}

    }

	/** Updates a Service if all inputs are valid, else throws an InvalidInputException
	 * @author Jessie
	 * @param oldServiceName Name of the service to be changed.
	 * @param newServiceName Specifies the name of which to change the service to.
	 * @param duration New length of time of the service.
	 * @param downtimeDuration New length of the downtime within the service.
	 * @param downtimeStart New starting time of the downtime within the service duration.
	 * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.
	 */
	public static void updateService(String oldServiceName, String newServiceName, int duration, int downtimeDuration, int downtimeStart) throws InvalidInputException {
		String error = "";

		// check that owner is logged in
		if (!FlexiBookApplication.getCurrentUser().getUsername().equals(ownerName)) {
			error = "You are not authorized to perform this operation";
			throw new InvalidInputException(error.trim());
		}

		if (duration <= 0) {
			error = error + "Duration must be positive";
		}
		if (downtimeDuration == 0 && downtimeStart > 0) {
			error = error + "Downtime duration must be positive";
		}
		if (downtimeStart == 0 && downtimeDuration < 0) {
			error = error + "Downtime duration must be 0";
		}
		if (downtimeStart == 0 && downtimeDuration > 0) {
			error = error + "Downtime must not start at the beginning of the service";
		}
		if (downtimeStart < 0) {
			error = error + "Downtime must not start before the beginning of the service";
		}
		if (downtimeStart + downtimeDuration > duration) {
			error = error + "Downtime must not end after the service";
		}
		if (downtimeStart > duration) {
			error = error + "Downtime must not start after the end of the service";
		}
		if (error.length() > 0) {
			throw new InvalidInputException(error.trim());
		}

		Service existingService = findService(oldServiceName);

		if (findService(newServiceName) == null){
			existingService.setName(newServiceName);
			existingService.setDuration(duration);
			existingService.setDowntimeDuration(downtimeDuration);
			existingService.setDowntimeStart(downtimeStart);
		}
		else if (newServiceName.equals(oldServiceName)){
			existingService.setDuration(duration);
			existingService.setDowntimeDuration(downtimeDuration);
			existingService.setDowntimeStart(downtimeStart);
		}
		else {
			error = "Service " + newServiceName + " already exists";
			throw new InvalidInputException(error.trim());
		}
		try {
			FlexiBook flexibook = FlexiBookApplication.getFlexibook();
			FlexiBookPersistence.save(flexibook);
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}

	/** Method that deletes a service.
	 * @author Jessie
	 * @param serviceName The name of the service to be deleted.
	 */
	public static void deleteService(String serviceName) throws InvalidInputException {
		Service service = findService(serviceName);
		String error = "";

		// check that owner is logged in
		if (!FlexiBookApplication.getCurrentUser().getUsername().equals(ownerName)) {
			error += "You are not authorized to perform this operation";
		}
		if (service != null){
			if (service.hasAppointments()){

				String currentDateTime = SystemTime.getSystemTime();
				LocalDateTime actualLocalDateTime = LocalDateTime.parse(currentDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
				for(Appointment currentAppointment : service.getAppointments()){
					LocalDateTime appointmentLocalDateTime = LocalDateTime.parse(currentAppointment.getTimeSlot().getStartDate().toString()+"+"+currentAppointment.getTimeSlot().getStartTime().toString().substring(0,5),
							DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
					if(appointmentLocalDateTime.compareTo(actualLocalDateTime)>0){
						error += "The service contains future appointments";;
					}
				}

			}
		}
		if (error.length() > 0) {
			throw new InvalidInputException(error.trim());
		}
		else {
			service.delete();
		}
		try {
			FlexiBook flexibook = FlexiBookApplication.getFlexibook();
			FlexiBookPersistence.save(flexibook);
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}


	}


	/**Method that returns a service if it exists.
	 * @author Jessie
	 * @param name Name of the service to be found.
	 * @return Service
	 */
	private static Service findService(String name) {
    	Service foundService = null;
    	for (BookableService service : FlexiBookApplication.getFlexibook().getBookableServices()) {

    		//Note: compare strings with .equals() and not just ==, otherwise won't be equal
    		if (service.getName().equals(name)) {
    			foundService = (Service)service;
    			break;
    		}
    	}
    	return foundService;
    }

	/** Method that returns a list of all existing Service data transfer objects.
	 * @author Jessie
	 * @return List<TOService>
	 */
	public static List<TOService> getServices(){
    	ArrayList<TOService> services = new ArrayList<TOService>();
    	for (BookableService service : FlexiBookApplication.getFlexibook().getBookableServices()) {
    		if (service.getClass() == Service.class) { 
    			TOService toService = new TOService(service.getName(), ((Service) service).getDuration(), ((Service)service).getDowntimeDuration(), ((Service) service).getDowntimeStart());
        		services.add(toService);
    		}
    	}
    	return services;
    }
}
