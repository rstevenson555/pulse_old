/*
 * Created on Dec 31, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.bos.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author I0360D4
 *
 * This class does not use persistance.
 */
public class CalendarBean implements Serializable {

	private String SUNDAY_STR= "Sunday";
	private String MONDAY_STR= "Monday";
	private String TUESDAY_STR= "Tuesday";
	private String WEDNESDAY_STR= "Wednesday";
	private String THURSDAY_STR= "Thursday";
	private String FRIDAY_STR= "Friday";
	private String SATURDAY_STR= "Saturday";
	
	private String JANUARY_STR= "January";
	private String FEBUARY_STR= "Febuary";
	private String MARCH_STR= "March";
	private String APRIL_STR= "April";
	private String MAY_STR= "May";
	private String JUNE_STR= "June";
	private String JULY_STR= "July";
	private String AUGUST_STR= "August";
	private String SEPTEMBER_STR= "September";
	private String OCTOBER_STR= "October";
	private String NOVEMBER_STR= "November";
	private String DECEMBER_STR= "December";
		
	private int prevMonthLastDate;
	
	private int prevMonthLastDayOfWeek;
	
	private int prevMonth;
	
	private int prevMonthYear;
	
	private String currentDay;
	
	private int currentDate;
	
	private int currentMonth;
	
	private String currentMonthName;
	
	private int currentMonthLastDate;

	private int currentYear;
	
	private int nextMonth;
	
	private int nextMonthYear;
	
	private int todaysDate;
	
	private int todaysMonth;
	
	private int todaysYear;

