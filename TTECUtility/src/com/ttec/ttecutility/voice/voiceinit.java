package com.ttec.ttecutility.voice;



import com.ttec.session.SessionObject;

import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Say.Language;
import com.twilio.twiml.voice.Say.Voice;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.voice.Play;

import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Number;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.VoiceResponse.Builder;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import db.JdbcConnection;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servlet implementation class voiceinit
 */
@WebServlet("/voiceinit")
public class voiceinit extends HttpServlet {
	
	private Voice voiceObject=Say.Voice.MAN;
	private Language languageObject=Language.EN_US;;
	
	Logger logger = LogManager.getLogger(voiceinit.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public voiceinit() {
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
			logger.info("call from: " + fromNumber);

			SessionObject SessionObject=GetSessionFromDB(fromNumber);

			
			logger.info("Current StateName is: "+SessionObject.getState());	
			logger.info("Voice call from: "+fromNumber);

			VoiceResponse twiml=null;
			
			Builder Buildeer=new VoiceResponse.Builder();
			if(SessionObject.getName()!=null&&SessionObject.getName().length()>0) {
				String Name=SessionObject.getName();
				logger.info("Existing session. Name is: "+Name);
				String ref=SessionObject.getRefNum()+"";
				ref=ref.replaceAll(".(?=.)", "$0 ");
				String TTS="Hello "+Name+". Welcome to T-TECH Utility. Your Reference Number is <say-as interpret-as=\"digits\">"+ref+"</say-as>";					
				logger.info("Buidling TTS Object to say: "+TTS);
				Say say = new Say.Builder(TTS).voice(voiceObject).language(languageObject).build();
				Buildeer.say(say);	
				
				TTS="Let me connect you to an agent in "+SessionObject.getIntent()+" departement. Your call will be monitored for quality and training purposes.";				
				logger.info("Buidling TTS Object to say: "+TTS);
				 say = new Say.Builder(TTS).voice(voiceObject).language(languageObject).build();
				Buildeer.say(say);	
				
				Play play = new Play.Builder("http://com.twilio.sounds.music.s3.amazonaws.com/MARKOVICHAMP-Borghestral.mp3").build();
				Number number = new Number.Builder("15122412500")
			            .sendDigits(""+SessionObject.getRefNum()).build();
			        Dial dial = new Dial.Builder().number(number).build();
			        VoiceResponse  voiceResponse = new VoiceResponse.Builder().dial(dial).build();
				Buildeer.dial(dial);
			}else {
				String TTS="Hi. Welcome to T-TECH Utilities.";					
				logger.info("Buidling TTS Object to say: "+TTS);
				Say say = new Say.Builder(TTS).voice(voiceObject).language(languageObject).build();
				Buildeer.say(say);	
				
				TTS="In your own words, please tell me what I can do for  you today?";				
				logger.info("Buidling TTS Object to say: "+TTS);
				say = new Say.Builder(TTS).voice(voiceObject).language(languageObject).build();
				Buildeer.say(say);	
				
				TTS="THIS IS THE END OF THE DEMO. GOODBYE";				
				logger.info("Buidling TTS Object to say: "+TTS);
				say = new Say.Builder(TTS).voice(voiceObject).language(languageObject).build();
				Buildeer.say(say);	
			}
			
			twiml = Buildeer.build();	
			response.setContentType("application/xml");
			response.getWriter().print(twiml.toXml());

		} catch (TwiMLException e) {
			e.printStackTrace();
		}
	}
	
	private SessionObject GetSessionFromDB(String PhoneNumber) {
		JdbcConnection JdbcConnection=new JdbcConnection();
		
		SessionObject SessionObject=null;
		
		String Query="SELECT \"sessionId\", name, intent, tag FROM public.\"Sessions\" where \"sessionId\" like '%"+PhoneNumber+"';";
		logger.info(Query); 
		ResultSet RS=JdbcConnection.ExecuteQuery(Query);
		try {
			if (RS.next()) {
				SessionObject = new SessionObject(RS.getString("sessionId"));
				SessionObject.setName(RS.getString("name"));
				SessionObject.setIntent(RS.getString("intent"));
				SessionObject.setRefNum(RS.getInt("tag"));
			}	
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.toString());
			e.printStackTrace();
		}
		if(SessionObject==null) {
			SessionObject= new SessionObject(PhoneNumber);
		}
		return SessionObject;
	}

	

}
