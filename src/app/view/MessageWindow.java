package app.view;

import javax.mail.Message;

import app.model.MailboxModel;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MessageWindow extends Stage {
	private final WebView browser; // displays html
    private final WebEngine webEngine; // loads content
	private BorderPane root;
	private Message message;
	private double SPACING = 10, PADDING = 10;
	
	public MessageWindow(Message message) throws Exception{
		setTitle(message.getSubject() + " from " + message.getFrom()[0].toString());
		this.message = message;
		MailboxModel.writePart(message);
		
		root = new BorderPane();
		browser = new WebView();
		webEngine = browser.getEngine();
		
		// parse html from message and load it into engine
		String content = MailboxModel.getHTMLFromMessage(message);
		webEngine.loadContent(content);
		
		// putting fields in vbox
		VBox dialogBox = new VBox();
		dialogBox.setId("mainWindow");
		dialogBox.setSpacing(PADDING);
		dialogBox.setPadding(new Insets(PADDING));
		
		// putting labels in hbox so can grow them and make them look like messages in mailbox
		// sender -> only looks at sender, not everyone attached. might add later
		final HBox senderBox = new HBox();
		final Label lblFrom = new Label("From:");
		lblFrom.setPrefWidth(100);
		final Label lblSender = new Label(message.getFrom()[0].toString());
		senderBox.getChildren().addAll(lblFrom, lblSender);
		senderBox.setHgrow(lblSender, Priority.ALWAYS);
		senderBox.getStyleClass().add("messageLine");
		dialogBox.getChildren().add(senderBox);
		
		// subject
		final HBox subjectBox = new HBox();
		final Label lblSubject = new Label("Subject:"); 
		lblSubject.setPrefWidth(100);
		final Label lblSubjectText = new Label(message.getSubject());
		subjectBox.getChildren().addAll(lblSubject, lblSubjectText);
		subjectBox.setHgrow(lblSubject, Priority.ALWAYS);
		subjectBox.getStyleClass().add("messageLine");
		dialogBox.getChildren().add(subjectBox);
		root.setTop(dialogBox);
		
		
		// put the browser in an hbox
		HBox browserBox = new HBox();
		browserBox.getChildren().add(browser);
		browserBox.setHgrow(browser, Priority.ALWAYS); // resize width fo editor
		root.setCenter(browserBox);
		
		// reply button
		HBox buttonBox = new HBox();
		final Button replyButton = new Button("Reply");
		replyButton.getStyleClass().add("buttonClass");
		buttonBox.getChildren().addAll(replyButton);
		buttonBox.setPadding(new Insets(PADDING));
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		root.setBottom(buttonBox);
		
		// import css
		root.getStylesheets().add("app/view/common.css");
		root.getStylesheets().add("app/view/MailboxWindowStyles.css"); // stealing a couple styles/ should probably make a seperate sheet later
		// set stage and show
		this.setScene(new Scene(root, 800, 500));
		this.show();
	}

}