	/** full constructor */
	public CalendarBean(Calendar calendar) {
		Date date = calendar.getTime();
		int year = 0;
		int month = 0;
		int datei = 0;

		Calendar prevMonthCalendar = (Calendar) calendar.clone();
		Calendar nextMonthCalendar = (Calendar) calendar.clone();
		
			System.out.println("+++++++++++ selectedDate:"+date.toString());
			//get calendar for selected DATE
			year= date.getYear();
			month= date.getMonth();
			datei= date.getDate();
			
//			prevMonthCalendar.set(year+1900, month , datei);
//			nextMonthCalendar.set(year+1900, month , datei);			
		
		
		System.out.println("#########################################################");			
		System.out.println("# Currently Selected Date: "+calendar.getTime());
		prevMonthCalendar.add(Calendar.MONTH, -1);
		System.out.println("# Previous Month (Selected -1month): "+prevMonthCalendar.getTime());
		
		nextMonthCalendar.add(Calendar.MONTH, 1);
		System.out.println("# Next Month (Selected +1month): "+nextMonthCalendar.getTime());
		System.out.println("#########################################################");
				
		this.setPrevMonthLastDate(prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		int max = prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int curr = prevMonthCalendar.get(Calendar.DAY_OF_MONTH);
		int diff = max - curr;
 
		System.out.println("** diff: "+diff);
		prevMonthCalendar.add(Calendar.DAY_OF_MONTH, diff);
		System.out.println("# Previous Month (Selected -1month): "+prevMonthCalendar.getTime());
		
		System.out.println("**prev Month last day of week: "+ prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		
		this.setPrevMonthLastDayOfWeek(prevMonthCalendar.get(Calendar.DAY_OF_WEEK));
		this.setPrevMonth(prevMonthCalendar.get(Calendar.MONTH)+1);  //need to add 1 to make month range 1-12, Calendar API uses 0-11
		this.setPrevMonthYear(prevMonthCalendar.get(Calendar.YEAR));		
		
		this.setCurrentDay(calendar.get(Calendar.DAY_OF_WEEK));
		this.setCurrentDate(calendar.get(Calendar.DAY_OF_MONTH));
		this.setCurrentMonth(calendar.get(Calendar.MONTH)+1);	//need to add 1 to make month range 1-12, Calendar API uses 0-11
		this.setCurrentMonthName(calendar.get(Calendar.MONTH));
		this.setCurrentYear(calendar.get(Calendar.YEAR));
		this.setCurrentMonthLastDate(calendar.getMaximum(Calendar.DAY_OF_MONTH));
		
		this.setNextMonth(nextMonthCalendar.get(Calendar.MONTH)+1);
		this.setNextMonthYear(nextMonthCalendar.get(Calendar.YEAR));
		
		Calendar todaysCalendar = Calendar.getInstance();
		this.setTodaysDate(todaysCalendar.get(Calendar.DATE));
		this.setTodaysMonth(todaysCalendar.get(Calendar.MONTH)+1); 	//need to add 1 to make month range 1-12, Calendar API uses 0-11
		this.setTodaysYear(todaysCalendar.get(Calendar.YEAR));
		
	}

	public org.w3c.dom.Document toDOM() {
		Document doc = null;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;		

		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
		} catch (Exception ex) {
		}
		Element calendarBean = doc.createElement("CalendarBean");

		//previous month elements
		Element previousMonthLastDateElement = doc.createElement("PreviousMonthLastDate");
		previousMonthLastDateElement.appendChild(doc.createTextNode(this.getPrevMonthLastDate()+""));
		Element previousMonthLastDayOfWeekElement = doc.createElement("PreviousMonthLastDayOfWeek");
		previousMonthLastDayOfWeekElement.appendChild(doc.createTextNode(this.getPrevMonthLastDayOfWeek()+""));
		Element previousMonthNumberElement = doc.createElement("PreviousMonthNumber");
		previousMonthNumberElement.appendChild(doc.createTextNode(this.getPrevMonth()+""));
		Element previousMonthYearElement = doc.createElement("PreviousMonthYear");
		previousMonthYearElement.appendChild(doc.createTextNode(this.getPrevMonthYear()+""));
		
		//current month elements
		Element currentDayElement = doc.createElement("CurrentDay");
		currentDayElement.appendChild(doc.createTextNode(this.getCurrentDay()));
		Element currentDateElement = doc.createElement("CurrentDate");
		currentDateElement.appendChild(doc.createTextNode(this.getCurrentDate()+""));		
		Element currentMonthElement = doc.createElement("CurrentMonth");
		currentMonthElement.appendChild(doc.createTextNode(this.getCurrentMonthName()));
		Element currentMonthNumberElement = doc.createElement("CurrentMonthNumber");
		currentMonthNumberElement.appendChild(doc.createTextNode(String.valueOf(this.getCurrentMonth())));
		
		Element currentMonthLastDateElement = doc.createElement("CurrentMonthLastDate");
		currentMonthLastDateElement.appendChild(doc.createTextNode(this.getCurrentMonthLastDate()+""));
		
		Element currentYearElement = doc.createElement("CurrentYear");
		currentYearElement.appendChild(doc.createTextNode(this.getCurrentYear()+""));
		
		//next month elements
		Element nextMonthNumberElement = doc.createElement("NextMonthNumber");
		nextMonthNumberElement.appendChild(doc.createTextNode(this.getNextMonth()+""));
		Element nextMonthYearElement = doc.createElement("NextMonthYear");
		nextMonthYearElement.appendChild(doc.createTextNode(this.getNextMonthYear()+""));

		//todays date elements
		Element todaysDateElement = doc.createElement("TodaysDate");
		todaysDateElement.appendChild(doc.createTextNode(this.getTodaysDate()+""));
		Element todaysMonthElement = doc.createElement("TodaysMonth");
		todaysMonthElement.appendChild(doc.createTextNode(this.getTodaysMonth()+""));
		Element todaysYearElement = doc.createElement("TodaysYear");
		todaysYearElement.appendChild(doc.createTextNode(this.getTodaysYear()+""));
				
		//append previous month elements
		calendarBean.appendChild(previousMonthLastDateElement);
		calendarBean.appendChild(previousMonthLastDayOfWeekElement);
		calendarBean.appendChild(previousMonthNumberElement);
		calendarBean.appendChild(previousMonthYearElement);
		
		calendarBean.appendChild(currentDayElement);
		calendarBean.appendChild(currentDateElement);
		calendarBean.appendChild(currentMonthElement);
		calendarBean.appendChild(currentMonthNumberElement);
		calendarBean.appendChild(currentMonthLastDateElement);
		calendarBean.appendChild(currentYearElement);
		
		calendarBean.appendChild(nextMonthNumberElement);
		calendarBean.appendChild(nextMonthYearElement);
		
		calendarBean.appendChild(todaysDateElement);
		calendarBean.appendChild(todaysMonthElement);
		calendarBean.appendChild(todaysYearElement);
		
		doc.appendChild(calendarBean);
		
		return doc;		
	}
	/** default constructor */
	public CalendarBean() {
	}

	/**
	 * @return Returns the prevMonthLastDate.
	 */
	public int getPrevMonthLastDate() {
		return prevMonthLastDate;
	}
	
	/**
	 * @param prevMonthLastDate The prevMonthLastDate to set.
	 */
	public void setPrevMonthLastDate(int prevMonthLastDate) {
		this.prevMonthLastDate = prevMonthLastDate;
	}

	/**
	 * @return Returns the prevMonthLastDate.
	 */
	public int getPrevMonthLastDayOfWeek() {
		return prevMonthLastDayOfWeek;
	}
	
	/**
	 * @param prevMonthLastDate The prevMonthLastDate to set.
	 */
	public void setPrevMonthLastDayOfWeek(int prevMonthLastDay) {
		this.prevMonthLastDayOfWeek = prevMonthLastDay;
	}
	
	/**
	 * @return Returns the currentDay.
	 */
	public String getCurrentDay() {
		return currentDay;
	}
	
	/**
	 * @param currentDay The currentDay to set.
	 */
	public void setCurrentDay(int currentDay) {
		if (currentDay == Calendar.SUNDAY) {
			this.currentDay = SUNDAY_STR;
		} else if (currentDay == Calendar.MONDAY) {
			this.currentDay = MONDAY_STR;			
		} else if (currentDay == Calendar.TUESDAY) {
			this.currentDay = TUESDAY_STR;			
		} else if (currentDay == Calendar.WEDNESDAY) {
			this.currentDay = WEDNESDAY_STR;			
		} else if (currentDay == Calendar.THURSDAY) {
			this.currentDay = THURSDAY_STR;			
		} else if (currentDay == Calendar.FRIDAY) {
			this.currentDay = FRIDAY_STR;			
		} else {
			this.currentDay = SATURDAY_STR;			
		}
	}

	/**
	 * @return Returns the currentDate.
	 */
	public int getCurrentDate() {
		return currentDate;
	}
	
	/**
	 * @param currentDay The currentDay to set.
	 */
	public void setCurrentDate(int currentDate) {
		this.currentDate = currentDate;
	}
		
	/**
	 * @return Returns the currentMonth.
	 */
	public int getCurrentMonth() {
		return currentMonth;
	}

	/**
	 * @param currentMonth The currentMonth to set.
	 */
	public void setCurrentMonth(int currentMonth) {
		this.currentMonth = currentMonth;
		this.setCurrentMonthName(currentMonth);
	}

	/**
	 * @return Returns the currentMonthName.
	 */
	public String getCurrentMonthName() {
		return currentMonthName;
	}

	/**
	 * @param currentMonthName The currentMonthName to set.
	 */
	public void setCurrentMonthName(int currentMonth) {
		if (currentMonth == Calendar.JANUARY) {
			this.currentMonthName = this.JANUARY_STR;
		} else if (currentMonth == Calendar.FEBRUARY) {
			this.currentMonthName = this.FEBUARY_STR;
		} else if (currentMonth == Calendar.MARCH) {
			this.currentMonthName = this.MARCH_STR;
		} else if (currentMonth == Calendar.APRIL) {
			this.currentMonthName = this.APRIL_STR;
		} else if (currentMonth == Calendar.MAY) {
			this.currentMonthName = this.MAY_STR;
		} else if (currentMonth == Calendar.JUNE) {
			this.currentMonthName = this.JUNE_STR;
		} else if (currentMonth == Calendar.JULY) {
			this.currentMonthName = this.JULY_STR;
		} else if (currentMonth == Calendar.AUGUST) {
			this.currentMonthName = this.AUGUST_STR;
		} else if (currentMonth == Calendar.SEPTEMBER) {
			this.currentMonthName = this.SEPTEMBER_STR;
		} else if (currentMonth == Calendar.OCTOBER) {
			this.currentMonthName = this.OCTOBER_STR;
		} else if (currentMonth == Calendar.NOVEMBER) {
			this.currentMonthName = this.NOVEMBER_STR;
		} else {
			this.currentMonthName = this.DECEMBER_STR;
		}
	}
	
	/**
	 * @return Returns the currentYear.
	 */
	public int getCurrentYear() {
		return currentYear;
	}

	/**
	 * @param currentYear The currentYear to set.
	 */
	public void setCurrentYear(int currentYear) {
		this.currentYear = currentYear;
	}
	
	/**
	 * @return Returns the currentMonthLastDate.
	 */
	public int getCurrentMonthLastDate() {
		return currentMonthLastDate;
	}

	/**
	 * @param currentMonthLastDate The currentMonthLastDate to set.
	 */
	public void setCurrentMonthLastDate(int currentMonthLastDate) {
		this.currentMonthLastDate = currentMonthLastDate;
	}
	
	/**
	 * @return Returns the prevMonth.
	 */
	public int getPrevMonth() {
		return prevMonth;
	}

	/**
	 * @param prevMonth The prevMonth to set.
	 */
	public void setPrevMonth(int prevMonth) {
		this.prevMonth = prevMonth;
	}

	/**
	 * @return Returns the prevMonthYear.
	 */
	public int getPrevMonthYear() {
		return prevMonthYear;
	}

	/**
	 * @param prevMonthYear The prevMonthYear to set.
	 */
	public void setPrevMonthYear(int prevMonthYear) {
		this.prevMonthYear = prevMonthYear;
	}

	/**
	 * @return Returns the nextMonth.
	 */
	public int getNextMonth() {
		return nextMonth;
	}

	/**
	 * @param nextMonth The nextMonth to set.
	 */
	public void setNextMonth(int nextMonth) {
		this.nextMonth = nextMonth;
	}

	/**
	 * @return Returns the nextMonthYear.
	 */
	public int getNextMonthYear() {
		return nextMonthYear;
	}

	/**
	 * @param nextMonthYear The nextMonthYear to set.
	 */
	public void setNextMonthYear(int nextMonthYear) {
		this.nextMonthYear = nextMonthYear;
	}

	/**
	 * @return Returns the todaysDate.
	 */
	public int getTodaysDate() {
		return todaysDate;
	}

	/**
	 * @param todaysDate The todaysDate to set.
	 */
	public void setTodaysDate(int todaysDate) {
		this.todaysDate = todaysDate;
	}

	/**
	 * @return Returns the todaysMonth.
	 */
	public int getTodaysMonth() {
		return todaysMonth;
	}

	/**
	 * @param todaysMonth The todaysMonth to set.
	 */
	public void setTodaysMonth(int todaysMonth) {
		this.todaysMonth = todaysMonth;
	}

	/**
	 * @return Returns the todaysYear.
	 */
	public int getTodaysYear() {
		return todaysYear;
	}

	/**
	 * @param todaysYear The todaysYear to set.
	 */
	public void setTodaysYear(int todaysYear) {
		this.todaysYear = todaysYear;
	}

}
