package app.model;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
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
	
//	public String getStringFromMessage(Message message){
//		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//	}
	
	public static void writePart(Part p) throws Exception {
		if (p instanceof Message)	
		System.out.println("----------------------------");
		System.out.println("CONTENT-TYPE: " + p.getContentType());
	
		//check if the content is plain text
		if (p.isMimeType("text/plain")) {
			System.out.println("This is plain text");
			System.out.println("---------------------------");
			System.out.println((String) p.getContent());
		} 
		//check if the content has attachment
		else if (p.isMimeType("multipart/*")) {
			System.out.println("This is a Multipart");
			System.out.println("---------------------------");
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++)
				writePart(mp.getBodyPart(i));
		} 
		//check if the content is a nested message
		else if (p.isMimeType("message/rfc822")) {
			System.out.println("This is a Nested Message");
			System.out.println("---------------------------");
			writePart((Part) p.getContent());
		} 
		//check if the content is an inline image
		else if (p.isMimeType("image/jpeg")) {
			System.out.println("--------> image/jpeg");
			Object o = p.getContent();
	
			InputStream x = (InputStream) o;
			// Construct the required byte array
			System.out.println("x.length = " + x.available());
			int i = 0;
			byte[] bArray = new byte[x.available()];
			while ((i = (int) ((InputStream) x).available()) > 0) {
				int result = (int) (((InputStream) x).read(bArray));
				if (result == -1)
					break;
			}
			FileOutputStream f2 = new FileOutputStream("/tmp/image.jpg");
			f2.write(bArray);
		} 
		else if (p.getContentType().contains("image/")) {
			System.out.println("content type" + p.getContentType());
			File f = new File("image" + new Date().getTime() + ".jpg");
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(f)));
	        com.sun.mail.util.BASE64DecoderStream test = 
	             (com.sun.mail.util.BASE64DecoderStream) p.getContent();
	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = test.read(buffer)) != -1) {
	        	output.write(buffer, 0, bytesRead);
	        }
		} 
		else {
			Object o = p.getContent();
			if (o instanceof String) {
				System.out.println("This is a string");
				System.out.println("---------------------------");
				System.out.println((String) o);
			} 
			else if (o instanceof InputStream) {
				System.out.println("This is just an input stream");
				System.out.println("---------------------------");
				InputStream is = (InputStream) o;
				is = (InputStream) o;
				int c;
				while ((c = is.read()) != -1)
					System.out.write(c);
			} 
			else {
				System.out.println("This is an unknown type");
				System.out.println("---------------------------");
	            System.out.println(o.toString());
	 		}
	  	}
	}
	
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