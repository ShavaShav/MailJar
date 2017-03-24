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
	private static final boolean DEBUG = true;
	
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
		emailFolder.open(Folder.READ_WRITE);
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
	public void sendMessage(String toList, String ccList, String bccList, String subject, Multipart content) throws AddressException, MessagingException{
        InternetAddress[] toListIA = InternetAddress.parse(toList);
        InternetAddress[] ccListIA = InternetAddress.parse(ccList);
        InternetAddress[] bccListIA = InternetAddress.parse(bccList);

		Message message = new MimeMessage(emailSession); // MIME type (HTML ok)
	    
        message.setFrom(new InternetAddress(emailAddress)); // from self
        message.setRecipients(Message.RecipientType.TO, toListIA);
        message.setRecipients(Message.RecipientType.CC, ccListIA);
        message.setRecipients(Message.RecipientType.BCC, bccListIA);

	    message.setSubject("Testing Subject"); // subject 
	    message.setContent(content); // Multipart is subclass of Message, Composed messages will have to be of this type
	    Transport.send(message); // Send message
	}
	
	public Store getStore(){ return store; }
	public Session getSession(){ return emailSession; }
	
	public static String getContent(Message message) throws Exception{
		try {
			return getHTMLFromMessage(message);
		} catch (Exception e){
			String s = "";
			return parseContent(message, s);			
		}
	}
	
	// TODO this function needs work, maybe check for html first and if exists ignore the rest?
	private static String parseContent(Part p, String content) throws Exception {
		//check if the content is plain text
		if (p.isMimeType("text/plain")) {
			if (DEBUG){
				System.out.println("This is plain text");
				System.out.println("---------------------------");				
			}
			content += (String) p.getContent();
		} else if (p.isMimeType("multipart/*")) {
			// the content has attachment
			if (DEBUG){
				System.out.println("This is a Multipart");
				System.out.println("---------------------------");
			}
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++)
				content += parseContent(mp.getBodyPart(i), content);
		} else if (p.isMimeType("message/rfc822")) {
			// content is a nested message
			if (DEBUG){
				System.out.println("This is a Nested Message");
				System.out.println("---------------------------");				
			}
			content += parseContent((Part) p.getContent(), content);
		} else if (p.isMimeType("image/jpeg")) {
			// content is an inline image
			if (DEBUG)
				System.out.println("--------> image/jpeg");
			
			Object o = p.getContent();
			InputStream x = (InputStream) o;
			// Construct the required byte array
			if (DEBUG) System.out.println("x.length = " + x.available());
			int i = 0;
			byte[] bArray = new byte[x.available()];
			while ((i = (int) ((InputStream) x).available()) > 0) {
				int result = (int) (((InputStream) x).read(bArray));
				if (result == -1)
					break;
			}
			FileOutputStream f2 = new FileOutputStream("src/tmp/image.jpg");
			f2.write(bArray);
		} else if (p.getContentType().contains("image/")) {
			if (DEBUG) System.out.println("content type" + p.getContentType());
			File f = new File("src/tmp/image" + new Date().getTime() + ".jpg");
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(f)));
	        com.sun.mail.util.BASE64DecoderStream test = 
	             (com.sun.mail.util.BASE64DecoderStream) p.getContent();
	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = test.read(buffer)) != -1) {
	        	output.write(buffer, 0, bytesRead);
	        }
		} else {
			Object o = p.getContent();
			if (o instanceof String) {
				if (DEBUG){
					System.out.println("This is a string");
					System.out.println("---------------------------");
				}
				content += (String) o;
			} else if (o instanceof InputStream) {
				if (DEBUG){
					System.out.println("This is just an input stream");
					System.out.println("---------------------------");
				}
				InputStream is = (InputStream) o;
				StringBuilder sb = new StringBuilder();
				is = (InputStream) o;
				int c;
				while ((c = is.read()) != -1)
					sb.append(c);
				content += sb.toString();
			} else {
				if (DEBUG){
					System.out.println("This is an unknown type");
					System.out.println("---------------------------");
				}
				content += o.toString();					
	 		}
	  	}
		return content;
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
		String content = "";
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
		throw new Exception("not html");
	}
//	
//	public static void main(String args[]){
//		File f = new File("src/tmp/image.jpg");
//		System.out.println(f.getAbsolutePath());
//	}
}