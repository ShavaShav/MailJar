package app.view;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;

import app.MainApp;
import app.model.MailboxModel;
import app.model.SMTPModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class MailboxWindow extends Stage {

	private int maxMessages = 50; 
	private AnchorPane root;
	private MailboxModel mailbox; // model that supplies methods to get info for window
	private Tab currentTab; // for refreshing and other utilities
	private SMTPModel smtp;
	private boolean currentlyInDrafts = false;
	private int unreadMessages;
	private String greeting;
	private Text info, prompt;
	
	private static final int DELAY = 50;

	private double PADDING = 10;
	private double TOP_HEIGHT = 180;
	
	public MailboxWindow(MailboxModel mailbox, SMTPModel smtp) throws MessagingException, IOException{
		setTitle("Mailbox");
		this.getIcons().addAll(MainApp.ICONS);
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
		AnchorPane.setBottomAnchor(tabBox, PADDING + 60);
		AnchorPane.setLeftAnchor(tabBox, PADDING);
		AnchorPane.setRightAnchor(tabBox, PADDING);
		
		generateBottomElements();
		
		// import css
		root.getStylesheets().add("app/view/common.css");
		root.getStylesheets().add("app/view/MailboxWindowStyles.css");

		// if this window is closed, then whole application will close
		this.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				try {
					mailbox.closeModel();
				} catch (MessagingException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Unable to close to store", JOptionPane.ERROR_MESSAGE);
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
			// try to refresh after 50 milliseconds: enough time on my PC to display the prompt
			Timeline timeline = new Timeline(new KeyFrame(
			        Duration.millis(DELAY),
			        ae -> {
			    		// try to refresh
			        	try {	
							int newEmails = mailbox.refresh();
							prompt.setText("");
							setTabContentToCurrentFolder(currentTab);
						} catch (Exception e) {
							e.printStackTrace();
						}
			        }
			));
			timeline.play();
			
			prompt.setText("Refreshing...");
		});
		refresh.getStyleClass().add("buttonClass");
		
		//info message
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String time = sdf.format(cal.getTime());
        int hour = Integer.parseInt(time);
        info = new Text();
        
        if (hour >= 6 && hour < 12){
        	greeting = "Good morning ";
        }
        else if (hour >= 12 && hour < 18){
        	greeting = "Good afternoon ";
        }
        else if (hour >=18 && hour < 24) {
        	greeting = "Good evening ";
        }
        else{
        	greeting = "Good night ";
        }
        
        //Text info = new Text("Good night " + mailbox.getEmail() + ",\nYou have 4 unread messages");
        
        Font infoFont = new Font("Verdana", 25);
		info.setFont(infoFont);
		composeBtn.getStyleClass().add("infoText");
		
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
					JOptionPane.showMessageDialog(null, e.getMessage(), "Unable to close to store", JOptionPane.ERROR_MESSAGE);
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
		AnchorPane.setTopAnchor(info, PADDING*5);
		AnchorPane.setRightAnchor(info, PADDING*15);
		
		buttonWindow.getChildren().addAll(composeBtn, refresh);
		root.getChildren().addAll(logoView, buttonWindow, menuBar, info);
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
				JOptionPane.showMessageDialog(null, e.getMessage(), "Unable to open subfolders", JOptionPane.ERROR_MESSAGE);
			}
			Tab tab = new Tab();
			tab.setText(folders[i].getName());
			
			tabs.getTabs().add(tab);
			tab.setId(String.valueOf(i)); // mapping to folder index
			tab.getStyleClass().add("tab");
		}
		
		// when tab is clicked, open folder and set tab content to messages
		tabs.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
			
			// load tab's folder according to it's id
			int folderIndex = Integer.parseInt(newTab.getId());
			Folder folder = folders[folderIndex];
			
			Timeline timeline = new Timeline(new KeyFrame(
			        Duration.millis(DELAY),
			        ae -> {
			        	try {
							mailbox.openFolder(folder);
							currentTab = newTab;
							//set the greeting text with the current number of unread messages
							updateGreeting();
							
							// set content of tab to folder messages
							setTabContentToCurrentFolder(currentTab);
							prompt.setText("");
							// if draft, set boolean so we can open different compose window
							if (folder.getName().equals("Drafts"))
								currentlyInDrafts = true;
							else
								currentlyInDrafts = false;
					} catch (Exception e) {
							JOptionPane.showMessageDialog(null , "Can't open the folder for this tab!", "Exception!", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
					} 
			 }));
			timeline.play();
			prompt.setText("Opening folder " + folder.getName());	

		});
		
		// attempt to set open the first tab's folder
		try {
			mailbox.openFolder(folders[0]);
			currentTab = tabs.getTabs().get(0);
			setTabContentToCurrentFolder(currentTab);
			tabs.getSelectionModel().selectFirst();
			
			//set the greeting text with the current number of unread messages
			updateGreeting();
			
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(null, "Can't open first folder", "Exception", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		return tabs;
	}
	
	public void updateGreeting(){
		try {
			unreadMessages = mailbox.getCurrentFolder().getUnreadMessageCount();
			info.setText(greeting + mailbox.getEmail().split("@")[0] + ",\nYou have " 
					+ unreadMessages + " unread messages in " + currentTab.getText());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// the actual message pane part gets set to the content of a tab here
	private void setTabContentToCurrentFolder(Tab tab){
		try {
			VBox messagePane = getMessagePane(mailbox.getNextTenMessages());
			tab.setContent(messagePane);
			updateGreeting();
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(null, "Can't open folder", "Exception", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private GridPane getMessageHeaderPane(){
		Text from = new Text();
		from.setText("E-MAIL");
		Text subject = new Text();
		subject.setText("SUBJECT");
		Text date = new Text();
		date.setText("DATE");
		
		GridPane headerWindow = new GridPane();
		headerWindow.setPrefHeight(40);
		
		ColumnConstraints column1 = new ColumnConstraints(300);
	    ColumnConstraints column2 = new ColumnConstraints();
	    column2.setHgrow(Priority.ALWAYS);
	    ColumnConstraints column3 = new ColumnConstraints(215);
	    headerWindow.getColumnConstraints().addAll(column1, column2, column3);
	
		headerWindow.add(from, 0, 0);
		headerWindow.add(subject, 1, 0);
		headerWindow.add(date, 2, 0);
		headerWindow.setId("headers");
		
		return headerWindow;
	}

	private VBox getMessagePane(ArrayList<Message> messages) throws MessagingException{
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
		for (Message message : messages) {

			//obtain message senders email
			Address[] sender = message.getFrom();
			Text emailText = new Text();
			if (sender.length != 0){
				String email = sender == null ? null : ((InternetAddress) sender[0]).getAddress();
				emailText.setText(email);				
			} else {
				emailText.setText("Deleted"); //TODO This makes trash unable to be opened!
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
									updateGreeting();
									
								}	
							} catch (Exception e) {
								// TODO Print a message if unable to open!
								e.printStackTrace();
							}
						}
					}
				}
			});
			
			//delete button
			Button delete = new Button();
			Image deleteGraphic = new Image(getClass().getResourceAsStream("/img/delete.png"));
			delete.setGraphic(new ImageView(deleteGraphic));
			delete.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent arg0) {
					try {
						message.setFlag(Flag.DELETED, true);
						mailbox.getCurrentFolder().expunge();
						messageLine.setStyle("-fx-background-color: lightsalmon;");
					} catch (MessagingException e) {
						JOptionPane.showMessageDialog(null, "Unable to delete message", "Exception", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					} // delete message
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
		    ColumnConstraints colDelete = new ColumnConstraints(50);
		    colSubject.setHgrow(Priority.ALWAYS); // let subject grow
		    mgp.getColumnConstraints().addAll(colEmail, colSubject, colDate, colDelete);
		    
			mgp.add(emailText, 0, 0);
			mgp.add(textSubject, 1, 0);
			mgp.add(textDate, 2, 0);
			mgp.add(delete, 3, 0);

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
	
	private void generateBottomElements() {
		prompt = new Text();

		
		HBox bottomButtons = new HBox();
		final Button previousButton = new Button("Previous 10");
		previousButton.getStyleClass().add("buttonClass");
		previousButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				try {
					currentTab.setContent(getMessagePane(mailbox.getPrevTenMessages()));
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		final Button nextButton = new Button("Next 10");
		nextButton.getStyleClass().add("buttonClass");
		nextButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				try {
					currentTab.setContent(getMessagePane(mailbox.getNextTenMessages()));
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		bottomButtons.getChildren().addAll(previousButton, nextButton);
		
		//sizing
		bottomButtons.setPadding(new Insets(PADDING));
		bottomButtons.setSpacing(10);
		
		//anchoring
		AnchorPane.setLeftAnchor(prompt, PADDING-5);
		AnchorPane.setBottomAnchor(prompt, PADDING-5);
		AnchorPane.setRightAnchor(bottomButtons, PADDING-5);
		AnchorPane.setBottomAnchor(bottomButtons, PADDING-5);
		
		bottomButtons.setAlignment(Pos.BOTTOM_RIGHT);
		root.getChildren().addAll(bottomButtons, prompt);
		
		
	}
}
