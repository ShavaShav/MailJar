package app.view;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;

import app.MainApp;
import app.Parser;
import app.model.MailboxModel;
import app.model.SMTPModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
public class ComposeMailWindow extends Stage implements EventHandler<ActionEvent>{
	private SMTPModel model;
	private BorderPane root;
	private static HTMLEditor editor;
	private double SPACING = 10, PADDING = 10;
	private ChoiceBox boxReceiver;
	private TextField tfReceiver;
	private TextField tfSubject;
	
	// these are only set if the draft constructor is called
	private boolean isOriginallyDraft = false;
	private Message originalDraft;
	private MailboxModel mailbox;
	
	private String to = "", cc = "", bcc = ""; // save these when they're entered
	
	//constructor called from MessageWindow reply button
	public ComposeMailWindow(SMTPModel s_model, MailboxModel m_model, String replyTo, String subject, String preparedHTML){
		this(s_model, m_model);
		tfReceiver.setText(replyTo);
		tfSubject.setText(subject);
		editor.setHtmlText(preparedHTML);
	}
	
	// Opening an existing draft --> will have to set mailbox window to use this constructor if in drafts folder.
	public ComposeMailWindow(SMTPModel s_model, MailboxModel m_model, Message draft) throws MessagingException, Exception{
		this(s_model, m_model);
		if (!draft.isSet(Flag.DRAFT))
			throw new Exception("Message is not a draft");
		
		to = Parser.getStringFromAddresses(draft.getRecipients(RecipientType.TO));
		cc = Parser.getStringFromAddresses(draft.getRecipients(RecipientType.CC));
		bcc = Parser.getStringFromAddresses(draft.getRecipients(RecipientType.BCC));

		tfReceiver.setText(to);
		
		editor.setHtmlText(Parser.getContent(draft));	
		originalDraft = draft;
		isOriginallyDraft = true;
	}
	
	public ComposeMailWindow(SMTPModel s_model, MailboxModel m_model){
		this.model = s_model;
		mailbox = m_model;
		setTitle("Compose New Message");
		this.getIcons().addAll(MainApp.ICONS);
		root = new BorderPane();
		
		// putting fields in grid pane
		final GridPane dialogPane = new GridPane();
		dialogPane.setVgap(PADDING);
		dialogPane.setHgap(SPACING);
		
		// receiver
		boxReceiver =
				new ChoiceBox(FXCollections.observableArrayList(
						"To:", "Cc:", "Bcc:")
		);
		boxReceiver.getSelectionModel().selectFirst();
		boxReceiver.setPrefWidth(100);
		dialogPane.setConstraints(boxReceiver, 0, 0);
		
		tfReceiver = new TextField();
		dialogPane.setConstraints(tfReceiver, 1, 0);
		dialogPane.setHgrow(tfReceiver, Priority.ALWAYS);
		
		boxReceiver.getSelectionModel().selectedIndexProperty().addListener(
				new ChangeListener<Number>(){
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number oldI, Number newI) {
						// store field of receivers being switched out
						switch (oldI.intValue()){
						case 0: to = tfReceiver.getText();
							break;
						case 1: cc = tfReceiver.getText();
							break;
						case 2: bcc = tfReceiver.getText();
							break;
						}
						// load stored receivers into field being switched in
						switch (newI.intValue()){
						case 0: tfReceiver.setText(to);
							break;
						case 1: tfReceiver.setText(cc);
							break;
						case 2: tfReceiver.setText(bcc);
							break;
						}	
					}
				});
		
		// subject
		final Label lblSubject = new Label("Subject:");
		dialogPane.setConstraints(lblSubject, 0, 1);
		
		tfSubject = new TextField();
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
		draftButton.setOnAction(this);
		final Button sendButton = new Button("Send");
		sendButton.getStyleClass().add("buttonClass");
		sendButton.setOnAction(this);
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
	
	@Override
	public void handle(ActionEvent e) {
		// have to store most recent textfield according to choicebox
		switch(boxReceiver.getSelectionModel().getSelectedIndex()){
		case 0: to = tfReceiver.getText();
			break;
		case 1: cc = tfReceiver.getText();
			break;
		case 2: bcc = tfReceiver.getText();
			break;
		}
		String subject = tfSubject.getText();
		String htmlContent = editor.getHtmlText();
		Object o = e.getSource();
		if (o instanceof Button){
			Button b = (Button) o;
			if (b.getText().equals("Send")){
				// send
				try {
					model.sendHTMLMessage(to, cc, bcc, subject, htmlContent);
					this.close(); // close the compose window
				} catch (AddressException e1) {
					JOptionPane.showMessageDialog(null, "Invalid recipients: " + e1.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Can't open first folder: " + e1.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			} else {
				// draft
				try {
					// create message and set flag to draft
					Message draft = Parser.createMessage(model.getSession(), model.getEmailAdress(), to, cc, bcc, subject, htmlContent);
					draft.setFlag(Flag.DRAFT, true);
					
					// open the drafts folder
					Folder draftsFolder = mailbox.getFolder("Drafts");
					if (draftsFolder == null)
						throw new Exception("No drafts folder!");
			
					// must delete old draft before adding, if this is originally a draft
					if (isOriginallyDraft){
						originalDraft.setFlag(Flag.DELETED, true);
						draftsFolder.expunge(); // clear folder of flagged file
					}
					
					// save to drafts folder
					draft.setFlag(Flag.SEEN, true); // we've seen it
					Message[] drafts = {draft};
					draftsFolder.appendMessages(drafts);
					System.out.println("Draft saved!");
					this.close();
					
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Unable to save draft: " + e1.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}			
	}

}
