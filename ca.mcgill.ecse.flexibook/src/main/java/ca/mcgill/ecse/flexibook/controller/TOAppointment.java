/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.30.1.5099.60569f335 modeling language!*/

package ca.mcgill.ecse.flexibook.controller;
import java.util.*;

// line 28 "../../../../../FlexiBookTransferObjects.ump"
public class TOAppointment
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //TOAppointment Attributes
  private String username;
  private String serviceName;
  private List<String> services;
  private String startDate;
  private String startTime;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public TOAppointment(String aUsername, String aServiceName, String aStartDate, String aStartTime)
  {
    username = aUsername;
    serviceName = aServiceName;
    services = new ArrayList<String>();
    startDate = aStartDate;
    startTime = aStartTime;
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setUsername(String aUsername)
  {
    boolean wasSet = false;
    username = aUsername;
    wasSet = true;
    return wasSet;
  }

  public boolean setServiceName(String aServiceName)
  {
    boolean wasSet = false;
    serviceName = aServiceName;
    wasSet = true;
    return wasSet;
  }
  /* Code from template attribute_SetMany */
  public boolean addService(String aService)
  {
    boolean wasAdded = false;
    wasAdded = services.add(aService);
    return wasAdded;
  }

  public boolean removeService(String aService)
  {
    boolean wasRemoved = false;
    wasRemoved = services.remove(aService);
    return wasRemoved;
  }

  public boolean setStartDate(String aStartDate)
  {
    boolean wasSet = false;
    startDate = aStartDate;
    wasSet = true;
    return wasSet;
  }

  public boolean setStartTime(String aStartTime)
  {
    boolean wasSet = false;
    startTime = aStartTime;
    wasSet = true;
    return wasSet;
  }

  public String getUsername()
  {
    return username;
  }

  public String getServiceName()
  {
    return serviceName;
  }
  /* Code from template attribute_GetMany */
  public String getService(int index)
  {
    String aService = services.get(index);
    return aService;
  }

  public String[] getServices()
  {
    String[] newServices = services.toArray(new String[services.size()]);
    return newServices;
  }

  public int numberOfServices()
  {
    int number = services.size();
    return number;
  }

  public boolean hasServices()
  {
    boolean has = services.size() > 0;
    return has;
  }

  public int indexOfService(String aService)
  {
    int index = services.indexOf(aService);
    return index;
  }

  public String getStartDate()
  {
    return startDate;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public void delete()
  {}


  public String toString()
  {
    return super.toString() + "["+
            "username" + ":" + getUsername()+ "," +
            "serviceName" + ":" + getServiceName()+ "," +
            "startDate" + ":" + getStartDate()+ "," +
            "startTime" + ":" + getStartTime()+ "]";
  }
}