package pjv.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.WindowEvent;
import pjv.controller.ChatRoomHandler;
import pjv.controller.Server;
import pjv.utils.HibernateUtils;
import pjv.model.*;

// This is the main class of the application
// To run the application you should execute following command:
// java --module-path /opt/javafx-sdk-14.0.1/lib --add-modules javafx.controls,javafx.fxml,javafx.media -jar ServerApp-1-jar-with-dependencies.jar
// Please note, that execution requires javafx modules to be included (see command line)


/**
 * This is the main class for this application. It starts running the application.
 *
 * @author Michal Pechník and Oľga Ostashchuk
 * @version 1.0
 */
public class ServerWindow extends Application  {

    // declare logger
    private final static Logger LOGGER = Logger.getLogger(ServerWindow.class.getName());

    // declaration of gui elements
    private static ListView chatRoomList;
    private static ListView clientList;
    private Button delChatRoom;
    private Button showClients;
    private Button showAllClients;
    private Button delClient;
    private Button createChatRoomButton;
    private TextField createChatRoomText = new TextField();
    private static Label errorLab;


    // declaration of variables which store data of clicked list elements
    private static String chatRoomNameString;
    private static String clientUserNameString;

    // declaration of Server instance
    private volatile Server serverRunning;

    /**
     * This method sets snd shows scene of the primary stage.
     *
     * @param primaryStage Stage the primary stage, onto which the application scene can be set.
     * @exception Exception when a Thread abruptly terminates due to an uncaught exception.
     */
    public void start(Stage primaryStage) throws Exception {

        // configure logger
        configureLogger();

        // add the scene to the Stage
        primaryStage.setScene(createScene());
        // set the title of the Stage
        primaryStage.setTitle("ServerApp");
        // set the min. height of the Stage
        primaryStage.setMinHeight(500);
        // set the min. width of the Stage
        primaryStage.setMinWidth(800);
        // display the Stage
        primaryStage.show();

        // show data when app starts running
        refreshLists();

        // create new Server instance in new thread
        new Thread(() -> {
            serverRunning = new Server(5000);
            serverRunning.waitForNewClients();
        }).start();

        // set action on closing the server window
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                try {
                    // inform clients about closing
                    serverRunning.clearClients();
                    // close socket communication
                    serverRunning.endServer();
                    Platform.exit();
                    System.exit(0);
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        });
    }

    /**
     * This method configures ServerWindow class logger.
     */
    private void configureLogger() {
        FileHandler logFile;
        try {
            logFile = new FileHandler("server_window_logs.xml");
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(logFile);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(ServerWindow.class.getName()).log(Level.ALL, null, ex);
        }
    }

    /**
     * This method creates a new scene for the primary stage of this application.
     *
     * @return Scene instance
     */
    public Scene createScene() {

        // create main pane
        BorderPane mainBP = new BorderPane();

        // set background image
        Image img = new Image("bg.jpg");
        BackgroundImage background = new BackgroundImage(img,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        mainBP.setBackground(new Background(background));

        // set main heading on pane
        Label mainLabel = new Label();
        mainLabel.setPadding(new Insets(50, 0, 0, 0));
        mainLabel.setText("SERVER APPLICATION");
        mainLabel.getStyleClass().add("mainLab");

        mainBP.setTop(mainLabel);
        mainBP.setAlignment(mainLabel, Pos.BOTTOM_CENTER);

        // create labels for lists
        BorderPane centerBP = new BorderPane();
        mainBP.setCenter(centerBP);

        HBox topHB = new HBox();
        topHB.setAlignment(Pos.CENTER);
        topHB.setPadding(new Insets(50, 60, 25, 0));
        topHB.setSpacing(80);
        centerBP.setTop(topHB);

        Label chatroomLabel = new Label();
        chatroomLabel.setText("  List of chatRooms");
        chatroomLabel.getStyleClass().add("lab");
        topHB.getChildren().add(chatroomLabel);

        Label clientLabel = new Label();
        clientLabel.setText("  List of clients");
        clientLabel.getStyleClass().add("lab");
        topHB.getChildren().add(clientLabel);

        // create list of all chatRooms
        HBox centerHB = new HBox();
        centerHB.setAlignment(Pos.CENTER);
        centerHB.setPadding(new Insets(0, 100, 150, 100));
        centerHB.setSpacing(100);
        centerBP.setCenter(centerHB);

        chatRoomList = new ListView();
        chatRoomList.getStyleClass().add("list");
        chatRoomList.setOnMouseClicked(this:: onMouseClickedChatRoomList);
        centerHB.getChildren().add(chatRoomList);

        // create list of all clients
        clientList = new ListView();
        clientList.getStyleClass().add("list");
        clientList.setOnMouseClicked(this:: onMouseClickedClientList);
        centerHB.getChildren().add(clientList);

        // create buttons for chatRoom and client lists
        HBox bottomHB = new HBox();
        bottomHB.setAlignment(Pos.CENTER);
        bottomHB.setPadding(new Insets(-100, 100, 150, 100));
        bottomHB.setSpacing(80);
        centerBP.setBottom(bottomHB);

        // create button for deleting chatRoom
        delChatRoom = new Button("Delete chatRoom");
        delChatRoom.getStyleClass().add("button");
        delChatRoom.setOnAction(this::deleteChatRoom);
        bottomHB.getChildren().add(delChatRoom);

        // create button for showing clients of selected chatRoom
        showClients = new Button("Show clients");
        showClients.getStyleClass().add("button");
        showClients.setOnAction(this::showClientsOfChatRoom);
        bottomHB.getChildren().add(showClients);

        // create button for showing all clients
        showAllClients = new Button("Show all clients");
        showAllClients.getStyleClass().add("button");
        showAllClients.setOnAction(this::showAllClients);
        bottomHB.getChildren().add(showAllClients);

        // create button for deleting client
        delClient = new Button("Delete client");
        delClient.getStyleClass().add("button");
        delClient.setOnAction(this::deleteClient);
        bottomHB.getChildren().add(delClient);

        // create label for creating new chatroom
        Label createClientLab = new Label();
        createClientLab.setText("Name of chatRoom: ");
        createClientLab.getStyleClass().add("lab");

        // create button for creating new chatroom
        createChatRoomButton = new Button("Create new chatroom");
        createChatRoomButton.getStyleClass().add("button");
        createChatRoomButton.setOnAction(this::createChatRoom);

        // create textField for writing name when creating new chatroom
        createChatRoomText = new TextField();
        createChatRoomText.getStyleClass().add("text-field");

        BorderPane bottomMain = new BorderPane();
        mainBP.setBottom(bottomMain);

        HBox bottomLastHB = new HBox();
        bottomLastHB.setAlignment(Pos.CENTER);
        bottomLastHB.setPadding(new Insets(-100, 100, 0, 100));
        bottomLastHB.setSpacing(50);
        bottomMain.setTop(bottomLastHB);

        bottomLastHB.getChildren().add(createClientLab);
        bottomLastHB.getChildren().add(createChatRoomText);
        bottomLastHB.getChildren().add(createChatRoomButton);

        //create error label
        errorLab = new Label();
        errorLab.getStyleClass().add("error");
        errorLab.setPadding(new Insets(0, 0, 10, 0));
        bottomMain.setCenter(errorLab);


        // create new instance of Scene
        Scene scene = new Scene(mainBP);
        // add stylesheet
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        // return scene
        return scene;
    }

    /**
     * This method refreshes list of chatroom and list of clients.
     */
    public static void refreshLists() {

        // logger message
        LOGGER.info("Lists refreshed!");

        // hide error label
        errorLab.setVisible(false);

        // initialize variable of Client list
        ArrayList<String> stringClient = new ArrayList<String>();

        for (Object o : HibernateUtils.fetchClients()) {
            Client client = (Client) o;
            // add client's username to list
            stringClient.add( client.getClientUserName());
        }

        clientList.setItems(FXCollections.observableArrayList(stringClient));


        // initialize variable of ChatRoom list
        ArrayList<String> stringChatRoom = new ArrayList<String>();

        for (Object o : HibernateUtils.fetchChatRooms()) {
            ChatRoom chatRoom = (ChatRoom) o;
            // add chatRoom's name to list
            stringChatRoom.add( chatRoom.getChatRoomName());
        }

        chatRoomList.setItems(FXCollections.observableArrayList(stringChatRoom));

        // clear values of variables which store data of clicked list elements
        chatRoomNameString = "";
        clientUserNameString = "";
    }

    /**
     * This method creates new chatRoom.
     *
     * @param event ActionEvent on clicking on the Create new chatRoom button.
     */
    public void createChatRoom(ActionEvent event) {

        // play sound after clicking on the button
        AudioClip audio = new AudioClip(getClass().getResource("/sound.mp3").toExternalForm());
        audio.play();

        // hide error label
        errorLab.setVisible(false);

        // check if textfield of chatRoom name is filled
        if (!createChatRoomText.getText().equals("")) {

            try {
                // create and add new chatRoom to the database
                ChatRoomHandler.addChatRoom(createChatRoomText.getText());
                createChatRoomText.setText("");
                clearVariables();
                // add created chatRoom to list of chatRooms
                refreshLists();
                // logger message
                LOGGER.info("ChatRoom created!");
            }
            catch (IOException e){
                // show error label
                errorLab.setText(e.getMessage());
                errorLab.setVisible(true);
            }
        } else {
            // show error label
            errorLab.setText("Field with chatRoom name must be filled!");
            errorLab.setVisible(true);
        }
    }

    /**
     * This method saves String of value of clicked item on chatRoom list.
     *
     * @param event MouseEvent on clicking on the item of chatRoom list.
     */
    public void onMouseClickedChatRoomList(MouseEvent event) {
        try {
            chatRoomNameString = chatRoomList.getSelectionModel().getSelectedItem().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method saves String of value of clicked item on client list.
     *
     * @param event MouseEvent on clicking on the item of client list.
     */
    public void onMouseClickedClientList(MouseEvent event) {
        try {
            clientUserNameString = clientList.getSelectionModel().getSelectedItem().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method shows clients of selected chatRoom.
     *
     * @param event ActionEvent on clicking on the Show clients button.
     */
    public void showClientsOfChatRoom(ActionEvent event) {

        // play sound after clicking on the button
        AudioClip audio = new AudioClip(getClass().getResource("/sound.mp3").toExternalForm());
        audio.play();

        // hide error label
        errorLab.setVisible(false);

        // check if chatRoom was selected from the chatRoom list
        if (!chatRoomNameString.equals("")) {
            ArrayList<String> stringClients = new ArrayList<String>();

            // find chatRoom with selected chatRoom name
            ChatRoom chatRoom = new ChatRoom();
            for (Object o : HibernateUtils.fetchChatRooms()) {
                chatRoom = (ChatRoom) o;
                if (chatRoom.getChatRoomName().equals(chatRoomNameString)) {
                    break;
                }
            }

            // find clients of selected chatRoom
            Client client;
            for (Object o : HibernateUtils.fetchClients()) {
                client = (Client) o;
                if (client.getClientChatRoomId() == chatRoom.getChatRoomId()) {
                    stringClients.add(client.getClientUserName());
                }
            }
            try {
                // update client list
                clientList.setItems(FXCollections.observableArrayList(stringClients));
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }

            // clear values of variables
            clearVariables();

        } else {
            // show error label
            errorLab.setText("Choose chatRoom from the list and click on it!");
            errorLab.setVisible(true);
        }
    }

    /**
     * This method shows all clients.
     *
     * @param event ActionEvent on clicking on the Show all clients button.
     */
    public void showAllClients(ActionEvent event) {


        // play sound after clicking on the button
        AudioClip audio = new AudioClip(getClass().getResource("/sound.mp3").toExternalForm());
        audio.play();

        // clear values of variables
        clearVariables();
        // hide error label
        errorLab.setVisible(false);
        // fill lists with all data in the database
        refreshLists();
    }

    /**
     * This method deletes client selected in client list.
     *
     * @param event ActionEvent on clicking on the Delete client button.
     */
    public void deleteClient(ActionEvent event) {

        // play sound after clicking on the button
        AudioClip audio = new AudioClip(getClass().getResource("/sound.mp3").toExternalForm());
        audio.play();

        // hide error label
        errorLab.setVisible(false);

        // check if client was selected in the client list
        if (!clientUserNameString.equals("")) {
            // find client by username
            Client client;
            for (Object o : HibernateUtils.fetchClients()) {
                client = (Client) o;
                if (client.getClientUserName().equals(clientUserNameString)) {
                    // remove client
                    Server.removeClient(client);
                    // logger message
                    LOGGER.info("Client deleted!");
                    // clear values of variables
                    clearVariables();
                    // refresh lists
                    refreshLists();
                } else {
                    // show error label
                    errorLab.setText("Please, try again later.");
                    errorLab.setVisible(true);
                }
            }
        } else {
            // show error label
            errorLab.setText("Choose client from the list and click on it!");
            errorLab.setVisible(true);
        }
    }

    /**
     * This method deletes chatRoom selected in chatRoom list.
     *
     * @param event ActionEvent on clicking on the Delete chatRoom button.
     */
    public void deleteChatRoom(ActionEvent event) {

        // play sound after clicking on the button
        AudioClip audio = new AudioClip(getClass().getResource("/sound.mp3").toExternalForm());
        audio.play();

        // hide error label
        errorLab.setVisible(false);

        // check if chatRoom was selected in the chatRoom list
        if (!chatRoomNameString.equals("")) {
            try {
                // remove chatRoom
                Server.removeChatRoom(chatRoomNameString);
                // logger message
                LOGGER.info("ChatRoom deleted!");
                clearVariables();
                refreshLists();
            } catch (Exception e) {
                // show error label
                errorLab.setText("Please, try again later.");
                errorLab.setVisible(true);
            }
        } else {
            // show error label
            errorLab.setText("Choose chatRoom from the list and click on it!");
            errorLab.setVisible(true);
        }
    }

    /**
     * This method clears values of variables which temporarily store data.
     */
    public void clearVariables() {
        chatRoomNameString = "";
        clientUserNameString = "";
    }

    /**
     * This method starts running this application.
     *
     * @param args console arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
