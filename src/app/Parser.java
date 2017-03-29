package app;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Parser {
	private static final boolean DEBUG = false;
	private static InputStream x;
	private static InputStream is;
	private static FileOutputStream f2;
	private static DataOutputStream output;
	
	public static String getContent(Message message) throws Exception{
		try {
			return Parser.getHTMLFromMessage(message);
		} catch (Exception e){
			String s = "";
			return Parser.parseContent(message, s);			
		}
	}
	
	// TODO this function needs work, maybe check for html first and if exists ignore the rest?
	public static String parseContent(Part p, String content) throws Exception {
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
			x = (InputStream) o;
			// Construct the required byte array
			if (DEBUG) System.out.println("x.length = " + x.available());
			byte[] bArray = new byte[x.available()];
			while (((int) ((InputStream) x).available()) > 0) {
				int result = (int) (((InputStream) x).read(bArray));
				if (result == -1)
					break;
			}
			f2 = new FileOutputStream("src/tmp/image.jpg");
			f2.write(bArray);
		} else if (p.getContentType().contains("image/")) {
			if (DEBUG) System.out.println("content type" + p.getContentType());
			File f = new File("src/tmp/image" + new Date().getTime() + ".jpg");
			output = new DataOutputStream(
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
				is = (InputStream) o;
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
	
	public static String getStringFromAddresses(Address[] addresses){
		String emails = "";
		if (addresses != null){
			for (int i = 0; i < addresses.length; i++){
				if (i != 0)
					emails += ", ";
				emails += addresses[i].toString();		
			}			
		}
		return emails;
	}
	
	public static Message createMessage(Session session, String from, String toList, String ccList, String bccList, String subject, String htmlContent) throws MessagingException {
		// split by comma
		InternetAddress[] toListIA = InternetAddress.parse(toList);
        InternetAddress[] ccListIA = InternetAddress.parse(ccList);
        InternetAddress[] bccListIA = InternetAddress.parse(bccList);
        
        // create an html message
		Message message = new MimeMessage(session);
		message.setContent(htmlContent, "text/html; charset=utf-8");
	    message.setFrom(new InternetAddress(from)); // from self
		    
        message.setRecipients(Message.RecipientType.TO, toListIA);
        message.setRecipients(Message.RecipientType.CC, ccListIA);
        message.setRecipients(Message.RecipientType.BCC, bccListIA);
	    message.setSubject(subject); // subject 
	    
	    return message;
	}
}
