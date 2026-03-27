
package pjv.controller;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pjv.model.ChatRoom;
import pjv.utils.HibernateUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*",
        "com.sun.org.apache.xalan.*" })
@PrepareForTest({ HibernateUtils.class })
public class ChatRoomHandlerTest {

    ChatRoom chatRoom;

    @Test
    public void find_chatRoom_which_exists_test() throws Exception{

        // ARRANGE

        chatRoom = new ChatRoom();
        chatRoom.setChatRoomName("room1");

        PowerMockito.mockStatic(HibernateUtils.class);
        PowerMockito.doReturn(List.of(chatRoom)).when(HibernateUtils.class, "fetchChatRooms");


        // ACT + ASSERT

        assertEquals(chatRoom, ChatRoomHandler.findChatRoom("room1"));
    }

    @Test
    public void find_chatRoom_which_does_not_exist_throws_IOException_test() throws Exception{

        // ARRANGE

        PowerMockito.mockStatic(HibernateUtils.class);
        PowerMockito.doReturn(List.of()).when(HibernateUtils.class, "fetchChatRooms");


        // ACT

        Exception exception = Assertions.assertThrows(IOException.class, () ->
                ChatRoomHandler.findChatRoom("room1"));

        String expectedMessage = "ChatRoom with this name does not exist!";
        String actualMessage = exception.getMessage();


        // ASSERT

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void create_chatRoom_with_name_which_already_exists_throws_IOException_test() throws Exception{

        // ARRANGE

        chatRoom = new ChatRoom();
        chatRoom.setChatRoomName("room1");

        PowerMockito.mockStatic(HibernateUtils.class);
        PowerMockito.doReturn(List.of(chatRoom)).when(HibernateUtils.class, "fetchChatRooms");


        // ACT

        Exception exception = Assertions.assertThrows(IOException.class, () ->
                ChatRoomHandler.addChatRoom("room1"));

        String expectedMessage = "ChatRoom with this name already exists!";
        String actualMessage = exception.getMessage();


        // ASSERT

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void create_chatRoom_with_unique_name_does_not_throw_IOException_test() throws Exception{

        // ARRANGE

        chatRoom = new ChatRoom();
        chatRoom.setChatRoomName("room1");

        PowerMockito.mockStatic(HibernateUtils.class);
        PowerMockito.doReturn(List.of()).when(HibernateUtils.class, "fetchChatRooms");
        PowerMockito.doNothing().when(HibernateUtils.class, "persistChatRoom", chatRoom);


        // ACT + ASSERT

        Assertions.assertDoesNotThrow(() ->
                ChatRoomHandler.addChatRoom("room1"));
    }

    @Test
    public void delete_existing_chatRoom_does_not_throw_IOException() throws Exception{

        // ARRANGE

        chatRoom = new ChatRoom();
        chatRoom.setChatRoomName("room1");

        PowerMockito.mockStatic(HibernateUtils.class);
        PowerMockito.doReturn(List.of(chatRoom)).when(HibernateUtils.class, "fetchChatRooms");
        PowerMockito.doNothing().when(HibernateUtils.class, "deleteChatRoom", chatRoom);


        // ACT + ASSERT

        Assertions.assertDoesNotThrow(() ->
                ChatRoomHandler.deleteChatRoomFromDBS("room1"));
    }

    @Test
    public void delete_not_existing_chatRoom_throws_IOException() throws Exception{

        // ARRANGE

        chatRoom = new ChatRoom();
        chatRoom.setChatRoomName("room1");


        PowerMockito.mockStatic(HibernateUtils.class);
        PowerMockito.doReturn(List.of()).when(HibernateUtils.class, "fetchChatRooms");


        // ACT

        Exception exception = Assertions.assertThrows(IOException.class, () ->
                ChatRoomHandler.deleteChatRoomFromDBS("room1"));

        String expectedMessage = "ChatRoom with this name does not exist!";
        String actualMessage = exception.getMessage();


        // ASSERT

        Assertions.assertEquals(expectedMessage, actualMessage);
    }
}