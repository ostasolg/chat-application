package integrations;

import org.junit.jupiter.api.*;
import pjv.controller.Client;
import pjv.model.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ClientAppIntegrationTests {


    private Message testMessage;
    private ArrayList<String> clientData;

    @BeforeEach
    public void setUp() {
        // prepare message for sending to server
        LocalDateTime dateTime = LocalDateTime.now();
        testMessage = new Message();
        testMessage.setMessageText("TEST_MESSAGE_TEXT");
        testMessage.setDateTime(dateTime);
    }


    @Test
    public void login_and_send_public_message_test() {

        // ARRANGE

        // create new client and connect it to server
        Client client1 = new Client("127.0.0.1", 5000);
        client1.loadChatRooms();

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // set clients data
        clientData = new ArrayList<>();
        clientData.add("name1");
        clientData.add(client1.getChatRoomNames().get(0));

        // send clients data to server
        client1.sendMessage(client1.prepareArrayList(clientData));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client1.loadString();
        client1.loadOldMessages();


        // ACT

        // send public message for everyone in the chatRoom
        client1.sendMessage(client1.prepareMessage(testMessage));
        // receive public message from server
        Message result = client1.receiveMessage();

        // disconnect client
        client1.sendMessage("##session##end##");
        client1.closeConnection();


        // ASSERT

        assertEquals(clientData.get(0), result.getFromClientName());
        assertEquals(testMessage.getDateTime(), result.getDateTime());
        assertEquals(testMessage.getMessageText(), result.getMessageText());

    }


    @Test
    public void login_and_send_private_message_to_existing_client_test() {


        // ARRANGE

        // create new client and connect it to server
        Client client2 = new Client("127.0.0.1", 5000);
        client2.loadChatRooms();

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // set clients data
        clientData = new ArrayList<>();
        clientData.add("name2");
        clientData.add(client2.getChatRoomNames().get(0));

        // send clients data to server
        client2.sendMessage(client2.prepareArrayList(clientData));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client2.loadString();
        client2.loadOldMessages();

        // set receiver's name of message in order to send private message
        testMessage.setToClientName(clientData.get(0));


        // ACT

        // send private message in the chatRoom
        client2.sendMessage(client2.prepareMessage(testMessage));
        // receive private message in case client is sender or receiver, from server
        Message result = client2.receiveMessage();

        // disconnect client
        client2.sendMessage("##session##end##");
        client2.closeConnection();


        // ASSERT

        assertEquals(clientData.get(0), result.getToClientName());
        assertEquals(clientData.get(0), result.getFromClientName());
        assertEquals(testMessage.getDateTime(), result.getDateTime());
        assertEquals(testMessage.getMessageText(), result.getMessageText());

    }


    @Test
    public void login_and_send_private_message_to_not_existing_client_test() {


        // ARRANGE

        // create new client and connect it to server
        Client client11 = new Client("127.0.0.1", 5000);
        client11.loadChatRooms();

        // set clients data
        clientData = new ArrayList<>();
        clientData.add("name11");
        clientData.add(client11.getChatRoomNames().get(0));

        // send clients data to server
        client11.sendMessage(client11.prepareArrayList(clientData));

        // receive verifying answer from server and load old messages of chatRoom
        client11.loadString();
        client11.loadOldMessages();

        // set not existing receiver's name of message in order to send private message
        testMessage.setToClientName("RANDOM STRING");


        // ACT

        // send private message
        client11.sendMessage(client11.prepareMessage(testMessage));

        ArrayList<Message> result = new ArrayList<>();

        // new thread for waiting for new messages from server
        new Thread(() -> {
            while (true) {
                try {
                    // read new message
                    result.add(client11.receiveMessage());
                } catch (Exception e) {
                    continue;
                }
            }
        }).start();

        // disconnect client
        client11.sendMessage("##session##end##");
        client11.closeConnection();


        // ASSERT

        // check that private message to not existing client was not sent
        assertTrue(result.size() == 0);


    }


    @Test
    public void login_with_taken_username_not_allowed_test() {


        // ARRANGE

        // create new client and connect it to server
        Client client3 = new Client("127.0.0.1", 5000);
        client3.loadChatRooms();

        // set clients data
        clientData = new ArrayList<>();
        clientData.add("same_name");
        clientData.add(client3.getChatRoomNames().get(0));

        // send clients data to server
        client3.sendMessage(client3.prepareArrayList(clientData));

        // receive verifying answer from server and load old messages of chatRoom
        client3.loadString();
        client3.loadOldMessages();

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e);
        }

        // create new client and connect it to server
        Client client4 = new Client("127.0.0.1", 5000);
        client4.loadChatRooms();

        // set clients data
        clientData = new ArrayList<>();
        clientData.add("same_name");
        clientData.add(client4.getChatRoomNames().get(0));

        // send clients data to server
        client4.sendMessage(client4.prepareArrayList(clientData));

        // receive negative verifying answer
        client4.loadString();

        // disconnect clients
        client3.sendMessage("##session##end##");
        client3.closeConnection();
        client4.sendMessage("##session##end##");
        client4.closeConnection();

        // ACT

        boolean expectedCheck = false;
        boolean resultCheck = client4.isCheck();


        // ASSERT

        assertEquals(expectedCheck, resultCheck);
    }


    @Test
    public void load_old_public_messages_after_entering_chatroom() {


        // ARRANGE

        // create new client and connect it to server
        Client client5 = new Client("127.0.0.1", 5000);
        client5.loadChatRooms();

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // set clients data
        ArrayList<String> clientData5 = new ArrayList<>();
        clientData5.add("client5");
        clientData5.add(client5.getChatRoomNames().get(0));

        // send clients data to server
        client5.sendMessage(client5.prepareArrayList(clientData5));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client5.loadString();
        client5.loadOldMessages();

        // send public message for everyone in the chatRoom
        client5.sendMessage(client5.prepareMessage(testMessage));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive public message
        client5.receiveMessage();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(e);
        }

        // create new client and connect it to server
        Client client6 = new Client("127.0.0.1", 5000);
        client6.loadChatRooms();

        // set clients data
        clientData = new ArrayList<>();
        clientData.add("client6");
        clientData.add(client6.getChatRoomNames().get(0));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // send clients data to server
        client6.sendMessage(client6.prepareArrayList(clientData));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client6.loadString();
        client6.loadOldMessages();

        // disconnect clients
        client5.sendMessage("##session##end##");
        client5.closeConnection();
        client6.sendMessage("##session##end##");
        client6.closeConnection();


        // ACT

        // check that sent message by first client was loaded to second client as old messages after entering
        // the chatRoom
        Message result = new Message();

        for (Message message : client6.getMessages()) {

            if (message.getMessageText().equals(testMessage.getMessageText()) &&
                    message.getDateTime().isEqual(testMessage.getDateTime()) &&
                    message.getFromClientName().equals(clientData5.get(0))) {

                result = message;
                break;
            }
        }


        // ASSERT

        assertNotNull(result.getDateTime());
        assertNotNull(result.getMessageText());
        assertNotNull(result.getFromClientName());
    }

    @Test
    public void load_old_private_messages_after_entering_chatroom_by_sender_or_receiver() {

        // ARRANGE

        // create new client and connect it to server
        Client client7 = new Client("127.0.0.1", 5000);
        client7.loadChatRooms();

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // set clients data
        ArrayList<String> clientDataSender = new ArrayList<>();
        clientDataSender.add("client7");
        clientDataSender.add(client7.getChatRoomNames().get(0));

        // send clients data to server
        client7.sendMessage(client7.prepareArrayList(clientDataSender));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client7.loadString();
        client7.loadOldMessages();

        // create new client and connect it to server
        Client client8 = new Client("127.0.0.1", 5000);
        client8.loadChatRooms();

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // set clients data
        ArrayList<String> clientDataReceiver = new ArrayList<>();
        clientDataReceiver.add("client8");
        clientDataReceiver.add(client8.getChatRoomNames().get(0));

        // send clients data to server
        client8.sendMessage(client8.prepareArrayList(clientDataReceiver));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client8.loadString();
        client8.loadOldMessages();

        // set receiver's name of message in order to send private message
        testMessage.setToClientName("client8");

        // send private message to connected client
        client7.sendMessage(client7.prepareMessage(testMessage));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        client7.receiveMessage();
        client8.receiveMessage();

        // disconnect clients
        client7.sendMessage("##session##end##");
        client7.closeConnection();
        client8.sendMessage("##session##end##");
        client8.closeConnection();

        try {
            Thread.sleep(900);
        } catch (Exception e) {
            System.out.println("here");
        }

       //  create new client and connect it to server
        Client client9 = new Client("127.0.0.1", 5000);
        client9.loadChatRooms();


        // send the same clients data of sender to server
        client9.sendMessage(client9.prepareArrayList(clientDataSender));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client9.loadString();
        client9.loadOldMessages();

        // create new client and connect it to server
        Client client10 = new Client("127.0.0.1", 5000);
        client10.loadChatRooms();

        // send the same clients data of receiver to server
        client10.sendMessage(client10.prepareArrayList(clientDataReceiver));

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println(e);
        }

        // receive verifying answer from server and load old messages of chatRoom
        client10.loadString();
        client10.loadOldMessages();

        // disconnect clients
        client9.sendMessage("##session##end##");
        client9.closeConnection();
        client10.sendMessage("##session##end##");
        client10.closeConnection();

        // ACT

        // check that sender received the private message in loaded old messages
        Message resultForSender = new Message();

        for (Message message : client9.getMessages()) {

            if (message.getMessageText().equals(testMessage.getMessageText()) &&
                    message.getDateTime().isEqual(testMessage.getDateTime()) &&
                    message.getFromClientName().equals(clientDataSender.get(0))) {

                resultForSender = message;
                break;
            }
        }

        // check that receiver received the private message in loaded old messages
        Message resultForReceiver = new Message();

        for (Message message : client9.getMessages()) {

            if (message.getMessageText().equals(testMessage.getMessageText()) &&
                    message.getDateTime().isEqual(testMessage.getDateTime()) &&
                    message.getToClientName().equals(clientDataReceiver.get(0))) {

                resultForReceiver = message;
                break;
            }
        }


        // ASSERT

        assertNotNull(resultForSender.getDateTime());
        assertNotNull(resultForSender.getMessageText());
        assertNotNull(resultForSender.getFromClientName());
        assertEquals(resultForSender.getToClientName(), clientDataReceiver.get(0));

        assertNotNull(resultForReceiver.getDateTime());
        assertNotNull(resultForReceiver.getMessageText());
        assertNotNull(resultForReceiver.getFromClientName());
        assertEquals(resultForReceiver.getToClientName(), clientDataReceiver.get(0));
    }


    @Test
    public void check_message_was_sent_to_correct_chatRoom() {

        // ARRANGE

        // create new client and connect it to server
        Client client12 = new Client("127.0.0.1", 5000);
        client12.loadChatRooms();

        // set clients data
        ArrayList<String> clientData1 = new ArrayList<>();
        clientData1.add("client12");
        clientData1.add(client12.getChatRoomNames().get(0));

        // send clients data to server
        client12.sendMessage(client12.prepareArrayList(clientData1));

        // receive verifying answer from server and load old messages of chatRoom
        client12.loadString();
        client12.loadOldMessages();

        // create new client and connect it to server
        Client client13 = new Client("127.0.0.1", 5000);
        client13.loadChatRooms();

        // set clients data
        ArrayList<String> clientData2 = new ArrayList<>();
        clientData2.add("client13");
        clientData2.add(client13.getChatRoomNames().get(1));

        // send clients data to server
        client13.sendMessage(client13.prepareArrayList(clientData2));

        // receive verifying answer from server and load old messages of chatRoom
        client13.loadString();
        client13.loadOldMessages();


        // send public message to chatRoom of first client
        client12.sendMessage(client12.prepareMessage(testMessage));


        // ACT

        // receive sent message
        Message expMessage = testMessage;
        Message result1 = client12.receiveMessage();

        ArrayList<Message> result2 = new ArrayList<>();
        // new thread for waiting for new messages from server
        new Thread(() -> {
            while (true) {
                try {
                    // read new message
                    result2.add(client13.receiveMessage());
                } catch (Exception e) {
                    continue;
                }
            }
        }).start();

        // ASSERT

        // check message was sent only to chatRoom from which it was sent
        assertEquals(expMessage.getMessageText(), result1.getMessageText());
        assertEquals(expMessage.getDateTime(), result1.getDateTime());
        assertEquals(clientData1.get(0), result1.getFromClientName());

        assertEquals(0, result2.size());

        // disconnect clients
        client12.sendMessage("##session##end##");
        client12.closeConnection();
        client13.sendMessage("##session##end##");
        client13.closeConnection();
    }

    @Test
    public void check_private_message_was_not_sent_to_another_person_in_the_same_chatRoom() {

        // ARRANGE

        // create new client and connect it to server
        Client client14 = new Client("127.0.0.1", 5000);
        client14.loadChatRooms();

        // set clients data
        ArrayList<String> clientData1 = new ArrayList<>();
        clientData1.add("client14");
        clientData1.add(client14.getChatRoomNames().get(0));

        // send clients data to server
        client14.sendMessage(client14.prepareArrayList(clientData1));

        // receive verifying answer from server and load old messages of chatRoom
        client14.loadString();
        client14.loadOldMessages();



        // create new client and connect it to server
        Client client15 = new Client("127.0.0.1", 5000);
        client15.loadChatRooms();

        // set clients data
        ArrayList<String> clientData2 = new ArrayList<>();
        clientData2.add("client15");
        clientData2.add(client15.getChatRoomNames().get(0));

        // send clients data to server
        client15.sendMessage(client15.prepareArrayList(clientData2));

        // receive verifying answer from server and load old messages of chatRoom
        client15.loadString();
        client15.loadOldMessages();



        // create new client and connect it to server
        Client client16 = new Client("127.0.0.1", 5000);
        client16.loadChatRooms();

        // set clients data
        ArrayList<String> clientData3 = new ArrayList<>();
        clientData3.add("client16");
        clientData3.add(client16.getChatRoomNames().get(0));

        // send clients data to server
        client16.sendMessage(client16.prepareArrayList(clientData3));

        // receive verifying answer from server and load old messages of chatRoom
        client16.loadString();
        client16.loadOldMessages();

        try {
            Thread.sleep(900);
        } catch (Exception e) {
            System.out.println("here");
        }


        // ACT

        // set receiver's name of message in order to send private message
        testMessage.setToClientName("client15");

        // send private message to connected client
        client14.sendMessage(client14.prepareMessage(testMessage));

        try {
            Thread.sleep(900);
        } catch (Exception e) {
            System.out.println("here");
        }

        Message result1 = client14.receiveMessage();
        Message result2 = client15.receiveMessage();

        Message expResult = testMessage;


        ArrayList<Message> result3 = new ArrayList<>();
        // new thread for waiting for new messages from server
        new Thread(() -> {
            while (true) {
                try {
                    // read new message
                    result3.add(client16.receiveMessage());
                } catch (Exception e) {
                    continue;
                }
            }
        }).start();


        // ASSERT

        // check private message was sent only to sender and receiver
        assertEquals(expResult.getMessageText(), result1.getMessageText());
        assertEquals(expResult.getDateTime(), result1.getDateTime());
        assertEquals(clientData1.get(0), result1.getFromClientName());
        assertEquals(clientData2.get(0), result1.getToClientName());

        assertEquals(expResult.getMessageText(), result2.getMessageText());
        assertEquals(expResult.getDateTime(), result2.getDateTime());
        assertEquals(clientData1.get(0), result2.getFromClientName());
        assertEquals(clientData2.get(0), result2.getToClientName());

        assertEquals(0, result3.size());

        // disconnect clients
        client14.sendMessage("##session##end##");
        client14.closeConnection();
        client15.sendMessage("##session##end##");
        client15.closeConnection();
        client16.sendMessage("##session##end##");
        client16.closeConnection();
    }
}
