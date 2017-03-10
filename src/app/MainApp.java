package app;

import app.view.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
    	new LoginWindow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
