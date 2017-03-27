package app.model;

import java.util.HashMap;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import app.Parser;

public class SMTPModel {
	private HashMap<String, Properties> hostMap;
	private String emailAddress;
	private Session session;
		
	public SMTPModel(String email, String password) {
		emailAddress = email;
		hostMap = new HashMap<String, Properties>();
		hostMap.put("gmail", getGoogleProperties());
		hostMap.put("uwindsor", getGoogleProperties());
		hostMap.put("hotmail", getOutlookProperties());
		hostMap.put("live", getOutlookProperties());
		hostMap.put("outlook", getOutlookProperties());
		
		String hostKey = email.split("@")[1].split("\\.")[0];
		String userName = email.split("@")[0];
		
		// Setup mail server
		session = Session.getInstance(hostMap.get(hostKey), 
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
		});
	}
	
	public String getEmailAdress(){ return emailAddress; }
	public Session getSession(){ return session; }
	
	// Method to send an email using the current session
	public void sendHTMLMessage(String toList, String ccList, String bccList, String subject, String htmlContent) throws AddressException, MessagingException{
		Message message = Parser.createMessage(session, emailAddress, toList, ccList, bccList, subject, htmlContent);

	    Transport.send(message); // Send message
	}
	
	private Properties getGoogleProperties(){
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com"); 
		props.put("mail.smtp.port", "587");
		return props;
	}
	
	private Properties getOutlookProperties(){
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp-mail.outlook.com"); 
		props.put("mail.smtp.port", "587");
		return props;
	}
}
