package app.model;

import java.util.HashMap;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

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
		hostMap.put("uwindsor", "imap.gmail.com");
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
		
		// Setup mail server
		properties.setProperty("mail.smtp.host", host);
  
		//create the IMAP store object and connect with the imap server
		store = emailSession.getStore("imaps");
		store.connect(host, email, password);  
	}
	
	public Folder getFolder(String name) throws MessagingException{
		for (Folder f : getFolders())
			if (f.getName().equals(name)){
				System.out.println(f.getName());
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
		emailFolder = store.getFolder(folder.getFullName());
		// this is why |GMAIL| wasn't opening, it's a folder of folders!
		// we can probably delete this because we're going to ignore recursive folders i think
		if (emailFolder.getType() == Folder.HOLDS_FOLDERS){
			Folder[] subFolders = getSubFolders(emailFolder);
			for (Folder f : subFolders)
				System.out.print(f.getFullName() + " *** ");
			System.out.println("Opening "+subFolders[0].getFullName()+"..");
			openFolder(subFolders[0]); // TODO : testing right now with 1st sub folder
		} else {
			emailFolder.open(Folder.READ_WRITE);			
		}
		messages = emailFolder.getMessages();
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
	public Message[] getMessages() throws MessagingException {
		return messages;
	}
	
	// Refreshs the current store and return the number of new emails
	public int refresh() throws Exception{
		int numEmails = messages.length;
		openFolder(emailFolder); // re-open folder
		messages = getMessages();
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
	public int getMessageCount(){
		int count = 0;
		try {
			count = store.getDefaultFolder().getMessageCount();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public int getNewMessageCount(){
		int count = 0;
		try {
			count = store.getDefaultFolder().getNewMessageCount();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public int getUnreadMessageCount(){
		int count = 0;
		try {
			count = store.getDefaultFolder().getUnreadMessageCount();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public int getDeletedMessageCount(){
		int count = 0;
		try {
			count = store.getDefaultFolder().getDeletedMessageCount();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public String getEmail(){
		return emailAddress;
	}
}
