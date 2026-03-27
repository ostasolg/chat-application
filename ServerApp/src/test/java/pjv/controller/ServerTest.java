package pjv.controller;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class ServerTest {

    //ARRANGE

    private static Server  server;
    private static int portInput;

    @BeforeAll
    public static void setUp() {
        portInput = 5000;
        server = new Server(portInput);
    }


    @Test
    public void server_constructor_test() {

        //ACT
        int port = server.getServer().getLocalPort();

        //ASSERT
        assertEquals(portInput, port);
    }


    @Test
    public void start_server_and_delete_chatRoom_test() throws Exception {

        // ARRANGE

        ChatRoomHandler.addChatRoom("test1");


        // ACT

        Server.removeChatRoom("test1");


        Exception exception = Assertions.assertThrows(IOException.class, () ->
                ChatRoomHandler.findChatRoom("test1"));

        String expectedMessage = "ChatRoom with this name does not exist!";
        String actualMessage = exception.getMessage();


        // ASSERT

        Assertions.assertEquals(expectedMessage, actualMessage);
    }
}
