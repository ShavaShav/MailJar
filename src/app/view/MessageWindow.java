package app.view;

import javax.mail.Message;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MessageWindow extends Stage {
	
	AnchorPane root;
	Message message;
	
	public MessageWindow(Message message){
		this.message = message;
		root = new AnchorPane();
		
		
		
		// set stage and show
		this.setScene(new Scene(root, 800, 500));
		this.show();
	}

}
