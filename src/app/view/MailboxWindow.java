package app.view;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import app.model.MailboxModel;
import app.model.SMTPModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MailboxWindow extends Stage {

	private int maxMessages = 50; 
	private AnchorPane root;
	private MailboxModel mailbox; // model that supplies methods to get info for window
	private Tab currentTab; // for refreshing and other utilities
	private SMTPModel smtp;
	private boolean currentlyInDrafts = false;

	private double PADDING = 10;
	private double TOP_HEIGHT = 180;
	
	public MailboxWindow(MailboxModel mailbox, SMTPModel smtp) throws MessagingException, IOException{
		setTitle("Mailbox");
		root = new AnchorPane();
	
		this.mailbox = mailbox; // set up mailbox model
		this.smtp = smtp;
		
		// generate menu buttons, logos etc by Anchoring
		generateTopElements(smtp);

		Folder[] folders = mailbox.getFolders();
		
		// this'll hold tabs, header, and messages
		HBox tabBox = new HBox();
		TabPane tabs = getTabPane(folders);
		tabBox.getChildren().add(tabs);
		HBox.setHgrow(tabs, Priority.ALWAYS);		
		
		root.getChildren().add(tabBox);
		AnchorPane.setTopAnchor(tabBox, TOP_HEIGHT + PADDING);
		AnchorPane.setBottomAnchor(tabBox, PADDING);
		AnchorPane.setLeftAnchor(tabBox, PADDING);
		AnchorPane.setRightAnchor(tabBox, PADDING);
		
		// import css
		root.getStylesheets().add("app/view/common.css");
		root.getStylesheets().add("app/view/MailboxWindowStyles.css");

		// if this window is closed, then whole application will close
		this.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				try {
					mailbox.closeModel();
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
	
	private void generateTopElements(SMTPModel smtp){
		HBox buttonWindow = new HBox();
		buttonWindow.setAlignment(Pos.BOTTOM_LEFT);
		Button composeBtn = new Button ("Compose New Message");
		composeBtn.getStyleClass().add("buttonClass");
		composeBtn.setPrefSize(220.0, 40.0);
		composeBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				new ComposeMailWindow(smtp, mailbox);
			}
		});
		Button refresh = new Button ("Refresh");
		refresh.setOnAction(evt -> {
			System.out.println("Refreshing current folder");
			try {
				int newEmails = mailbox.refresh();
				System.out.println(newEmails + " new emails.");
				setTabContentToCurrentFolder(currentTab);
			} catch (Exception e) {
				System.out.println("Failed to refresh");
				e.printStackTrace();
			}
		});
		refresh.getStyleClass().add("buttonClass");
		
		// logo
		final Image logo = new Image("img/logo.png");
		final ImageView logoView = new ImageView();
		logoView.setImage(logo);
		logoView.setFitWidth(100);
		logoView.setPreserveRatio(true);
		logoView.setSmooth(true);
		logoView.setCache(true);
		
		//create menu
		final Menu fileMenu = new Menu("File");
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(fileMenu);
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				try {
					mailbox.closeModel();
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
		buttonWindow.setPadding(new Insets(PADDING));
		buttonWindow.setSpacing(10);
		
		//anchoring
		AnchorPane.setLeftAnchor(buttonWindow, PADDING);
		AnchorPane.setTopAnchor(buttonWindow, PADDING*3);
		AnchorPane.setRightAnchor(logoView, PADDING);
		AnchorPane.setTopAnchor(logoView, PADDING);
		
		buttonWindow.getChildren().addAll(composeBtn, refresh);
		root.getChildren().addAll(logoView, buttonWindow, menuBar);
	}
	
	// holds tabs, and it's content areas hold headers and messages
	private TabPane getTabPane(Folder[] folders){
		TabPane tabs = new TabPane();;
		tabs.setPrefWidth(1200);
		tabs.setPrefHeight(580);
		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabs.getStyleClass().add("tabPane");
		// create individual tabs for each mailbox folder
		for (int i = 0; i < folders.length; i++)
		{
			// we won't give tabs access to folders of folders
			try {
				if (folders[i].getType() == Folder.HOLDS_FOLDERS)
					continue;
			} catch (MessagingException e) {
				System.out.println("Unable to ignore recursive folder");
			}
			Tab tab = new Tab();
			tab.setText(folders[i].getName());
			
			tabs.getTabs().add(tab);
			tab.setId(String.valueOf(i)); // mapping to folder index
			tab.getStyleClass().add("tab");
		}
		
		// when tab is clicked, open folder and set tab content to messages
		tabs.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
	    	try {
	    		// load tab's folder according to it's id
	    		int folderIndex = Integer.parseInt(newTab.getId());
	    		System.out.println("Opening folder " + folderIndex + ": " + folders[folderIndex].getName());	
				mailbox.openFolder(folders[folderIndex]);
				currentTab = newTab;
				// set content of tab to folder messages
				setTabContentToCurrentFolder(currentTab);
				// if draft, set boolean so we can open different compose window
				if (folders[folderIndex].getName().equals("Drafts"))
					currentlyInDrafts = true;
				else
					currentlyInDrafts = false;
			} catch (Exception e) {
				System.out.println("Can't open the folder for this tab!");
				e.printStackTrace();
			} 
		});
		
		// attempt to set open the first tab's folder
		try {
			mailbox.openFolder(folders[0]);
			currentTab = tabs.getTabs().get(0);
			setTabContentToCurrentFolder(currentTab);
			tabs.getSelectionModel().selectFirst();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			System.out.println("Cant open first folder");
			e.printStackTrace();
		}
		return tabs;
	}
	
	// the actual message pane part gets set to the content of a tab here
	private void setTabContentToCurrentFolder(Tab tab){
		try {
			VBox messagePane = getMessagePane(mailbox.getMessages());
			tab.setContent(messagePane);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			System.out.println("Can't open folder");
			e.printStackTrace();
		}
	}
	
	private GridPane getMessageHeaderPane(){
		Text from = new Text();
		from.setText("E-MAIL");
		Text subject = new Text();
		subject.setText("SUBJECT");
		Text date = new Text ();
		date.setText("DATE");
		
		GridPane headerWindow = new GridPane();
		headerWindow.setPrefHeight(40);
		
		ColumnConstraints column1 = new ColumnConstraints(300);
	    ColumnConstraints column2 = new ColumnConstraints();
	    column2.setHgrow(Priority.ALWAYS);
	    ColumnConstraints column3 = new ColumnConstraints(150);
	    headerWindow.getColumnConstraints().addAll(column1, column2, column3);
	
		headerWindow.add(from, 0, 0);
		headerWindow.add(subject, 1, 0);
		headerWindow.add(date, 2, 0);
		headerWindow.setId("headers");
		
		return headerWindow;
	}

	private VBox getMessagePane(Message[] messages) throws MessagingException{
		// holds header and scrolling pane
		VBox messagePane = new VBox();
		
		// message headers
		GridPane headerPane = getMessageHeaderPane();
		
		//create scrolling pane for messages 
		VBox mainWindow = new VBox();
		mainWindow.setId("mainWindow");
		
		ScrollPane vScroll = new ScrollPane();
		vScroll.setId("vScroll");
		vScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		//display messages
		for (int i = 0; i < messages.length && i < maxMessages; i++) {
			Message message = messages[i];

			//obtain message senders email
			Address[] sender = message.getFrom();
			Text emailText = new Text();
			if (sender.length != 0){
				String email = sender == null ? null : ((InternetAddress) sender[0]).getAddress();
				emailText.setText(email);				
			} else {
				emailText.setText("Deleted");
			}

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
			HBox messageLine = new HBox();
			HBox.setHgrow(messageLine, Priority.ALWAYS);
			messageLine.getStyleClass().add("messageLine"); // css #messageLine class
			messageLine.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
						if(mouseEvent.getClickCount() == 2){
							try {
								if (currentlyInDrafts)
									new ComposeMailWindow(smtp, mailbox, message);
								else {
									new MessageWindow(message, smtp, mailbox);
									messageLine.setId("seenMessageLine");									
								}	
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
				messageLine.setId("seenMessageLine");
			else 
				messageLine.setId("unseenMessageLine");
			
			//create GridPane to display message sender, subject and date
			GridPane mgp = new GridPane();
			
			ColumnConstraints colEmail = new ColumnConstraints(300);
		    ColumnConstraints colSubject = new ColumnConstraints();
		    ColumnConstraints colDate = new ColumnConstraints(150);
		    colSubject.setHgrow(Priority.ALWAYS); // let subject grow
		    mgp.getColumnConstraints().addAll(colEmail, colSubject, colDate);
		    
			mgp.add(emailText, 0, 0);
			mgp.add(textSubject, 1, 0);
			mgp.add(textDate, 2, 0);

			HBox.setHgrow(mgp, Priority.ALWAYS);
			messageLine.getChildren().add(mgp);
			mainWindow.getChildren().add(messageLine);
			vScroll.setContent(mainWindow);
			vScroll.setFitToWidth(true);
		}
		messagePane.getChildren().addAll(headerPane, vScroll);
		VBox.setVgrow(vScroll, Priority.ALWAYS); // after header, fill rest of screen with messages
		return messagePane;
	}
}
