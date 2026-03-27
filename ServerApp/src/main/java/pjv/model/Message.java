package pjv.model;

import javax.persistence.*;
import java.time.LocalDateTime;


/**
 * Message entity class.
 * This class is mapped by the Hibernate.
 * It contains all required information about the messages sent by clients.
 *
 * @author Oľga Ostashchuk and Michal Pechník
 * @version 1.0
 */
@Entity
@Table(name = "Message")
public class Message {

    // ID of message
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "idMessage")
    private int idMessage;

    // ID of client who sent the message
    @Column(name = "fromClientId")
    private int fromClientId;

    // name of client who sent the message
    @Column(name = "fromClientName")
    private String fromClientName;

    // name of client to whom the private message was sent
    @Column(name = "toClientName")
    private String toClientName;

    // ID of chatRoom in which the message was sent
    @Column(name = "toChatRoomId")
    private int toChatRoomId;

    // text of the message
    @Column(name = "messageText")
    private String messageText;

    // date and time the message was sent
    @Column(name = "dateTime")
    private LocalDateTime dateTime;


    /**
     * This method gets ID of Message instance.
     *
     * @return Message's ID
     */
    public int getIdMessage() {
        return idMessage;
    }

    /**
     * This method sets ID of Message instance.
     *
     * @param idMessage int
     */
    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }

    /**
     * This method gets ID of client who sent the message.
     *
     * @return ID of client
     */
    public int getFromClientId() {
        return fromClientId;
    }

    /**
     * This method sets ID of client who sent the message.
     *
     * @param fromClientId int
     */
    public void setFromClientId(int fromClientId) {
        this.fromClientId = fromClientId;
    }

    /**
     * This method gets ID of chatRoom in which the message was sent.
     *
     * @return ID of chatRoom
     */
    public int getToChatRoomId() {
        return toChatRoomId;
    }

    /**
     * This method sets ID of chatRoom in which the message was sent.
     *
     * @param  toChatRoomId int
     */
    public void setToChatRoomId(int toChatRoomId) {
        this.toChatRoomId = toChatRoomId;
    }

    /**
     * This method gets text of the sent message.
     *
     * @return text of the message
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * This method sets text of the sent message.
     *
     * @param  messageText String
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /**
     * This method gets date and time when the message was sent.
     *
     * @return date and time when the message was sent
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * This method sets date and time when the message was sent.
     *
     * @param  dateTime LocalDateTime
     */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * This method gets name of client who sent the message.
     *
     * @return name of client
     */
    public String getFromClientName() {
        return fromClientName;
    }

    /**
     * This method sets name of client who sent the message.
     *
     * @param fromClientName String
     */
    public void setFromClientName(String fromClientName) {
        this.fromClientName = fromClientName;
    }

    /**
     * This method gets name of client to whom the private message was sent.
     *
     * @return name of client
     */
    public String getToClientName() {
        return toClientName;
    }

    /**
     * This method sets name of client to whom the private message was sent.
     *
     * @param toClientName String - name of client
     */
    public void setToClientName(String toClientName) {
        this.toClientName = toClientName;
    }

    /**
     * This method overrides toString() method of Object class
     * to create a string representation of the Message instance.
     *
     * @return String of Message instance
     */
    @Override
    public String toString() {
        return "Message " +
                "from: " + fromClientName +
                "\n" +
                "dateTime: " + dateTime.toString().replace("T", " ") +
                "\n" +
                "text: " +
                messageText +
                "\n";
    }

    /**
     * This method creates a string representation of the Message instance if the sent message was private.
     *
     * @return String of Message instance
     */
    public String privateToString() {
        return
                "Message " +
                "from: " + fromClientName +
                "\n" +
                "to: " + toClientName +
                "\n" +
                "dateTime: " + dateTime.toString().replace("T", " ") +
                "\n" +
                "text: " +
                messageText +
                "\n" +
                "\n";
    }
}
