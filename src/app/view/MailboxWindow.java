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
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	//private HBox messageLine;

	private double SPACING = 10, PADDING = 10;
	private double TOP_HEIGHT = 180;

	public MailboxWindow(MailboxModel mailbox) throws MessagingException, IOException{
		setTitle("Mailbox");
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

		//initialize variables
		root = new AnchorPane();
		buttonWindow = new HBox();
		buttonWindow.setAlignment(Pos.BOTTOM_LEFT);
		composeBtn = new Button ("Compose New Message");
		composeBtn.getStyleClass().add("buttonClass");
		composeBtn.setPrefSize(220.0, 40.0);
		composeBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				new ComposeMailWindow();
			}
		});
		
		// logo
		final Image logo = new Image("app/view/logo.png");
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

		//anchoring
		AnchorPane.setLeftAnchor(buttonWindow, PADDING);
		AnchorPane.setTopAnchor(buttonWindow, PADDING*3);
		AnchorPane.setRightAnchor(logoView, PADDING);
		AnchorPane.setTopAnchor(logoView, PADDING);
		
		//create tab pane
		tabs = new TabPane();
		tabs.setPrefWidth(1200);
		tabs.setPrefHeight(580);
		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabs.getStyleClass().add("tabPane");

		//create individual tabs for each mailbox folder
		for (int i=0; i<numFolders; i++)
		{
			Tab tab = new Tab();
			tab.setText(folders[i].getName());
			Message[] messages = mailbox.getMessages();
			displayMessages(messages);
			tab.setContent(vScroll);
			tabs.getTabs().add(tab);
			tab.getStyleClass().add("tab");
		}

		//add nodes to scenes
		buttonWindow.getChildren().add(composeBtn);
		root.getChildren().addAll(logoView, menuBar, buttonWindow, tabs);
		// import css
		root.getStylesheets().add("app/view/common.css");
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
		
		//create message window
		mainWindow = new VBox();
		mainWindow.setId("mainWindow");
		vScroll = new ScrollPane();
		vScroll.setId("vScroll");
		vScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		mainWindow.setPrefHeight(580);
		mainWindow.setPrefWidth(1180);
		vScroll.setPrefWidth(1180);
		vScroll.setPrefHeight(580);
		//vScroll.setPadding(new Insets(PADDING));
		
		AnchorPane.setTopAnchor(tabs, TOP_HEIGHT + PADDING);
		AnchorPane.setBottomAnchor(tabs, PADDING);
		AnchorPane.setRightAnchor(tabs, PADDING);
		AnchorPane.setLeftAnchor(tabs, PADDING);
		
		//display messages
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
			HBox messageLine = new HBox();
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
			vScroll.setContent(mainWindow);
		}
	}

}