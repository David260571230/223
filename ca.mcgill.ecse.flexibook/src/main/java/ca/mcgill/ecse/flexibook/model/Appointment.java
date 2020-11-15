/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.30.1.5099.60569f335 modeling language!*/

package ca.mcgill.ecse.flexibook.model;
import java.sql.Date;
import java.sql.Time;
import java.util.*;
import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.util.SystemTime;
import java.io.Serializable;

// line 1 "../../../../../FlexiBookStates.ump"
// line 19 "../../../../../FlexiBookPersistence.ump"
// line 88 "../../../../../FlexiBook.ump"
public class Appointment implements Serializable
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Appointment State Machines
  public enum AppointmentStatus { Pending, Final, Active }
  private AppointmentStatus appointmentStatus;

  //Appointment Associations
  private Customer customer;
  private BookableService bookableService;
  private List<ComboItem> chosenItems;
  private TimeSlot timeSlot;
  private FlexiBook flexiBook;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Appointment(Customer aCustomer, BookableService aBookableService, TimeSlot aTimeSlot, FlexiBook aFlexiBook)
  {
    boolean didAddCustomer = setCustomer(aCustomer);
    if (!didAddCustomer)
    {
      throw new RuntimeException("Unable to create appointment due to customer. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
    }
    boolean didAddBookableService = setBookableService(aBookableService);
    if (!didAddBookableService)
    {
      throw new RuntimeException("Unable to create appointment due to bookableService. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
    }
    chosenItems = new ArrayList<ComboItem>();
    if (!setTimeSlot(aTimeSlot))
    {
      throw new RuntimeException("Unable to create Appointment due to aTimeSlot. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
    }
    boolean didAddFlexiBook = setFlexiBook(aFlexiBook);
    if (!didAddFlexiBook)
    {
      throw new RuntimeException("Unable to create appointment due to flexiBook. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
    }
    setAppointmentStatus(AppointmentStatus.Pending);
  }

  //------------------------
  // INTERFACE
  //------------------------

  public String getAppointmentStatusFullName()
  {
    String answer = appointmentStatus.toString();
    return answer;
  }

  public AppointmentStatus getAppointmentStatus()
  {
    return appointmentStatus;
  }

  public boolean startAppointment(BookableService bookingService,TimeSlot oldTimeSlot)
  {
    boolean wasEventProcessed = false;
    
    AppointmentStatus aAppointmentStatus = appointmentStatus;
    switch (aAppointmentStatus)
    {
      case Pending:
        setAppointmentStatus(AppointmentStatus.Active);
        wasEventProcessed = true;
        break;
      case Active:
        setAppointmentStatus(AppointmentStatus.Active);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean updateAppointment(BookableService bookingService,TimeSlot oldTimeSlot,TimeSlot newTimeSlot,List<ComboItem> addOptionalServices,List<ComboItem> removeOptionalServices)
  {
    boolean wasEventProcessed = false;
    
    AppointmentStatus aAppointmentStatus = appointmentStatus;
    switch (aAppointmentStatus)
    {
      case Pending:
        if (isBookable(newTimeSlot,Boolean.parseBoolean("true"),oldTimeSlot))
        {
        // line 14 "../../../../../FlexiBookStates.ump"
          if (dayOf(oldTimeSlot) && !newTimeSlot.equals(oldTimeSlot)) break;
        else doUpdate(bookingService, newTimeSlot, addOptionalServices, removeOptionalServices);
          setAppointmentStatus(AppointmentStatus.Pending);
          wasEventProcessed = true;
          break;
        }
        break;
      case Active:
        if (isBookable(newTimeSlot,Boolean.parseBoolean("true"),oldTimeSlot))
        {
        // line 30 "../../../../../FlexiBookStates.ump"
          if (!newTimeSlot.getStartTime().equals(oldTimeSlot.getStartTime())) break;
         else doUpdate(bookingService, newTimeSlot, addOptionalServices, removeOptionalServices);
          setAppointmentStatus(AppointmentStatus.Active);
          wasEventProcessed = true;
          break;
        }
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean cancelAppointment(BookableService bookingService,TimeSlot oldTimeSlot)
  {
    boolean wasEventProcessed = false;
    
    AppointmentStatus aAppointmentStatus = appointmentStatus;
    switch (aAppointmentStatus)
    {
      case Pending:
        if (!(dayOf(oldTimeSlot)))
        {
        // line 19 "../../../../../FlexiBookStates.ump"
          
          setAppointmentStatus(AppointmentStatus.Final);
          wasEventProcessed = true;
          break;
        }
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean cancelForNoShow(BookableService bookingService,TimeSlot oldTimeSlot)
  {
    boolean wasEventProcessed = false;
    
    AppointmentStatus aAppointmentStatus = appointmentStatus;
    switch (aAppointmentStatus)
    {
      case Pending:
        if (dayOf(oldTimeSlot))
        {
        // line 21 "../../../../../FlexiBookStates.ump"
          incrementNoShow();
          setAppointmentStatus(AppointmentStatus.Final);
          wasEventProcessed = true;
          break;
        }
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean finishAppointment()
  {
    boolean wasEventProcessed = false;
    
    AppointmentStatus aAppointmentStatus = appointmentStatus;
    switch (aAppointmentStatus)
    {
      case Active:
        setAppointmentStatus(AppointmentStatus.Final);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  private void setAppointmentStatus(AppointmentStatus aAppointmentStatus)
  {
    appointmentStatus = aAppointmentStatus;

    // entry actions and do activities
    switch(appointmentStatus)
    {
      case Final:
        delete();
        break;
    }
  }
  /* Code from template association_GetOne */
  public Customer getCustomer()
  {
    return customer;
  }
  /* Code from template association_GetOne */
  public BookableService getBookableService()
  {
    return bookableService;
  }
  /* Code from template association_GetMany */
  public ComboItem getChosenItem(int index)
  {
    ComboItem aChosenItem = chosenItems.get(index);
    return aChosenItem;
  }

  public List<ComboItem> getChosenItems()
  {
    List<ComboItem> newChosenItems = Collections.unmodifiableList(chosenItems);
    return newChosenItems;
  }

  public int numberOfChosenItems()
  {
    int number = chosenItems.size();
    return number;
  }

  public boolean hasChosenItems()
  {
    boolean has = chosenItems.size() > 0;
    return has;
  }

  public int indexOfChosenItem(ComboItem aChosenItem)
  {
    int index = chosenItems.indexOf(aChosenItem);
    return index;
  }
  /* Code from template association_GetOne */
  public TimeSlot getTimeSlot()
  {
    return timeSlot;
  }
  /* Code from template association_GetOne */
  public FlexiBook getFlexiBook()
  {
    return flexiBook;
  }
  /* Code from template association_SetOneToMany */
  public boolean setCustomer(Customer aCustomer)
  {
    boolean wasSet = false;
    if (aCustomer == null)
    {
      return wasSet;
    }

    Customer existingCustomer = customer;
    customer = aCustomer;
    if (existingCustomer != null && !existingCustomer.equals(aCustomer))
    {
      existingCustomer.removeAppointment(this);
    }
    customer.addAppointment(this);
    wasSet = true;
    return wasSet;
  }
  /* Code from template association_SetOneToMany */
  public boolean setBookableService(BookableService aBookableService)
  {
    boolean wasSet = false;
    if (aBookableService == null)
    {
      return wasSet;
    }

    BookableService existingBookableService = bookableService;
    bookableService = aBookableService;
    if (existingBookableService != null && !existingBookableService.equals(aBookableService))
    {
      existingBookableService.removeAppointment(this);
    }
    bookableService.addAppointment(this);
    wasSet = true;
    return wasSet;
  }
  /* Code from template association_MinimumNumberOfMethod */
  public static int minimumNumberOfChosenItems()
  {
    return 0;
  }
  /* Code from template association_AddUnidirectionalMany */
  public boolean addChosenItem(ComboItem aChosenItem)
  {
    boolean wasAdded = false;
    if (chosenItems.contains(aChosenItem)) { return false; }
    chosenItems.add(aChosenItem);
    wasAdded = true;
    return wasAdded;
  }

  public boolean removeChosenItem(ComboItem aChosenItem)
  {
    boolean wasRemoved = false;
    if (chosenItems.contains(aChosenItem))
    {
      chosenItems.remove(aChosenItem);
      wasRemoved = true;
    }
    return wasRemoved;
  }
  /* Code from template association_AddIndexControlFunctions */
  public boolean addChosenItemAt(ComboItem aChosenItem, int index)
  {  
    boolean wasAdded = false;
    if(addChosenItem(aChosenItem))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfChosenItems()) { index = numberOfChosenItems() - 1; }
      chosenItems.remove(aChosenItem);
      chosenItems.add(index, aChosenItem);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMoveChosenItemAt(ComboItem aChosenItem, int index)
  {
    boolean wasAdded = false;
    if(chosenItems.contains(aChosenItem))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfChosenItems()) { index = numberOfChosenItems() - 1; }
      chosenItems.remove(aChosenItem);
      chosenItems.add(index, aChosenItem);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addChosenItemAt(aChosenItem, index);
    }
    return wasAdded;
  }
  /* Code from template association_SetUnidirectionalOne */
  public boolean setTimeSlot(TimeSlot aNewTimeSlot)
  {
    boolean wasSet = false;
    if (aNewTimeSlot != null)
    {
      timeSlot = aNewTimeSlot;
      wasSet = true;
    }
    return wasSet;
  }
  /* Code from template association_SetOneToMany */
  public boolean setFlexiBook(FlexiBook aFlexiBook)
  {
    boolean wasSet = false;
    if (aFlexiBook == null)
    {
      return wasSet;
    }

    FlexiBook existingFlexiBook = flexiBook;
    flexiBook = aFlexiBook;
    if (existingFlexiBook != null && !existingFlexiBook.equals(aFlexiBook))
    {
      existingFlexiBook.removeAppointment(this);
    }
    flexiBook.addAppointment(this);
    wasSet = true;
    return wasSet;
  }

  public void delete()
  {
    Customer placeholderCustomer = customer;
    this.customer = null;
    if(placeholderCustomer != null)
    {
      placeholderCustomer.removeAppointment(this);
    }
    BookableService placeholderBookableService = bookableService;
    this.bookableService = null;
    if(placeholderBookableService != null)
    {
      placeholderBookableService.removeAppointment(this);
    }
    chosenItems.clear();
    timeSlot = null;
    FlexiBook placeholderFlexiBook = flexiBook;
    this.flexiBook = null;
    if(placeholderFlexiBook != null)
    {
      placeholderFlexiBook.removeAppointment(this);
    }
  }


  /**
   * Method to check if a TimeSlot is within business days and hours. Compares Date and Time.
   * If the TimeSlot overlaps Holiday, vacation and outside of business hours and days, the method will return false.
   * @param serviceTimeSlot: TimeSlot of the bookable service.
   * @param isUpdate: boolean that indicates whether we're currently trying to update an appointment.
   * @param oldTimeSlot: TimeSlot of the appointment that we want to check the validity.
   * @return true or false: boolean that indicates whether the appointment is bookable. "true" if no conflict. "false" if there's a conflict.
   * @author Sandy
   */
  // line 47 "../../../../../FlexiBookStates.ump"
   private boolean isBookable(TimeSlot serviceTimeSlot, Boolean isUpdate, TimeSlot oldTimeSlot){
    //INITIALIZE
    boolean isHoliday = false;   //no conflict with Holiday
    boolean isVacation = false; //no conflict with Vacation
    boolean isOpen = false; // conflict with BusinessHours
    boolean isAppointmentOverlap = false; //conflict with Appointment
    /**BOOKABLESERVICE TIMESLOT***/
    //BOOK START
    Date startBookDate = serviceTimeSlot.getStartDate();  //bookdate
    Time startBookTime = serviceTimeSlot.getStartTime();    //startTime
    Calendar bookStartCal = SystemTime.dateTime2Calendar(startBookDate, startBookTime);  //convert Date&Time==>calendar USE THIS TO COMPARE  Note to myself: tested see SystemTime.dateTime2Calendar.java
    //BOOK END
    Date endBookDate = serviceTimeSlot.getEndDate();
    Time endBookTime = serviceTimeSlot.getEndTime();
    Calendar bookEndCal = SystemTime.dateTime2Calendar(endBookDate, endBookTime);  //convert Date&Time==>calendar USE THIS TO COMPARE

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
      Calendar holidayStartCal = SystemTime.dateTime2Calendar(holidayStartDate, holidayStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE
      //HOLIDAY-END
      Date holidayEndDate = currentHoliday.getEndDate();
      Time holidayEndTime = currentHoliday.getEndTime();
      Calendar holidayEndCal = SystemTime.dateTime2Calendar(holidayEndDate, holidayEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE

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
      Calendar vacationStartCal = SystemTime.dateTime2Calendar(vacationStartDate, vacationStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE

      //VACATION-END
      Date vacationEndDate = currentVacation.getEndDate();
      Time vacationEndTime = currentVacation.getEndTime();
      Calendar vacationEndCal = SystemTime.dateTime2Calendar(vacationEndDate, vacationEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE
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
        Calendar businessStartCal = SystemTime.dateTime2Calendar(businessStartDate, businessStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE

        //BUSINESS-END
        Date businessEndDate = serviceTimeSlot.getEndDate();  // since same weekday, set businessEndDate to be the same as the booking end date
        Time businessEndTime = currentBusinessHour.getEndTime();
        Calendar businessEndCal = SystemTime.dateTime2Calendar(businessEndDate, businessEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE

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
        if (oldAppointment.getTimeSlot().equals(oldTimeSlot)) continue;
      }
      //OLD APPOINTMENT START DATE
      Date appStartDate = oldAppointment.getTimeSlot().getStartDate();
      Time appStartTime = oldAppointment.getTimeSlot().getStartTime();
      Calendar appStartCal = SystemTime.dateTime2Calendar(appStartDate, appStartTime); //convert Date&Time==>calendar USE THIS TO COMPARE

      //OLD APPOINTMENT END DATE
      Date appEndDate = oldAppointment.getTimeSlot().getEndDate();
      Time appEndTime = oldAppointment.getTimeSlot().getEndTime();
      //appEndDate = cleanDate(appEndDate,appEndTime); //USE THIS :)
      Calendar appEndCal = SystemTime.dateTime2Calendar(appEndDate, appEndTime); //convert Date&Time==>calendar USE THIS TO COMPARE
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
            Calendar downtimeStartCal = SystemTime.downtime2Cal(appStartCal, intDowntimeStart);

            int durationStartService2Downtime = intDowntimeStart + intDowntimeDuration; //time elapsed since start of service until end of downtime
            //Date downtimeEnd = downtime2Date(appEndDate, durationStartService2Downtime); //USE THIS
            Calendar downtimeEndCal = SystemTime.downtime2Cal(appEndCal, durationStartService2Downtime);

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
            Calendar downtimeStartCal = SystemTime.downtime2Cal(appStartCal, intDowntimeStart);
            int intDowntimeEnd = oldServ.getDowntimeDuration() + intDowntimeStart;
            Calendar downtimeEndCal = SystemTime.downtime2Cal(appEndCal, intDowntimeEnd);
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


  /**
   * Performs update for appointment for adding and removing services
   * @author Tyler
   * @param bookingService - the bookable service for the appointment
   * @param newTimeSlot - the new time slot for the appointment
   * @param addOptionalServices - the list of optional services to add to the appointment
   * @param removeOptionalServices - the list of optional services to remove from the appointment
   */
  // line 254 "../../../../../FlexiBookStates.ump"
   private void doUpdate(BookableService bookingService, TimeSlot newTimeSlot, List<ComboItem> addOptionalServices, List<ComboItem> removeOptionalServices){
    // remove optional services
    if (removeOptionalServices != null) {
      for (ComboItem toBeRemoved : removeOptionalServices) {
        this.removeChosenItem(toBeRemoved);
      }
    }

    // add optional services
    if (addOptionalServices != null) {

      ServiceCombo serviceCombo = (ServiceCombo) bookingService;
      ArrayList<ComboItem> copy = new ArrayList<>(this.getChosenItems());
      ArrayList<ComboItem> addOptionalServicesCopy = new ArrayList<>(addOptionalServices);

      for (ComboItem existingComboItem : copy) { //LIST B
        int posExistingComboItem = serviceCombo.indexOfService(existingComboItem);
        for (ComboItem item2add : addOptionalServicesCopy) { //UPDATE LIST
          int posItem2Add = serviceCombo.indexOfService(item2add);
          if (posItem2Add < posExistingComboItem) {
            this.addChosenItemAt(item2add, posExistingComboItem);
            addOptionalServices.remove(item2add);
          } else if(copy.indexOf(existingComboItem)==copy.size()-1){
            this.addChosenItem(item2add);
          }
        }
      }
    }

    this.setTimeSlot(newTimeSlot);
  }


  /**
   * Checks if a timeslot starts on the same day as the system date
   * @author Tyler
   * @param oldTimeSlot - the old time slot to compare
   */
  // line 289 "../../../../../FlexiBookStates.ump"
   private boolean dayOf(TimeSlot oldTimeSlot){
    //take date, compare with system date
    return oldTimeSlot.getStartDate().equals(SystemTime.getSystemDate());
  }


  /**
   * Increments the appointment's customer's noShow count by 1
   * @author Tyler
   */
  // line 296 "../../../../../FlexiBookStates.ump"
   private void incrementNoShow(){
    // increment customer's noShow attribute by 1
    this.getCustomer().setNoShow(this.getCustomer().getNoShow() + 1);
  }
  
  //------------------------
  // DEVELOPER CODE - PROVIDED AS-IS
  //------------------------
  
  // line 22 "../../../../../FlexiBookPersistence.ump"
  private static final long serialVersionUID = 2315072607928790501L ;

  
}