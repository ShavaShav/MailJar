package app.view;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

public class ComposeMailWindow extends Stage{
	
	private AnchorPane root;
	private static HTMLEditor editor;
	private static final double TOP_OF_EDITOR = 100.0; // pixels from top of window 
	private static final double BOTTOM_OF_EDITOR = 50.0; // pixels from bottom of window
	
	public ComposeMailWindow(){
		setTitle("Compose New Message");
		root = new AnchorPane();
		
		editor = new HTMLEditor();
		HBox editorBox = new HBox();
		editorBox.getChildren().add(editor);
		
		// anchor the editor to preset top/bottom margins, 100% width
		AnchorPane.setTopAnchor(editorBox, TOP_OF_EDITOR);
		AnchorPane.setBottomAnchor(editorBox, BOTTOM_OF_EDITOR);
		AnchorPane.setRightAnchor(editorBox, 0.0); // pin to sides
		AnchorPane.setLeftAnchor(editorBox, 0.0);
		
		root.getChildren().add(editorBox);
		// set stage and show
		this.setScene(new Scene(root, 800, 500));
		this.show();
	}

}
