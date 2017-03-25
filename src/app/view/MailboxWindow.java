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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.SingleSelectionModel;
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
	private Button composeBtn, refresh;
	private VBox mainWindow;
	private int numFolders;
	private Folder[] folders;
	private TabPane tabs;
	private GridPane headerWindow;
	private VBox containerWindow;
	private Text from;
	private Text subject;
	private Text date;

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
		
		Button refresh = new Button ("Refresh");
		refresh.getStyleClass().add("buttonClass");
		refresh.setPrefSize(150.0, 40.0);
		
		from = new Text();
		from.setText("e-mail");
		subject = new Text();
		subject.setText("subject");
		date = new Text ();
		date.setText("date");
		
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
		buttonWindow.setSpacing(10);

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
			if (i == 1) continue;
			
			from = new Text();
			from.setText("E-MAIL");
			subject = new Text();
			subject.setText("SUBJECT");
			date = new Text ();
			date.setText("DATE");
			
			containerWindow = new VBox();
			containerWindow.setPrefWidth(1180);
			containerWindow.setPrefHeight(580);
			
			headerWindow = new GridPane();
			headerWindow.setPrefHeight(40);
			headerWindow.setPrefWidth(1180);
			
			ColumnConstraints column1 = new ColumnConstraints(450);
		    ColumnConstraints column2 = new ColumnConstraints();
		    column2.setHgrow(Priority.ALWAYS);
		    ColumnConstraints column3 = new ColumnConstraints(150);
		    headerWindow.getColumnConstraints().addAll(column1, column2, column3);
		
			headerWindow.add(from, 0, 0);
			headerWindow.add(subject, 1, 0);
			headerWindow.add(date, 2, 0);
			headerWindow.setId("headers");

			Tab tab = new Tab();
			tab.setText(folders[i].getName());
			System.out.println(folders[i].getFullName());
			mailbox.openFolder(folders[i]); // load tab's folder
			containerWindow.getChildren().addAll(headerWindow, (displayMessages(mailbox.getMessages())));
			tab.setContent(containerWindow); // set content to folder's messages
			tabs.getTabs().add(tab);
			tab.getStyleClass().add("tab");
		}
		
		//add nodes to scenes
		buttonWindow.getChildren().addAll(composeBtn, refresh);
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

	public ScrollPane displayMessages(Message[] messages) throws MessagingException{
		
		//create message window
		mainWindow = new VBox();
		mainWindow.setId("mainWindow");
		ScrollPane vScroll = new ScrollPane();
		vScroll.setId("vScroll");
		vScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		mainWindow.setPrefHeight(540);
		mainWindow.setPrefWidth(1180);
		vScroll.setPrefWidth(1180);
		vScroll.setPrefHeight(540);
		
		AnchorPane.setTopAnchor(tabs, TOP_HEIGHT + PADDING);
		AnchorPane.setBottomAnchor(tabs, PADDING);
		AnchorPane.setRightAnchor(tabs, PADDING);
		AnchorPane.setLeftAnchor(tabs, PADDING);
		
		AnchorPane.setTopAnchor(mainWindow, TOP_HEIGHT + PADDING);
		AnchorPane.setLeftAnchor(mainWindow, PADDING);
		
		AnchorPane.setTopAnchor(vScroll, TOP_HEIGHT + PADDING);
		AnchorPane.setLeftAnchor(vScroll, PADDING);
		
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
			messageLine.setHgrow(messageLine, Priority.ALWAYS);
			AnchorPane.setTopAnchor(messageLine, TOP_HEIGHT + PADDING);
			AnchorPane.setLeftAnchor(messageLine, PADDING);
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
			
			ColumnConstraints col1 = new ColumnConstraints(400);
		    ColumnConstraints col2 = new ColumnConstraints();
		    col2.setHgrow(Priority.ALWAYS);
		    ColumnConstraints col3 = new ColumnConstraints(150);
		    mgp.getColumnConstraints().addAll(col1, col2, col3);
		
//			mgp.getColumnConstraints().add(new ColumnConstraints(400));
//			mgp.getColumnConstraints().add(new ColumnConstraints(550));
//			mgp.getColumnConstraints().add(new ColumnConstraints(150));
			
			mgp.setPrefWidth(1140);
			mgp.setHgap(50.00);
			mgp.add(emailText, 0, 0);
			mgp.add(textSubject, 1, 0);
			mgp.add(textDate, 2, 0);

			messageLine.getChildren().add(mgp);
			mainWindow.getChildren().add(messageLine);
			vScroll.setContent(mainWindow);
		}
		return vScroll;
	}
}
