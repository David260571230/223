/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.30.1.5099.60569f335 modeling language!*/

package ca.mcgill.ecse.flexibook.controller;
import java.util.*;

// line 12 "../../../../../FlexiBookTransferObjects.ump"
public class TOServiceCombo
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //TOServiceCombo Attributes
  private String name;
  private String mainService;
  private List<String> services;
  private List<Integer> serviceLengths;
  private List<String> mandatoryServices;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public TOServiceCombo(String aName, String aMainService)
  {
    name = aName;
    mainService = aMainService;
    services = new ArrayList<String>();
    serviceLengths = new ArrayList<Integer>();
    mandatoryServices = new ArrayList<String>();
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setName(String aName)
  {
    boolean wasSet = false;
    name = aName;
    wasSet = true;
    return wasSet;
  }

  public boolean setMainService(String aMainService)
  {
    boolean wasSet = false;
    mainService = aMainService;
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
  /* Code from template attribute_SetMany */
  public boolean addServiceLength(Integer aServiceLength)
  {
    boolean wasAdded = false;
    wasAdded = serviceLengths.add(aServiceLength);
    return wasAdded;
  }

  public boolean removeServiceLength(Integer aServiceLength)
  {
    boolean wasRemoved = false;
    wasRemoved = serviceLengths.remove(aServiceLength);
    return wasRemoved;
  }
  /* Code from template attribute_SetMany */
  public boolean addMandatoryService(String aMandatoryService)
  {
    boolean wasAdded = false;
    wasAdded = mandatoryServices.add(aMandatoryService);
    return wasAdded;
  }

  public boolean removeMandatoryService(String aMandatoryService)
  {
    boolean wasRemoved = false;
    wasRemoved = mandatoryServices.remove(aMandatoryService);
    return wasRemoved;
  }

  public String getName()
  {
    return name;
  }

  public String getMainService()
  {
    return mainService;
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
  /* Code from template attribute_GetMany */
  public Integer getServiceLength(int index)
  {
    Integer aServiceLength = serviceLengths.get(index);
    return aServiceLength;
  }

  public Integer[] getServiceLengths()
  {
    Integer[] newServiceLengths = serviceLengths.toArray(new Integer[serviceLengths.size()]);
    return newServiceLengths;
  }

  public int numberOfServiceLengths()
  {
    int number = serviceLengths.size();
    return number;
  }

  public boolean hasServiceLengths()
  {
    boolean has = serviceLengths.size() > 0;
    return has;
  }

  public int indexOfServiceLength(Integer aServiceLength)
  {
    int index = serviceLengths.indexOf(aServiceLength);
    return index;
  }
  /* Code from template attribute_GetMany */
  public String getMandatoryService(int index)
  {
    String aMandatoryService = mandatoryServices.get(index);
    return aMandatoryService;
  }

  public String[] getMandatoryServices()
  {
    String[] newMandatoryServices = mandatoryServices.toArray(new String[mandatoryServices.size()]);
    return newMandatoryServices;
  }

  public int numberOfMandatoryServices()
  {
    int number = mandatoryServices.size();
    return number;
  }

  public boolean hasMandatoryServices()
  {
    boolean has = mandatoryServices.size() > 0;
    return has;
  }

  public int indexOfMandatoryService(String aMandatoryService)
  {
    int index = mandatoryServices.indexOf(aMandatoryService);
    return index;
  }

  public void delete()
  {}


  public String toString()
  {
    return super.toString() + "["+
            "name" + ":" + getName()+ "," +
            "mainService" + ":" + getMainService()+ "]";
  }
}