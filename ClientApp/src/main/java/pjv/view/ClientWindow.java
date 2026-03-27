package pjv.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import pjv.controller.ChatroomWindowController;
import pjv.controller.Client;


import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// This is the main class of the application
// To run the application you should execute following command:
// java --module-path /opt/javafx-sdk-14.0.1/lib --add-modules javafx.controls,javafx.fxml,javafx.media -jar ClientApp-1-jar-with-dependencies.jar
// Please note, that execution requires javafx modules to be included (see command line)


/**
 * This is the main class for this application. It starts running the application.
 *
 * @author Michal Pechník and Oľga Ostashchuk
 * @version 1.0
 */
public class ClientWindow extends Application {

    // declare logger
    private final static Logger LOGGER = Logger.getLogger(ClientWindow.class.getName());

    // declaration of gui elements
    private TextField userNameTextField;
    private static ComboBox<String> comboBox;
    private Label errorLabel;
    private Stage primaryStage;

    // declaration of variable which stores data for socket communication
    private static Client client;



    /**
     * This method sets snd shows scene of the primary stage and creates new thread for socket communication.
     *
     * @param stage Stage the primary stage, onto which the application scene can be set.
     * @exception Exception when a Thread abruptly terminates due to an uncaught exception.
     */
    @Override
    public void start(Stage stage) throws Exception {

        // configure logger
        configureLogger();

        primaryStage = stage;
        // add the scene to the Stage
        primaryStage.setScene(createScene());
        // set the title of the Stage
        primaryStage.setTitle("ClientApp");
        // set the min. height of the Stage
        primaryStage.setMinHeight(400);
        // set the min. width of the Stage
        primaryStage.setMinWidth(650);
        // display the Stage
        primaryStage.show();
        // set actions when client window will be closed
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                try {
                    // send warning message to server
                    client.sendMessage("##session##end##");
                    // close socket communication with server
                    client.closeConnection();
                    Platform.exit();
                    System.exit(0);
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        });

        // create new Client instance in new thread
        new Thread(() -> {
            // create Client instance
            client = new Client("127.0.0.1", 5000);
            // receive names of all currently existing chatRooms from server
            client.loadChatRooms();
        }).start();
    }


    /**
     * This method configures ClientWindow class logger.
     */
    private void configureLogger() {
        FileHandler logFile;
        try {
            logFile = new FileHandler("client_window_logs.xml");
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(logFile);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(ClientWindow.class.getName()).log(Level.ALL, null, ex);
        }
    }

    /**
     * This method creates a new scene for the primary stage of this application.
     *
     * @return Scene instance
     */
    public Scene createScene() {

        // create main label
        Label text = new Label();
        text.setText("Please fill in the form ");
        text.getStyleClass().add("labelMain");

        // create textField and label for username
        userNameTextField = new TextField();
        Label loginLabel = new Label("Username: ");
        loginLabel.getStyleClass().add("label");
        loginLabel.setLabelFor(userNameTextField);


        // create comboBox and label for chatRoom
        Label comboLabel = new Label("ChatRoom: ");
        comboBox = new ComboBox();
        comboLabel.setLabelFor(comboLabel);
        comboLabel.getStyleClass().add("label");

        // create button for entering chatRoom
        Button button = new Button("Enter Chatroom");
        button.setOnAction(this:: enterChatRoom);
        button.getStyleClass().clear();
        button.getStyleClass().add("button");

        GridPane root = new GridPane();
        root.setAlignment(Pos.TOP_CENTER);
        // set padding 9px top, bottom, 14 px left, right
        root.setPadding(new Insets(3,14,9,14));
        // set vertical gap to 3
        root.setVgap(3);
        // set horizontal gap to 3
        root.setHgap(3);

        // create label for error messages
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        root.add(errorLabel, 1, 10);

        root.add(text,1,25);
        root.addRow(40, loginLabel,userNameTextField);

        root.addRow(45,comboLabel,comboBox);
        root.add(button,1,50);

        loginLabel.setAlignment(Pos.CENTER);

        // create new Scene instance
        Scene scene = new Scene(root);
        // add stylesheet
        scene.getStylesheets().add(getClass().getResource("/style1.css").toExternalForm());
        // return created scene
        return scene;
    }


    /**
     * This method refreshes comboBox of chatRooms.
     */
    public static void refreshRooms() {
        // set chatRoom names received from server
        comboBox.setItems(FXCollections.observableArrayList(client.getChatRoomNames()));
    }

    /**
     * This method sends username and chosen chatRoom to server and enters the selected chatRoom.
     *
     * @param event ActionEvent when clicked on the Enter chatRoom button.
     */
    private void enterChatRoom(ActionEvent event) {
        // hide error label
        errorLabel.setVisible(false);

        // check if all fields are filled
        if (!userNameTextField.getText().equals("")) {
            try {
                // check if all fields are filled
                comboBox.getValue().toString();

                // save client's input data
                ArrayList<String> list = new ArrayList<>();
                list.add(userNameTextField.getText());
                list.add(comboBox.getValue());

                // send client's data to server
                try {
                    // send clients data
                    client.sendMessage(client.prepareArrayList(list));
                    // receive response message from server
                    client.loadString();

                    // process the response
                    if (!client.isCheck()) {
                        // set error label if username is taken
                        errorLabel.setText("This username is taken or chatRoom was deleted," + "\n" +
                                " please choose another one!");
                        errorLabel.setVisible(true);
                        return;
                    }

                    // logger message
                    LOGGER.info("ChatRoom entered!");

                    // load all old messages of selected chatRoom
                    client.loadOldMessages();
                    ChatroomWindowController.setClient(client);

                    // load fxml file
                    Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("chatroomWindow.fxml"));
                    // create new Scene instance according to loaded fxml file
                    Scene scene2 = new Scene(root);
                    // change scene of the stage to chatRoom scene
                    primaryStage.setScene(scene2);
                    // set the min. height of the Stage
                    primaryStage.setMinHeight(400);
                    // set the min. width of the Stage
                    primaryStage.setMinWidth(650);
                    // set the title of the Stage
                    primaryStage.setTitle(comboBox.getValue() + " " + userNameTextField.getText());

                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            } catch (Exception e) {
                // show error label
                errorLabel.setText("All fields must be filled!");
                errorLabel.setVisible(true);
            }
        } else {
            // show error label
            errorLabel.setText("All fields must be filled!");
            errorLabel.setVisible(true);
        }
    }

    /**
     * This method starts running this application.
     *
     * @param args console arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
