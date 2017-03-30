import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

// Test program logs in, reads mail
public class JavaMailTest {

   public static void check(String host, String storeType, String user,
      String password) 
   {
      try {

      //create properties field
      Properties properties = new Properties();

      properties.put("mail.pop3s.host", host);
      properties.put("mail.pop3s.port", "995");
      properties.put("mail.pop3s.starttls.enable", "true");
      Session emailSession = Session.getDefaultInstance(properties);
  
      //create the IMAP store object and connect with the imap server
      Store store = emailSession.getStore(storeType);
      store.connect(host, user, password);
    
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
         Object content = message.getContent();
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

      } catch (NoSuchProviderException e) {
    	  e.printStackTrace();
      } catch (AuthenticationFailedException e) {
    	  if (e.getMessage().contains("Web login"))
    		  System.out.println("Check mail server settings -> Allow less secure apps");
    	  else if (e.getMessage().contains("password"))
    		  System.out.println("Wrong username or password");
    	  e.printStackTrace();
      } catch (MessagingException e) {
    	  e.printStackTrace();
      } catch (Exception e) {
    	  e.printStackTrace();
      }
   }

   public static void main(String[] args) {
	  String host, mailStoreType, username, password;
	  
	  // POP3 doesn't play well, IMAP works better
	  
	  // MICROSOFT LIVE ACCOUNTs
//      host = "imap-mail.outlook.com";
//      mailStoreType = "imaps";
//      username = "****"; // Change accordingly
//      password = "****"; // Change accordingly
      
	  // GMAIL ACCOUNTS
      host = "imap.gmail.com";
      mailStoreType = "imaps";
      username = "****"; // Change accordingly
      password = "****"; // Change accordingly
	  
	  // FOR OTHERS, Just need to find host address
      
      check(host, mailStoreType, username, password);    	  

      
   }

}
