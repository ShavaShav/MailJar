package app.model;

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
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailboxModel {
	private Session emailSession;
	private Store store;
	private String emailAddress;
	private HashMap<String, String> hostMap;
	private Folder emailFolder;
	public Message [] messages;
	
	// to construct the model, must successfully connect 
	public MailboxModel(String email, String password) throws Exception {
		emailAddress = email;
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
		emailSession = Session.getDefaultInstance(properties);
  
		//create the IMAP store object and connect with the imap server
		store = emailSession.getStore("imaps");
		store.connect(host, email, password);  
	}	
	
	// returns the folders for current store
	public Folder[] getFolders() throws MessagingException{
		return store.getDefaultFolder().list("*");	
	}
	
	// sets the model to use a particular folder
	public void openFolder(Folder folder) throws MessagingException{
		emailFolder = store.getFolder(folder.getFullName());
		emailFolder.open(Folder.READ_ONLY);
		messages = emailFolder.getMessages();
		// TODO notify view of change -> they must get messages() and update
	}
	
	// sets the model to use the default folder
	public void openInbox() throws Exception{
		// open the first folder by default
		Folder inbox = getFolders()[0];
		if (!inbox.exists()) {
            throw new Exception("Inbox not found");
         }
		openFolder(inbox);
	}
	
	// return messages in current folder
	public Message[] getMessages() {
		return messages;
	}
	
	// Refreshs the current store and return the number of new emails
	public int refresh() throws Exception{
		int numEmails = messages.length;
		store.connect(); // reconnect to server
		openFolder(emailFolder); // open folder
		messages = getMessages();
		return numEmails - messages.length;
	}

	public void close() throws MessagingException{
		emailFolder.close(false);
		store.close();
	}

	// Method to send an email using the current session
	public void sendMessage(String recipientEmail, String subject, Multipart content) throws AddressException, MessagingException{
        Message message = new MimeMessage(emailSession); // MIME type (HTML ok)
	    message.setFrom(new InternetAddress(emailAddress)); // from self
	    message.setRecipients(Message.RecipientType.TO, // recipient
	          InternetAddress.parse(recipientEmail));
	    message.setSubject("Testing Subject"); // subject 
	    message.setContent(content); // Multipart is subclass of Message, Composed messages will have to be of this type
	    Transport.send(message); // Send message
	}
	
	public Store getStore(){ return store; }
	public Session getSession(){ return emailSession; }
	
	public static Multipart getMultipartFromMessage(Message message) throws Exception{
		Object content = message.getContent(); // throws I/O excep
		if (content instanceof Multipart)
			return (Multipart) content;
		else
			throw new Exception ("Not a multipart message");
	}
	
	public static String getHTMLFromMessage(Message message) throws Exception{
		Multipart mp = getMultipartFromMessage(message);
		for (int j = 0; j < mp.getCount(); j++) {
			BodyPart bp = mp.getBodyPart(j);
			if (Pattern
					.compile(Pattern.quote("text/html"),
							Pattern.CASE_INSENSITIVE)
					.matcher(bp.getContentType()).find()) {
				// found html part
				return (String) bp.getContent();
			}
		}
		throw new Exception ("Not an html message");
	}
}