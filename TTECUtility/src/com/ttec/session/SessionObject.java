package com.ttec.session;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SessionObject {	
	String PhoneNumber;
	//init - when the call is starting
	//name - waiting for firstname	
	//preintent - waiting for the customer to provide intent for the call
	//postintent - we know what the intent of the calll is
	//Goto:Channel:Destination - queue name or phonenumebr to be transfered to
	//For example "Goto:Text:Customer Service" or   "Goto:Voice:Customer Service"
	String State;
	String Name;
	String Intent;
	
	
	public String getIntent() {
		return Intent;
	}


	public void setIntent(String intent) {
		Intent = intent;
	}


	public String getName() {
		return Name;
	}


	public void setName(String name) {
		Name = name;
	}


	public SessionObject(String phoneNumber) {
		super();	
		PhoneNumber = phoneNumber;
		State="init";
		Intent="Customer Service";
	}
	
	
	public String getState() {
		return State;
	}


	public void setState(String state) {
		State = state;
	}


	public String getPhoneNumber() {
		return PhoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		PhoneNumber = phoneNumber;
	}
	
}
