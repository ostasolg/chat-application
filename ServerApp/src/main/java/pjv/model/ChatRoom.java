package pjv.model;

import javax.persistence.*;

/**
 * ChatRoom entity class.
 * This class is mapped by the Hibernate.
 * It contains all required information about the chatRoom.
 *
 * @author Oľga Ostashchuk and Michal Pechník
 * @version 1.0
 */
@Entity
@Table(name = "ChatRoom")
public class ChatRoom {

    // ID of chatRoom
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "chatRoomId")
    private int chatRoomId;

    // name of chatRoom
    @Column(name = "chatRoomName", unique = true)
    private String chatRoomName;

    /**
     * This method gets ID of ChatRoom instance.
     *
     * @return ChatRoom's ID
     */
    public int getChatRoomId() {
        return chatRoomId;
    }

    /**
     * This method sets ID of ChatRoom instance.
     *
     * @param chatRoomId int
     */
    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    /**
     * This method gets name of ChatRoom instance.
     *
     * @return ChatRoom's name
     */
    public String getChatRoomName() {
        return chatRoomName;
    }

    /**
     * This method sets name of ChatRoom instance.
     *
     * @param chatRoomName String
     */
    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }
}
