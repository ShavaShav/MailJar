package app.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

public class MailboxModel {
	Store store;
	HashMap<String, String> hostMap;
	
	public MailboxModel(String email, String password) throws MessagingException, IOException{
		hostMap = new HashMap<String, String>();
		hostMap.put("gmail", "imap.gmail.com");
		hostMap.put("hotmail", "imap-mail.outlook.com");
		hostMap.put("live", "imap-mail.outlook.com");
		hostMap.put("outlook", "imap-mail.outlook.com");
		
		//create properties fieldHashMap
		Properties properties = new Properties();
		
		String hostKey = email.split("@")[1].split("\\.")[0];
		String host = hostMap.get(hostKey);
		System.out.println(host);
		
		properties.put("mail.pop3s.host", host);
		properties.put("mail.pop3s.port", "995");
		properties.put("mail.pop3s.starttls.enable", "true");
		Session emailSession = Session.getDefaultInstance(properties);
  
      //create the IMAP store object and connect with the imap server
		Store store = emailSession.getStore("imaps");
		store.connect(host, email, password);
	
		 // List all folders under root directory
	      Folder[] folders = store.getDefaultFolder().list("*");
	      System.out.println("Folders:");
	      for (Folder f : folders){
	    	  System.out.print(" | " + f.getFullName());
	      }
	      System.out.println(" |\n");
	      
	      //create the folder object and open it
	      System.out.println("Opening INBOX:");
	      Folder emailFolder = store.getFolder("INBOX");
	      emailFolder.open(Folder.READ_ONLY);
	      System.out.println(emailFolder.getUnreadMessageCount() + " unread message(s)");

	      // retrieve the messages from inbox folder in an array and print it
	      Message[] messages = emailFolder.getMessages();
	      System.out.println(messages.length + " messages total.");

	      if (messages.length == 0){
	 
	    	  System.out.println("No messages, check that email server settings have IMAP settings enabled");
	      }
	      
	      for (int i = 0, n = messages.length; i < n; i++) {
	         Message message = messages[i];
	         System.out.println("---------------------------------");
	         System.out.println("Email Number " + (i + 1));
	         System.out.println("Subject: " + message.getSubject());
	         System.out.println("From: " + message.getFrom()[0]);
	         System.out.println("Text: " + message.getContent().toString());
	         System.out.println("HTML: ");
	         Object content = message.getContent(); // throws I/O excep
	         if (content instanceof Multipart) {
	             Multipart mp = (Multipart) content;
	             for (int j = 0; j < mp.getCount(); j++) {
	                 BodyPart bp = mp.getBodyPart(j);
	                 if (Pattern
	                         .compile(Pattern.quote("text/html"),
	                                 Pattern.CASE_INSENSITIVE)
	                         .matcher(bp.getContentType()).find()) {
	                     // found html part
	                     System.out.println((String) bp.getContent());
	                 } else {
	                     // some other bodypart...
	                 }
	             }
	         }

	      }

	      //close the store and folder objects
	      emailFolder.close(false);
	      store.close();
	}
	
	
	public Store getStore(){
		return store;
	}
}
