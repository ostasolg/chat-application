package pjv.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.AudioClip;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import pjv.model.Message;

/**
 * This is the controller class for the second scene of this application.
 *
 * @author Michal Pechník and Oľga Ostashchuk
 * @version 1.0
 */
public class ChatroomWindowController implements Initializable {


    // declaration of variable which stores data for socket communication
    private static Client client;

    // declaration of elements located on the second scene of the main stage
    @FXML
    private TextArea textareaMessages;

    @FXML
    private TextArea textAreaWrittenMessage;

    @FXML
    private Button sendall;

    @FXML
    private TextField recieverName;

    @FXML
    private Button sendprivate;

    @FXML
    private Label errorLabel;


    /**
     * This method sends message to server after clicking on Send button.
     *
     * @param event ActionEvent when clicked on Send button
     */
    @FXML
    void sendMessageAll(ActionEvent event) {
        // hide error label
        errorLabel.setVisible(false);
        // check if message is not empty
        if (!textAreaWrittenMessage.getText().equals("")) {
            // check if input from client is not too long
            if (textAreaWrittenMessage.getText().length() < 250) {
                // create new Message instance
                Message messageToSend = new Message();
                // set date and time of sending message
                messageToSend.setDateTime(LocalDateTime.now());
                // set text of message
                messageToSend.setMessageText(textAreaWrittenMessage.getText());
                // send message to server
                client.sendMessage(client.prepareMessage(messageToSend));
                // clear textArea of message
                textAreaWrittenMessage.setText("");
            } else {
                // show error label
                errorLabel.setText("The input is too long!");
                errorLabel.setVisible(true);
            }
        } else {
            // show error label
            errorLabel.setText("Fill in the field with text of message first!");
            errorLabel.setVisible(true);
        }
    }

    /**
     * This method sends private message to server after clicking on Send Private button.
     *
     * @param event ActionEvent when clicked on Send Private button
     */
    @FXML
    void sendMessagePrivate(ActionEvent event) {
        // hide error label
        errorLabel.setVisible(false);

        // check if message is not empty
        if (!textAreaWrittenMessage.getText().equals("")) {
            // check if receiver's name is not empty
            if (!recieverName.getText().equals("")) {
                // check if input from client is not too long
                if (textAreaWrittenMessage.getText().length() < 250 && recieverName.getText().length() < 250) {
                    // create new Message instance
                    Message messageToSend = new Message();
                    // set date and time of sending message
                    messageToSend.setDateTime(LocalDateTime.now());
                    // set text of message
                    messageToSend.setMessageText(textAreaWrittenMessage.getText());
                    // set receiver's name of message
                    messageToSend.setToClientName(recieverName.getText());
                    // send message to server
                    client.sendMessage(client.prepareMessage(messageToSend));
                    // clear textArea of message
                    textAreaWrittenMessage.setText("");
                    recieverName.setText("");
                } else {
                    // show error label
                    errorLabel.setText("The input is too long!");
                    errorLabel.setVisible(true);
                }
            } else {
                // show error label
                errorLabel.setText("Fill in the field of receiver's name!");
                errorLabel.setVisible(true);
            }
        } else {
            // show error label
            errorLabel.setText("Fill in the field of text of message!");
            errorLabel.setVisible(true);
        }
    }

    /**
     * This method refreshes textArea of all messages.
     */
    public void refresh() {
        for (Message message : client.getMessages()) {
            // check if message was private
            if (message.getToClientName() != null) {
                // add message to textArea
                textareaMessages.appendText(message.privateToString());
            } else {
                // add message to textArea
                textareaMessages.appendText(message.toString());
            }
        }
    }

    /**
     * This method adds message to chat.
     *
     * @param message Message to be added to textArea of all sent messages
     */
    public void addMessage(Message message) {

        // play sound when receiving message
        AudioClip audio = new AudioClip(getClass().getResource("/sound.mp3").toExternalForm());
        audio.play();

        try {
            // sleep for 150ms
            Thread.sleep(150);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check if message was private
        if (message.getToClientName() != null) {
            // add message to textArea
            textareaMessages.appendText(message.privateToString());
        } else {
            // add message to textArea
            textareaMessages.appendText(message.toString());
        }
    }

    /**
     * This method gets Client instance.
     *
     * @return Client instance
     */
    public Client getClient() {
        return client;
    }

    /**
     * This method sets Client instance.
     *
     * @param  clientInput Client
     */
    public static void setClient(Client clientInput) {
        client = clientInput;
    }

    /**
     * This method overrides initialize() method of Initializable interface in order
     * to initialize textArea of all messages and start waiting for new messages.
     *
     * @param url URL - The location used to resolve relative paths for the root object.
     * @param resourceBundle ResourceBundle - The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
         refresh();
         waitForMessages();
    }

    /**
     * This method creates new Thread for waiting for new messages.
     */
    public void waitForMessages() {

        // new thread for waiting for new messages from server
        new Thread(() -> {
            while (true) {
                try {
                    // read new message
                    addMessage(client.receiveMessage());
                } catch (Exception e) {
                    continue;
                }
                }
        }).start();
    }
}