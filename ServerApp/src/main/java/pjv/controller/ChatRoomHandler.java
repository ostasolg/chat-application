package pjv.controller;

import pjv.model.*;
import pjv.utils.*;
import java.io.IOException;

/**
 * This class contains static methods to create, delete and find ChatRoom instances in the PostgreSQL database.
 *
 * @author Michal Pechník and Oľga Ostashchuk
 * @version 1.0
 */
public class ChatRoomHandler {

    /**
     * This method creates and saves new chatRoom with specified name to the database.
     *
     * @param chatRoomName String specified name of new chatRoom
     * @exception IOException if ChatRoom instance with specified name is in the database.
     */
    public static void addChatRoom(String chatRoomName) throws IOException {
        try {
            // check if chatRoom with specified name is in the database
            findChatRoom(chatRoomName);
        }
        catch (IOException e) {
            // create new Instance of ChatRoom and save it to the database
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setChatRoomName(chatRoomName);
            HibernateUtils.persistChatRoom(chatRoom);
            return;
        }
        throw new IOException("ChatRoom with this name already exists!");
    }

    /**
     * This method removes existing chatRoom with specified name from the database.
     *
     * @param chatRoomName String specified name of existing chatRoom
     * @exception IOException if ChatRoom instance with specified name is not in the database.
     */
    public static void deleteChatRoomFromDBS(String chatRoomName) throws IOException{
        HibernateUtils.deleteChatRoom(findChatRoom(chatRoomName));
    }

    /**
     * This method finds chatRoom with specified chatRoom name in the database.
     *
     * @param chatRoomName String specified name of existing chatRoom
     * @exception IOException if ChatRoom instance with specified name is not in the database.
     */
    public static ChatRoom findChatRoom(String chatRoomName) throws IOException {

        for (Object o : HibernateUtils.fetchChatRooms()) {
            ChatRoom chatRoom = (ChatRoom) o;
            // compare chatRoom name with input String
            if (chatRoom.getChatRoomName().equals(chatRoomName)) {
                // return found chatRoom
                return chatRoom;
            }
        }
        throw new IOException("ChatRoom with this name does not exist!");
    }
}
