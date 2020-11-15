/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.30.1.5099.60569f335 modeling language!*/

package ca.mcgill.ecse.flexibook.controller;

// line 28 "../../../../../../model.ump"
// line 67 "../../../../../../model.ump"
public class TOBusinessHour
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //TOBusinessHour Attributes
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public TOBusinessHour(String aDayOfWeek, String aStartTime, String aEndTime)
    {
        dayOfWeek = aDayOfWeek;
        startTime = aStartTime;
        endTime = aEndTime;
    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setDayOfWeek(String aDayOfWeek)
    {
        boolean wasSet = false;
        dayOfWeek = aDayOfWeek;
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

    public boolean setEndTime(String aEndTime)
    {
        boolean wasSet = false;
        endTime = aEndTime;
        wasSet = true;
        return wasSet;
    }

    public String getDayOfWeek()
    {
        return dayOfWeek;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void delete()
    {}


    public String toString()
    {
        return super.toString() + "["+
                "dayOfWeek" + ":" + getDayOfWeek()+ "," +
                "startTime" + ":" + getStartTime()+ "," +
                "endTime" + ":" + getEndTime()+ "]";
    }
}