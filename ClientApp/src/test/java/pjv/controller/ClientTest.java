package pjv.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pjv.model.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {

    // ARRANGE

    private static Message testMessage;
    private static ArrayList<String> clientData;

    @BeforeAll
    public static void setUp() {
        testMessage = new Message();
        testMessage.setFromClientName("TEST_SENDER");
        testMessage.setToClientName("TEST_RECEIVER");
        testMessage.setFromClientId(1);
        testMessage.setToChatRoomId(1);
        testMessage.setMessageText("TEST_MESSAGE_TEXT");
        testMessage.setDateTime(LocalDateTime.of(2020, 6, 4, 21, 1, 28,
                837098000));

        clientData = new ArrayList<>();
        clientData.add("name");
        clientData.add("chatRoom");
    }


    @Test
    public void parse_string_to_Message_instance_test() {

        // ACT

        String strMessage = "{\"idMessage\":0,\"fromClientId\":1,\"fromClientName\":\"TEST_SENDER\",\"toClientName\"" +
                ":\"TEST_RECEIVER\",\"toChatRoomId\":1,\"messageText\":\"TEST_MESSAGE_TEXT\",\"dateTime\":{\"date\"" +
                ":{\"year\":2020,\"month\":6,\"day\":4},\"time\":{\"hour\":21,\"minute\":1,\"second\":28,\"nano\"" +
                ":837098000}}}";
        Message expResult = testMessage;
        Message result = Client.parseMessage(strMessage);


        // ASSERT

        assertEquals(expResult.toString(), result.toString());
    }

    @Test
    public void parse_string_to_ArrayList_instance_test() {

        // ACT

        String strArrayList = "[\"name\",\"chatRoom\"]";
        ArrayList<String> expResult = clientData;
        ArrayList<String> result = Client.parseArrayListOfString(strArrayList);


        // ASSERT

        assertEquals(expResult.toString(), result.toString());
    }
}
