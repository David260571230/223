package ca.mcgill.ecse.flexibook.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;


import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.model.*;
import ca.mcgill.ecse.flexibook.util.SystemTime;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;



public class ServiceComboController {

	private static String ownerName = FlexiBookApplication.getFlexibook().getOwner().getUsername();

	/**
	 * Creates a service combo if all parameters are valid else throws an invalidInputException.
	 * @author Jeremy
	 * @param comboName Sets the name of a new service combo.
	 * @param mainService Sets the main service of a new service combo.
	 * @param services Sets the services in a new service combo.
	 * @param mandatorySettings Sets the mandatory settings of services in a new service combo.
	 * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.
	 */
	public static void createServiceCombo(String comboName, String mainService, List<String> services, List<String> mandatorySettings) throws InvalidInputException{
		String error = "";
		FlexiBook flexiBook = FlexiBookApplication.getFlexibook();

		// check that owner is logged in
		if (!FlexiBookApplication.getCurrentUser().getUsername().equals(ownerName)) {
			error = "You are not authorized to perform this operation";
			throw new InvalidInputException(error.trim());
		}

		//checks if main service is a service
		if(findService(mainService) == null){
			error += "Service " + mainService + " does not exist\n";
		}
		//checks if services includes main service
		boolean mainServiceInclusion = false;
		for(String currentService : services){
			if(currentService.equals(mainService)){
				mainServiceInclusion = true;
			}
		}
		if(!mainServiceInclusion){
			error += "Main service must be included in the services\n";
		}
		//checks if mainService is mandatory
		if(mainServiceInclusion){
			for(String currentService : services){
				if(currentService.equals(mainService)){
					if(!Boolean.parseBoolean(mandatorySettings.get(services.indexOf(currentService)))){
						error += "Main service must be mandatory\n";
					}
				}
			}
		}
		//checks that there are at least 2 services
		if(services.size()<2) {
			error += "A service Combo must contain at least 2 services\n";
		}
		//checks that all services exist
		for(String currentService : services){
			if(findService(currentService) == null){
				error += "Service " + currentService + " does not exist\n";
			}
		}
		//checks if comboName exists already
		if(findServiceCombo(comboName) != null){
			error +=  "Service combo " + comboName + " already exists\n";
		}
		//checks if combo name contains main service name
		if(!comboName.toLowerCase().contains(mainService.toLowerCase())){
			error += "Combo name must include main service name";
		}
		//checks if amount of parameters match
		if(services.size() != mandatorySettings.size()){
			error += "Must specify whether each service is mandatory";
		}
		if(error.length() > 0) {
			throw new InvalidInputException(error.trim());
		}
		else {
			ServiceCombo serviceCombo = new ServiceCombo(comboName, flexiBook);
			ArrayList<Service> serviceList = new ArrayList<Service>();
			ArrayList<Boolean> mandatoryList = new ArrayList<Boolean>();
			for(String currentService : services){
				serviceList.add(findService(currentService));
			}
			for(String currentMandatorySetting : mandatorySettings){
				mandatoryList.add(Boolean.parseBoolean(currentMandatorySetting));
			}
			ComboItem comboItemAdded = null;
			for(int i=0; i<services.size(); i++) {
				 comboItemAdded = serviceCombo.addService(mandatoryList.get(i), serviceList.get(i));
				 if(services.get(i).equals(mainService)) {
					 serviceCombo.setMainService(comboItemAdded);
				 }
			}

		}
		try {
			FlexiBookPersistence.save(flexiBook);
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
		
	}
	
	/**
	 * Updates a service combo if all parameters are valid else throws an invalidInputException.
	 * @author Jeremy
	 * @param oldComboName Finds the service combo to be updated.
	 * @param comboName Sets the name of the new service combo.
	 * @param mainService Sets the main service of the new service combo
	 * @param services Sets the services of the new service combo.
	 * @param mandatorySettings Sets the mandatory settings of the services in the new service combo.
	 * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.
	 */
	public static void updateServiceCombo(String oldComboName, String comboName, String mainService, List<String> services, List<String> mandatorySettings) throws InvalidInputException {
		String error = "";
		FlexiBook flexiBook = FlexiBookApplication.getFlexibook();

		// check that owner is logged in
		if (!FlexiBookApplication.getCurrentUser().getUsername().equals(ownerName)) {
			error = "You are not authorized to perform this operation";
			throw new InvalidInputException(error.trim());
		}

		//checks if main service is a service
		if(findService(mainService) == null){
			error += "Service " + mainService + " does not exist\n";
		}
		//checks if services includes main service
		boolean mainServiceInclusion = false;
		for(String currentService : services){
			if(currentService.equals(mainService)){
				mainServiceInclusion = true;
			}
		}
		if(!mainServiceInclusion){
			error += "Main service must be included in the services\n";
		}
		//checks if mainService is mandatory
		if(mainServiceInclusion){
			for(String currentService : services){
				if(currentService.equals(mainService)){
					if(!Boolean.parseBoolean(mandatorySettings.get(services.indexOf(currentService)))){
						error += "Main service must be mandatory\n";
					}
				}
			}
		}
		//checks that there are at least 2 services
		if(services.size()<2) {
			error += "A service Combo must have at least 2 services\n";
		}
		//checks that all services exist
		for(String currentService : services){
			if(findService(currentService) == null){
				error += "Service " + currentService + " does not exist\n";
			}
		}
		//Checks if new combo name already exists
		if(!oldComboName.equals(comboName)){
			if(findServiceCombo(comboName)!=null){
				error += "Service combo " + comboName + " already exists";
			}
		}
		//checks if combo name contains main service name
		if(!comboName.toLowerCase().contains(mainService.toLowerCase())){
			error += "Combo name must include main service name";
		}
		//checks if amount of parameters match
		if(services.size() != mandatorySettings.size()){
			error += "Must specify whether each service is mandatory";
		}
		if(error.length() > 0) {
			throw new InvalidInputException(error.trim());
		}
		else {
			ServiceCombo serviceCombo = findServiceCombo(oldComboName);
			serviceCombo.delete();
			serviceCombo = new ServiceCombo(comboName, flexiBook);
			ArrayList<Service> serviceList = new ArrayList<Service>();
			ArrayList<Boolean> mandatoryList = new ArrayList<Boolean>();
			for(String currentService : services){
				serviceList.add(findService(currentService));
			}
			for(String currentMandatorySetting : mandatorySettings){
				mandatoryList.add(Boolean.parseBoolean(currentMandatorySetting));
			}
			ComboItem comboItemAdded = null;
			for(int i=0; i<services.size(); i++) {
				comboItemAdded = serviceCombo.addService(mandatoryList.get(i), serviceList.get(i));
				if(services.get(i).equals(mainService)) {
					serviceCombo.setMainService(comboItemAdded);
				}
			}
		}
		try {
			FlexiBookPersistence.save(flexiBook);
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}
	
	/**
	 * Deletes a service combo if combo doesn't have anymore appointments.
	 * @author Jeremy
	 * @param comboName Used to find the service combo to be deleted.
	 * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.
	 */
	public static void deleteServiceCombo(String comboName) throws InvalidInputException {
		String error = "";
		FlexiBook flexiBook = FlexiBookApplication.getFlexibook();
		ServiceCombo serviceCombo = findServiceCombo(comboName);
		// check that owner is logged in
		if (!FlexiBookApplication.getCurrentUser().getUsername().equals(ownerName)) {
			error += "You are not authorized to perform this operation";
		}
		if(serviceCombo.hasAppointments()) {
			String currentDateTime = SystemTime.getSystemTime();
			LocalDateTime actualLocalDateTime = LocalDateTime.parse(currentDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
			for(Appointment currentAppointment : serviceCombo.getAppointments()){
				LocalDateTime appointmentLocalDateTime = LocalDateTime.parse(currentAppointment.getTimeSlot().getStartDate().toString()+"+"+currentAppointment.getTimeSlot().getStartTime().toString().substring(0,5),
						DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm"));
				if(appointmentLocalDateTime.compareTo(actualLocalDateTime)>0){
					error += "Service combo " + serviceCombo.getName() + " has future appointments\n";
				}
			}
		}
		if(error.length() > 0) {
			throw new InvalidInputException(error.trim());
		}
		else {
			serviceCombo.delete();
		}
		try {
			FlexiBookPersistence.save(flexiBook);
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}
	
	/**
	 * @author Jeremy
	 * @param name Used to find service combo.
	 * @return ServiceCombo
	 * Returns a service combo if it exists.
	 */
    private static ServiceCombo findServiceCombo(String name) {
    	ServiceCombo foundServiceCombo = null;
    	for (BookableService bookableService : FlexiBookApplication.getFlexibook().getBookableServices()) {
    		//Note: compare strings with .equals() and not just ==, otherwise won't be equal
    		if (bookableService.getName().equals(name)) {

    			foundServiceCombo = (ServiceCombo)bookableService;
    			break;
    		}
    	}
    	return foundServiceCombo;
    }

	/**
	 * @author Jessie
	 * @param name Used to find a service.
	 * @return Service
	 * returns service if it exists.
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
    
    /**Transfer objects for ServiceCombo.
     * @author Jeremy
     * @return List<TOServiceCombo>
	 * returns a list of all existing service combos.
     */
    public static List<TOServiceCombo> getServiceCombos(){
    	ArrayList<TOServiceCombo> serviceCombos = new ArrayList<TOServiceCombo>();
    	for(BookableService bookableService : FlexiBookApplication.getFlexibook().getBookableServices()) {
    		if(bookableService.getClass() == ServiceCombo.class) {
    			ServiceCombo currentCombo = (ServiceCombo)bookableService;
    			TOServiceCombo toServiceCombo = new TOServiceCombo(currentCombo.getName(),currentCombo.getMainService().getService().getName());
    			for(ComboItem currentComboItem : currentCombo.getServices()) {
    				String serviceName = currentComboItem.getService().getName();
    				Integer duration = currentComboItem.getService().getDuration();
    				String mandatory = "" + currentComboItem.getMandatory();
    				toServiceCombo.addService(serviceName);
    				toServiceCombo.addServiceLength(duration);
    				toServiceCombo.addMandatoryService(mandatory);
    			}
    			serviceCombos.add(toServiceCombo);
    		}
    	}
    	return serviceCombos;
    }


}
