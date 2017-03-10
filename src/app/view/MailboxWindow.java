package app.view;

import app.model.MailboxModel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MailboxWindow extends Stage{
	
	AnchorPane root;
	MailboxModel mailbox; // model that supplies methods to get info for window
	
	public MailboxWindow(MailboxModel mailbox){
		this.mailbox = mailbox; // store model
		root = new AnchorPane();
		
		
		
		// set stage and show
		this.setScene(new Scene(root, 1200, 800));
		this.show();
	}

}
