package com.ttec.ttecutility.sms;



import com.ttec.session.SessionObject;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.MessagingResponse.Builder;
import com.twilio.twiml.TwiMLException;


import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import db.JdbcConnection;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servlet implementation class Init
 */
@WebServlet("/init")
public class Init extends HttpServlet {
	Logger logger = LogManager.getLogger(Init.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Init() {
		super();
		// TODO Auto-generated constructor stub
	}


	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String fromNumber=request.getParameter("From");
			String Body=request.getParameter("Body");
			String AddOns=request.getParameter("AddOns");			
			DateTime localDateTime = new DateTime();
			DateTimeZone dtz = DateTimeZone.forID("America/New_York");
			logger.info("localDateTime : " + localDateTime.toString());


			HttpSession session = request.getSession(true);
			

			//inacticity timer for session set to 1 hour
			session.setMaxInactiveInterval(3600);
			SessionObject SessionObject = (SessionObject) session.getAttribute("SessionObject_"+fromNumber);
			if (SessionObject==null) {
				SessionObject = new SessionObject(fromNumber);
			} 

			session.setAttribute("SessionObject_"+fromNumber, SessionObject);   


			DateTime CreationTime = new DateTime(Long.valueOf(session.getCreationTime()), DateTimeZone.getDefault());
			logger.info("Time of the session creation: "+CreationTime);
			logger.info("Current StateName is: "+SessionObject.getState());	
			logger.info("SMS from: "+fromNumber);

			UpdateSessionDB(SessionObject.getPhoneNumber(), SessionObject.getName(), SessionObject.getIntent(), SessionObject.getState(),SessionObject.getRefNum());

			Builder Buildeer=new MessagingResponse.Builder();       
			Message sms=null;
			MessagingResponse twiml=null;

			//canned responses


			if(Body.toLowerCase().indexOf("How are you")>-1||Body.toLowerCase().indexOf("How are you doing")>-1||Body.toLowerCase().indexOf("How do you do")>-1||Body.toLowerCase().indexOf("How r you")>-1
					||Body.toLowerCase().indexOf("How r u")>-1||Body.toLowerCase().indexOf("How are u doing")>-1||Body.toLowerCase().indexOf("How r u doing")>-1||Body.toLowerCase().indexOf("Whats up")>-1
					||Body.toLowerCase().indexOf("whuzza")>-1){	
				sms = new Message.Builder().body(new Body.Builder("I'm fine. Thanks").build()).build();  
				Buildeer.message(sms);   
			}



			if(SessionObject.getState().equals("init")) {
				String CustomerName=GetCustomerName(fromNumber);
				if(CustomerName.equals("xxxx")) {

					//unknown customer;
					sms = new Message.Builder().body(new Body.Builder("Hi. Welcome to TTEC Utility. My Name is Nemo.\n"
							+ "I don't seem to recognize your number.\n"
							+ "Could you please tell me what your name is?").build()).build();  
					Buildeer.message(sms);   
					SessionObject.setState("name");
					logger.info("Setting state name to \"name\"");    
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
				}else {
					SessionObject.setName(CustomerName);
					SessionObject.setState("preintent");
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
					sms = new Message.Builder().body(new Body.Builder("Hello "+CustomerName+". Welcome to TTEC Utility.\n"
							+ "My Name is Nemo.\n"
							+ "In your own words, please tell me how I can help you today?").build()).build();   
					Buildeer.message(sms);           
				}        	


			}else if(SessionObject.getState().equals("name")) {
				String Name=GetcustomerNamefromtext(Body);
				if(Name.length()>0) {
					SessionObject.setName(Name);
					SessionObject.setState("preintent");
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
					sms = new Message.Builder().body(new Body.Builder("Ok "+Name+". Nice to meet you.\n"
							+ "In your own words, please tell me how I can help you today?" ).build()).build();  
					logger.info("Name is: "+Name);  
				}else {
					SessionObject.setName("Error");
					SessionObject.setState("preintent");
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
					sms = new Message.Builder().body(new Body.Builder("Ok I did not get your name. Let's conitnue...\n"
							+ "In your own words, please tell me how can I help you today?" ).build()).build();  
					logger.info("Name is: Error");  
					logger.info("Trying to  intent now"); 
				}
				logger.info("Trying to collect intent now"); 	 
				Buildeer.message(sms);           
			}else if(SessionObject.getState().equals("preintent")) {
				String intent="Customer Service";
				String intentText=Body;
				if(intentText.toLowerCase().indexOf("bill")>-1||intentText.toLowerCase().indexOf("usuage")>-1||intentText.toLowerCase().indexOf("consumption")>-1||
						intentText.toLowerCase().indexOf("meter")>-1||intentText.toLowerCase().indexOf("disconnected")>-1||intentText.toLowerCase().indexOf("reconnection")>-1
						||intentText.toLowerCase().indexOf("re-connection")>-1||intentText.toLowerCase().indexOf("reconnecting")>-1||intentText.toLowerCase().indexOf("reconnecting")>-1) {
					intent="Billing";
				}else if(intentText.toLowerCase().indexOf("no service")>-1||intentText.toLowerCase().indexOf("distruption")>-1||
						intentText.toLowerCase().indexOf("issue")>-1||intentText.toLowerCase().indexOf("flickering")>-1||intentText.toLowerCase().indexOf("smell")>-1||
						intentText.toLowerCase().indexOf("no gas")>-1||intentText.toLowerCase().indexOf("no electricity")>-1||intentText.toLowerCase().indexOf("no power")>-1||
						intentText.toLowerCase().indexOf("outage")>-1||intentText.toLowerCase().indexOf("interuption")>-1){
					intent="Outage";
				}else if(intentText.toLowerCase().indexOf("new service")>-1||intentText.toLowerCase().indexOf("purchase")>-1||intentText.toLowerCase().indexOf("additional")>-1) {
					intent="New Service";
				}
				logger.info("Text is: "+Body);  
				logger.info("Intent is: "+intent);  
				SessionObject.setIntent(intent);
				SessionObject.setState("postintent");
				session.setAttribute("SessionObject_"+fromNumber, SessionObject);
				sms = new Message.Builder().body(new Body.Builder("Ok. I will get you an agent in our "+intent+" departament.\n"
						+ "Do you perefer to continue using text message or do you prefer to talk to someone over the phone?\n"
						+ "For example type in \"Continue\" or type in \"Phone\"").build()).build();  
				Buildeer.message(sms);   
			}else if(SessionObject.getState().equals("postintent")) {
				//using voice
				if(Body.toLowerCase().indexOf("talk")>-1||Body.toLowerCase().indexOf("phone")>-1||Body.toLowerCase().indexOf("voice")>-1) {
					String intent=SessionObject.getIntent();
					SessionObject.setState("Goto:Voice:"+intent);
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
					sms = new Message.Builder().body(new Body.Builder("Ok. You prefer to talk to someone at "+intent+" departament. Please dial 1 (678) 249-3797 within next 60 minutes from your current phone and we will continue the conversation there.\n"
							+ "Your reference number for this conversation is "+SessionObject.getRefNum()+"\n"
							+"Thanks and talk to you soon.").build()).build();  
					Buildeer.message(sms);   

				}else {
					String intent=SessionObject.getIntent();
					SessionObject.setState("Goto:Text:"+intent);  
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
					sms = new Message.Builder().body(new Body.Builder("Ok. We will continue in Text. Please wait while I get you one our "+intent+" agents.").build()).build();  
					Buildeer.message(sms);   

					sms = new Message.Builder().body(new Body.Builder("THANK YOU FOR TRYING OUT THIS DEMO. THE CONVERSATION WILL END NOW").build()).build();  
					Buildeer.message(sms); 
					//resetting the session
					SessionObject.setState("init");  
					SessionObject.setName("");
					SessionObject.setIntent("");					
					session.setAttribute("SessionObject_"+fromNumber, SessionObject); 
					session.removeAttribute("SessionObject_"+fromNumber);
				}
				logger.info("trasnfering the conversation to: "+SessionObject.getState());
			}else { //default
				String Name=SessionObject.getName();
				if(Name.length()>0) {
					SessionObject.setName(Name);
					SessionObject.setState("preintent");
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
					sms = new Message.Builder().body(new Body.Builder("Ok "+Name+".\n"
							+ "In your own words, please tell me how I can help you today?" ).build()).build();  
				}else {
					SessionObject.setName("");
					SessionObject.setState("preintent");
					session.setAttribute("SessionObject_"+fromNumber, SessionObject);
					sms = new Message.Builder().body(new Body.Builder("Let's conitnue...\n"
							+ "In your own words, please tell me how I can help you today?" ).build()).build();  
				}
				logger.info("Trying to collect intent now"); 
				Buildeer.message(sms);      
			}
			UpdateSessionDB(SessionObject.getPhoneNumber(), SessionObject.getName(), SessionObject.getIntent(), SessionObject.getState(),SessionObject.getRefNum());
			twiml=Buildeer.build();
			response.setContentType("application/xml");
			response.getWriter().print(twiml.toXml());



		} catch (TwiMLException e) {
			e.printStackTrace();
		}
	}
	private void UpdateSessionDB(String PhoneNumber, String Name, String Intent, String State, int RefNum) {
		JdbcConnection JdbcConnection=new JdbcConnection();
		if (State==null)State="";
		if (Name==null)Name="";
		if (Intent==null)Intent="";

		if(State.equalsIgnoreCase("init")) {
			String Query="DELETE FROM public.\"Sessions\" WHERE \"sessionId\"='"+PhoneNumber+"'";
			logger.info(Query); 
			JdbcConnection.executeUpdate(Query);
			Query="INSERT INTO public.\"Sessions\"(\"sessionId\", name, intent, tag) VALUES ('"+PhoneNumber+"','"+Name+"','"+Intent+"','"+RefNum+"')";
			logger.info(Query); 
			JdbcConnection.executeUpdate(Query); 

		}else{
			String Query="UPDATE public.\"Sessions\" SET  name='"+Name+"', intent='"+Intent+"'	WHERE \"sessionId\"='"+PhoneNumber+"'";
			logger.info(Query); 
			JdbcConnection.executeUpdate(Query);
		}
		JdbcConnection.CloseConnection();
	}
	private String GetcustomerNamefromtext(String Body) {
		//its Arvand OWji
		//my name is arvand
		//the name is arvand
		//it's arvand
		//arvand

		Body=Body.toLowerCase();
		
		if(Body.indexOf("its ")==0) {
			Body=Body.replace("its ", "");
		}
		
		if(Body.indexOf("Sure!")>0) {
			Body=Body.replace("Sure!", "");
		}
		Body=Body.replaceAll("[^a-zA-Z0-9]", " ");  

		Body=Body.replace("my name is", "");
		Body=Body.replace("the name is ", "");
		if(Body.indexOf("it's ")==0) {
			Body=Body.replace("it's ", "");
		}
		Body=Body.trim();    
		if(Body.length()==0) {
			Body="";
		}else {
			logger.info("Body: "+Body);
			if(Body.indexOf(" ")>0) {
				String FirstName=Body.substring(0,Body.indexOf(" ")).trim();
				FirstName=FirstName.substring(0, 1).toUpperCase() + FirstName.substring(1);
				String LastName=Body.substring(Body.indexOf(" ")).trim();
				LastName=LastName.substring(0, 1).toUpperCase() + LastName.substring(1);
				Body=FirstName+" "+LastName;
			}else {
				Body=Body.substring(0, 1).toUpperCase() + Body.substring(1);
			}
			if(Body.split("\\s+").length>3) {
				Body="";
			}
		}

		return Body;
	}

	private String GetCustomerName(String PhoneNumber) {
		String Name="";
		JdbcConnection JdbcConnection=new JdbcConnection();
		PhoneNumber=PhoneNumber.replace("+1", "");
		String Query="SELECT * FROM public.\"Users\" where \"CellPhone\" like '%"+PhoneNumber+"';";
		logger.info(Query); 
		ResultSet RS=JdbcConnection.ExecuteQuery(Query);
		try {
			if (RS.next()) {			
				Name=(RS.getString("Name"));				
			}	

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.toString());
			e.printStackTrace();
		}

		if (Name.length()>0) {
			logger.info("Customer Found in CRM: "+Name);
			return Name;
		}else {
			logger.info("Customer Not Found in CRM.");
			return "xxxx";
		}

	}

}
