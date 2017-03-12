package app.view;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ComposeMailWindow extends Stage{
	
	AnchorPane root;
	
	public ComposeMailWindow(){
		
		root = new AnchorPane();
		
		
		
		// set stage and show
		this.setScene(new Scene(root, 800, 500));
		this.show();
	}

}
