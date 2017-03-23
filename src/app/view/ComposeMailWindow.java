package app.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;


// TODO Add action handling
public class ComposeMailWindow extends Stage{
	
	private BorderPane root;
	private static HTMLEditor editor;
	private double SPACING = 10, PADDING = 10;
	private String to = "", cc = "", bcc = ""; // save these when they're entered
	
	public ComposeMailWindow(){
		setTitle("Compose New Message");
		root = new BorderPane();
		
		// putting fields in grid pane
		final GridPane dialogPane = new GridPane();
		dialogPane.setVgap(PADDING);
		dialogPane.setHgap(SPACING);
		
		// receiver
		final ChoiceBox boxReceiver =
				new ChoiceBox(FXCollections.observableArrayList(
						"To:", "Cc:", "Bcc:")
		);
		boxReceiver.getSelectionModel().selectFirst();
		boxReceiver.setPrefWidth(100);
		dialogPane.setConstraints(boxReceiver, 0, 0);
		
		final TextField tfReceiver = new TextField();
		dialogPane.setConstraints(tfReceiver, 1, 0);
		dialogPane.setHgrow(tfReceiver, Priority.ALWAYS);
		
		// subject
		final Label lblSubject = new Label("Subject:");
		dialogPane.setConstraints(lblSubject, 0, 1);
		
		final TextField tfSubject = new TextField();
		dialogPane.setConstraints(tfSubject, 1, 1);
		dialogPane.setHgrow(tfSubject, Priority.ALWAYS);
		
		// add to root
		dialogPane.getChildren().addAll(boxReceiver, tfReceiver, lblSubject, tfSubject);
		dialogPane.setPadding(new Insets(PADDING));
		root.setTop(dialogPane);

		// editor
		editor = new HTMLEditor();
		HBox editorBox = new HBox();
		editorBox.getChildren().add(editor);
		editorBox.setHgrow(editor, Priority.ALWAYS); // resize width fo editor
		root.setCenter(editorBox);
		
		// send/save buttons
		HBox buttonBox = new HBox();
		final Button draftButton = new Button("Save Draft");
		draftButton.getStyleClass().add("buttonClass");
		final Button sendButton = new Button("Send");
		sendButton.getStyleClass().add("buttonClass");
		buttonBox.getChildren().addAll(draftButton, sendButton);
		buttonBox.setPadding(new Insets(PADDING));
		buttonBox.setSpacing(SPACING);
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		root.setBottom(buttonBox);
		
		// import css
		root.getStylesheets().add("app/view/common.css");
		// set stage and show
		this.setScene(new Scene(root, 800, 500));
		this.show();
	}

}
