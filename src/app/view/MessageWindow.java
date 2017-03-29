package app.view;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import app.MainApp;
import app.Parser;
import app.model.MailboxModel;
import app.model.SMTPModel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MessageWindow extends Stage implements EventHandler<ActionEvent> {
	private SMTPModel model;
	private MailboxModel mailbox;
	private final WebView browser; // displays html
    private final WebEngine webEngine; // loads content
	private BorderPane root;
	private Message message;
	final Label lblSender;
	final Label lblSubjectText;
	private double SPACING = 10, PADDING = 10;
	
	public MessageWindow(Message message, SMTPModel model, MailboxModel m_model) throws Exception{
		// this fails when opening trashed messages, dunno why
		setTitle(message.getSubject() + " from " + message.getFrom()[0].toString());
		this.getIcons().addAll(MainApp.ICONS);
		this.message = message;
		this.model = model;
		this.mailbox = m_model;
		
		root = new BorderPane();
		browser = new WebView();
		webEngine = browser.getEngine();
		
		// parse html from message and load it into engine
		String content = Parser.getContent(message);
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
		lblSender = new Label(message.getFrom()[0].toString());
		senderBox.getChildren().addAll(lblFrom, lblSender);
		HBox.setHgrow(lblSender, Priority.ALWAYS);
		senderBox.getStyleClass().add("messageLine");
		dialogBox.getChildren().add(senderBox);
		
		// subject
		final HBox subjectBox = new HBox();
		final Label lblSubject = new Label("Subject:"); 
		lblSubject.setPrefWidth(100);
		lblSubjectText = new Label(message.getSubject());
		subjectBox.getChildren().addAll(lblSubject, lblSubjectText);
		HBox.setHgrow(lblSubject, Priority.ALWAYS);
		subjectBox.getStyleClass().add("messageLine");
		dialogBox.getChildren().add(subjectBox);
		root.setTop(dialogBox);
		
		
		// put the browser in an hbox
		HBox browserBox = new HBox();
		browserBox.getChildren().add(browser);
		HBox.setHgrow(browser, Priority.ALWAYS); // resize width fo editor
		root.setCenter(browserBox);
		
		// reply and reply all buttons
		HBox buttonBox = new HBox();
		final Button replyButton = new Button("Reply");
		replyButton.getStyleClass().add("buttonClass");
		replyButton.setOnAction(this);
		final Button replyAllButton = new Button("Reply All");
		replyAllButton.getStyleClass().add("buttonClass");
		replyAllButton.setOnAction(this);
		buttonBox.getChildren().addAll(replyButton, replyAllButton);
		buttonBox.setPadding(new Insets(PADDING));
		buttonBox.setSpacing(SPACING);
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		root.setBottom(buttonBox);
		
		// import css
		root.getStylesheets().add("app/view/common.css");
		root.getStylesheets().add("app/view/MailboxWindowStyles.css"); // stealing a couple styles/ should probably make a seperate sheet later
		// set stage and show
		this.setScene(new Scene(root, 800, 500));
		this.show();
	}

	public void handle(ActionEvent e) {
		
		//save to, subject and originalText values to fill the new message with
		String subject = "Re: " + lblSubjectText.getText();
		try {
			Address[] tos = message.getFrom();
			String replyTo = (tos == null) ? null : ((InternetAddress) tos[0]).getAddress();
			String originalText = "<br><hr>" + (String) webEngine.executeScript("document.documentElement.outerHTML");
			String to;
			Object o = e.getSource();
			if (o instanceof Button){
				Button b = (Button) o;
				if (b.getText().equals("Reply")){
					String toAll = lblSender.getText();
					//reply to the first address instead of all
					try {
						to = message.getFrom()[0].toString();
					} catch (MessagingException e1) {
						e1.printStackTrace();
					}
				}
		
				new ComposeMailWindow(model, mailbox, replyTo, subject, originalText);
				this.close();
			}
		} catch (MessagingException e2) {
			System.out.println("Problem replying");
			e2.printStackTrace();
		}			
	}

}
