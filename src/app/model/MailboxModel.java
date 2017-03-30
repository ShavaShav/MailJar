package app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import java.util.List;

public class MailboxModel {
	private Session emailSession;
	private Store store;
	private String emailAddress;
	private HashMap<String, String> hostMap;
	private Folder emailFolder;
	public Message [] messages;
	private int currentStart = 0;
	private static final int MAX_REQUESTS = 10; // max number of emails to be shown at a time
	
	// to construct the model, must successfully connect 
	public MailboxModel(String email, String password) throws Exception {
		emailAddress = email;
		hostMap = new HashMap<String, String>();
		hostMap.put("gmail", "imap.gmail.com");
		hostMap.put("uwindsor", "imap.gmail.com");
		hostMap.put("hotmail", "imap-mail.outlook.com");
		hostMap.put("live", "imap-mail.outlook.com");
		hostMap.put("outlook", "imap-mail.outlook.com");
		
		//create properties fieldHashMap
		Properties properties = new Properties();
		
		String hostKey = email.split("@")[1].split("\\.")[0];
		String host = hostMap.get(hostKey);
		
		properties.put("mail.pop3s.host", host);
		properties.put("mail.pop3s.port", "995");
		properties.put("mail.pop3s.starttls.enable", "true");
		emailSession = Session.getDefaultInstance(properties);
		
		// Setup mail server
		properties.setProperty("mail.smtp.host", host);
  
		//create the IMAP store object and connect with the imap server
		store = emailSession.getStore("imaps");
		store.connect(host, email, password);  
	}
	
	public Folder getFolder(String name) throws MessagingException{
		for (Folder f : getFolders())
			if (f.getName().equals(name)){
				return f;
			}
		
		return null;
	}
	
	// returns the folders for current store
	public Folder[] getFolders() throws MessagingException{
		return store.getDefaultFolder().list("*");	
	}
	
	// returns subfolders, if they exist
	public Folder[] getSubFolders(Folder folder) throws MessagingException{
		if (folder.getType() == Folder.HOLDS_FOLDERS)
			return folder.list("*");
		else
			return null;
	}
	
	// sets the model to use a particular folder
	public void openFolder(Folder folder) throws MessagingException{
		currentStart =0 ; // start at the start!
		emailFolder = store.getFolder(folder.getFullName());
		// this is why |GMAIL| wasn't opening, it's a folder of folders!
		// we can probably delete this because we're going to ignore recursive folders i think
		if (emailFolder.getType() != Folder.HOLDS_FOLDERS)
			emailFolder.open(Folder.READ_WRITE);	
		
		messages = emailFolder.getMessages();
		
		// reverse the messages so newest are first
		List<Message> reversed = Arrays.asList(messages);
		Collections.reverse(reversed);
		reversed.toArray(messages);
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
	
//	// return messages in current folder
//	public Message[] getMessages() throws MessagingException {
//		return messages;
//	}
	
	// Refreshs the current store and return the number of new emails
	public int refresh() throws Exception{
		System.out.println(emailFolder.getName());
		int numEmails = messages.length;
		openFolder(emailFolder); // re-open folder
		currentStart = 0;
		return numEmails - messages.length;
	}

	// close the current store
	public void closeModel() throws MessagingException{
		emailFolder.close(false);
		store.close();
	}
	
	public Store getStore(){ return store; }
	public Session getSession(){ return emailSession; }
	public String getEmailAdress(){ return emailAddress; }
	public Folder getCurrentFolder() { return emailFolder; }
	
	// These methods returns counts for ALL folders (-duplicates)
	public int getMessageCount() throws MessagingException{
		return store.getDefaultFolder().getMessageCount();
	}
	
	public int getNewMessageCount() throws MessagingException{
		return store.getDefaultFolder().getNewMessageCount();
	}
	
	public int getUnreadMessageCount() throws MessagingException{
		return store.getDefaultFolder().getUnreadMessageCount();
	}
	
	public int getDeletedMessageCount() throws MessagingException{
		return store.getDefaultFolder().getDeletedMessageCount();
	}
	
	public String getEmail(){
		return emailAddress;
	}
	
	// gets the next ten emails
	public ArrayList<Message> getNextTenMessages(){
		ArrayList<Message> toReturn = new ArrayList<Message>();
		int currentRequest = 0;
		for (; currentRequest < MAX_REQUESTS && currentStart < messages.length; currentStart++, currentRequest++){
			toReturn.add(messages[currentStart]);
		}
		return toReturn;
	}
	
	public ArrayList<Message> getPrevTenMessages(){
		currentStart -= (MAX_REQUESTS * 2);
		if (currentStart < 0) currentStart = 0;
		return getNextTenMessages();
	}
}
