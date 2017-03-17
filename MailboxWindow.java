package app.view;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;

import app.model.MailboxModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MailboxWindow extends Stage {

	private int maxMessages = 50; 
	private AnchorPane root;
	private MailboxModel mailbox; // model that supplies methods to get info for window
	private HBox buttonWindow;
	private Button composeBtn;
	private VBox mainWindow;
	private ScrollPane vScroll;
	private int numFolders;
	private Folder[] folders;
	private TabPane tabs;
	private HBox messageLine;

	private double SPACING = 10, PADDING = 10;
	private double TOP_HEIGHT = 180;

	public MailboxWindow(MailboxModel mailbox) throws MessagingException, IOException{
		setTitle("Inbox");
		// set up mailbox model
		this.mailbox = mailbox;
		try {
			mailbox.openInbox();
		} catch (Exception e) {
			// TODO Display a message if unable to open inbox
			e.printStackTrace();
		}

		folders = mailbox.getFolders();
		numFolders = folders.length;
		System.out.println(numFolders);

		//initialize variables
		root = new AnchorPane();
		buttonWindow = new HBox();
		buttonWindow.setAlignment(Pos.BOTTOM_LEFT);
		composeBtn = new Button ("Compose New Message");
		composeBtn.setPrefSize(180.0, 40.0);
		composeBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				new ComposeMailWindow();
			}
		});
		mainWindow = new VBox();
		mainWindow.setId("mainWindow");
		vScroll = new ScrollPane();
		vScroll.setId("vScroll");
		vScroll.setContent(mainWindow);
		vScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		tabs = new TabPane();
		tabs.setPrefWidth(1180);
		tabs.setPrefHeight(580);
		tabs.setPadding(new Insets(PADDING));

		//create menu
		final Menu fileMenu = new Menu("File");
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(fileMenu);
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				try {
					mailbox.close();
				} catch (MessagingException e) {
					System.out.println("Unable to close store");
				} finally {
					Platform.exit();
					System.exit(0);	// exit regardless				
				}
			}
		});
		fileMenu.getItems().add(exit);

		//sizing
		buttonWindow.setPrefHeight(160);
		buttonWindow.setPrefWidth(1180);
		buttonWindow.setPadding(new Insets(PADDING));
		mainWindow.setPrefHeight(580);
		mainWindow.setPrefWidth(1140);
		vScroll.setPrefWidth(1180);
		vScroll.setPrefHeight(580);
		vScroll.setPadding(new Insets(PADDING));

		//anchoring
		AnchorPane.setLeftAnchor(buttonWindow, 10.0);
		AnchorPane.setTopAnchor(buttonWindow, 30.0);
		AnchorPane.setRightAnchor(buttonWindow, 10.0);

		AnchorPane.setTopAnchor(vScroll, TOP_HEIGHT + 10.0);
		AnchorPane.setBottomAnchor(vScroll, 10.0);
		AnchorPane.setRightAnchor(vScroll, 10.0);
		AnchorPane.setLeftAnchor(vScroll, 10.0);

		// get messages in the inbox
		displayMessages(mailbox.getMessages());
		
		//keep for now, I'm able to create tabs with each folder name 
		//but i'm not able to set the tabs content with the mainWindow 

//		for (int i=0; i<numFolders; i++)
//		{
//			Tab tab = new Tab();
//			tab.setText(folders[i].getName());
//			tab.setContent(mainWindow);
//			tabs.getTabs().add(tab);
//		}

		//add nodes to scenes
		buttonWindow.getChildren().add(composeBtn);
		root.getChildren().addAll(menuBar, buttonWindow, vScroll);
		root.getStylesheets().add("app/view/MailboxWindowStyles.css");

		// if this window is closed, then whole application will close
		this.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				try {
					mailbox.close();
				} catch (MessagingException e) {
					System.out.println("Unable to close store");
				} finally {
					Platform.exit();
					System.exit(0);	// exit regardless				
				}
			}
		});  

		// set stage and show
		this.setScene(new Scene(root, 1200, 800));
		this.show();
	}

	public void displayMessages(Message[] messages) throws MessagingException{
		// display messages
		for (int i = 0; i < messages.length && i < maxMessages; i++) {
			Message message = messages[i];

			//obtain message senders email
			Address[] sender = message.getFrom();
			String email = sender == null ? null : ((InternetAddress) sender[0]).getAddress();
			Text emailText = new Text();
			emailText.setText(email);

			//obtain email subject
			String subject = message.getSubject();
			String stringSubject = subject.toString();
			Text textSubject = new Text();
			textSubject.setText(stringSubject);

			//obtain email sent date
			Date date = message.getSentDate();
			SimpleDateFormat format = new SimpleDateFormat("MMM d");
			String formatDate = format.format(date);
			Text textDate = new Text();
			textDate.setText(formatDate);

			//create rows to display messages
			messageLine = new HBox();
			messageLine.setPadding(new Insets(5));
			messageLine.setPrefWidth(1180);
			messageLine.getStyleClass().add("messageLine"); // css #messageLine class
			messageLine.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
						if(mouseEvent.getClickCount() == 2){
							try {
								new MessageWindow(message);
								messageLine.setId("seenMessageLine");
							} catch (Exception e) {
								// TODO Print a message if unable to open!
								e.printStackTrace();
							}
						}
					}
				}
			});

			//determine if message has been read or not
			if (message.isSet(Flags.Flag.SEEN))
			{
				messageLine.setId("seenMessageLine");
			}

			else
			{
				messageLine.setId("unseenMessageLine");
			}
			
			//create GridPane to display message sender, subject and date
			GridPane mgp = new GridPane();
			mgp.getColumnConstraints().add(new ColumnConstraints(400));
			mgp.getColumnConstraints().add(new ColumnConstraints(550));
			mgp.getColumnConstraints().add(new ColumnConstraints(150));
			mgp.setPrefWidth(1140);
			mgp.setHgap(50.00);
			mgp.add(emailText, 0, 0);
			mgp.add(textSubject, 1, 0);
			mgp.add(textDate, 2, 0);

			messageLine.getChildren().add(mgp);
			mainWindow.getChildren().add(messageLine);

		}
	}

}
