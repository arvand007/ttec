package com.ttec.session;

import java.util.Random;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SessionObject {	
	private String PhoneNumber;
	//init - when the call is starting
	//name - waiting for firstname	
	//preintent - waiting for the customer to provide intent for the call
	//postintent - we know what the intent of the calll is
	//Goto:Channel:Destination - queue name or phonenumebr to be transfered to
	//For example "Goto:Text:Customer Service" or   "Goto:Voice:Customer Service"
	private String State;
	private String Name;
	private String Intent;
	private int RefNum;
	
	public int getRefNum() {
		return RefNum;
	}


	public void setRefNum(int refNum) {
		RefNum = refNum;
	}


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
		RefNum=getRandomNumberInRange(300000,999999);
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
	
	private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
}
