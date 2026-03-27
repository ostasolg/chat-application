package pjv.utils;

import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pjv.model.Message;
import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertTrue;


public class HibernateUtilsTest {

    // ARRANGE

    private static Message testMessage1;
    private static Message testMessage2;
    private static Message testMessage3;

    @BeforeAll
    public static void setUp() {
        // create Message instance
        testMessage1 = new Message();
        testMessage1.setToChatRoomId(1);
        testMessage1.setFromClientId(1);
        testMessage1.setDateTime(LocalDateTime.now());
        testMessage1.setMessageText("test1");

        // create Message instance
        testMessage2 = new Message();
        testMessage2.setToChatRoomId(2);
        testMessage2.setFromClientId(2);
        testMessage2.setDateTime(LocalDateTime.now());
        testMessage2.setMessageText("test2");

        // create Message instance
        testMessage3 = new Message();
        testMessage3.setToChatRoomId(3);
        testMessage3.setFromClientId(3);
        testMessage3.setDateTime(LocalDateTime.now());
        testMessage3.setMessageText("test3");
    }

    @AfterEach
    public void clean() {

        // get list of all messages
        Session session = HibernateUtils.sessionFactory.openSession();
        session.getTransaction().begin();
        List messages = session.createCriteria(Message.class).list();
        session.close();

        for (Object o : messages) {
            Message message = (Message) o;

            // delete testMessage1 if it is in the database
            if (message.getFromClientId() == testMessage1.getFromClientId() &&
                    message.getDateTime().isEqual(testMessage1.getDateTime()) &&
                    message.getToChatRoomId() == testMessage1.getToChatRoomId() &&
                    message.getMessageText().equals(testMessage1.getMessageText())) {

                Session session1 = HibernateUtils.sessionFactory.openSession();
                session1.getTransaction().begin();
                session1.delete(message);
                session1.getTransaction().commit();
                session1.close();
            }

            // delete testMessage2 if it is in the database
            if (message.getFromClientId() == testMessage2.getFromClientId() &&
                    message.getDateTime().isEqual(testMessage2.getDateTime()) &&
                    message.getToChatRoomId() == testMessage2.getToChatRoomId() &&
                    message.getMessageText().equals(testMessage2.getMessageText())) {

                Session session1 = HibernateUtils.sessionFactory.openSession();
                session1.getTransaction().begin();
                session1.delete(message);
                session1.getTransaction().commit();
                session1.close();
            }

            // delete testMessage3 if it is in the database
            if (message.getFromClientId() == testMessage3.getFromClientId() &&
                    message.getDateTime().isEqual(testMessage3.getDateTime()) &&
                    message.getToChatRoomId() == testMessage3.getToChatRoomId() &&
                    message.getMessageText().equals(testMessage3.getMessageText())) {

                Session session1 = HibernateUtils.sessionFactory.openSession();
                session1.getTransaction().begin();
                session1.delete(message);
                session1.getTransaction().commit();
                session1.close();
            }
        }
    }

    @Test
    public void persist_message_to_the_database_test() {

        // ACT

        // get size of list of all messages in the database
        Session session = HibernateUtils.sessionFactory.openSession();
        session.getTransaction().begin();
        List messagesFromDBS1 = session.createCriteria(Message.class).list();
        session.close();
        int sizeBeforePersisting = messagesFromDBS1.size();

        // add message to the database
        HibernateUtils.persistMessage(testMessage1);

        // get size of list of all messages in the database
        Session session2 = HibernateUtils.sessionFactory.openSession();
        session2.getTransaction().begin();
        List messagesFromDBS2 = session2.createCriteria(Message.class).list();
        session2.close();
        int sizeAfterPersisting = messagesFromDBS2.size();


        // ASSERT

        assertTrue(sizeBeforePersisting + 1 == sizeAfterPersisting);
    }

    @Test
    public void delete_message_from_the_database_test() {

        // ACT

        // add message to the database, which will be later deleted
        Session session1 = HibernateUtils.sessionFactory.openSession();
        session1.getTransaction().begin();
        session1.save(testMessage2);
        session1.getTransaction().commit();
        session1.close();

        // get size of list of all messages in the database
        Session session2 = HibernateUtils.sessionFactory.openSession();
        session2.getTransaction().begin();
        List messagesFromDBS1 = session2.createCriteria(Message.class).list();
        session2.close();
        int sizeBeforeDeleting = messagesFromDBS1.size();

        // delete message from the database
        Message messageToDelete = new Message();

        for (Object o : messagesFromDBS1) {
            Message message = (Message) o;
            if (message.getFromClientId() == testMessage2.getFromClientId() &&
                    message.getDateTime().isEqual(testMessage2.getDateTime()) &&
                    message.getToChatRoomId() == testMessage2.getToChatRoomId() &&
                    message.getMessageText().equals(testMessage2.getMessageText())) {

                messageToDelete = message;
            }
        }

        HibernateUtils.deleteMessage(messageToDelete);

        // get size of list of all messages in the database
        Session session3 = HibernateUtils.sessionFactory.openSession();
        session3.getTransaction().begin();
        List messagesFromDBS2 = session3.createCriteria(Message.class).list();
        session3.close();
        int sizeAfterDeleting = messagesFromDBS2.size();


        // ASSERT

        assertTrue(sizeBeforeDeleting - 1 == sizeAfterDeleting);
    }



    @Test
    public void get_list_of_all_messages_from_the_database_test() {

        // ACT

        // get size of list of all messages in the database
        int size_before = HibernateUtils.fetchMessages().size();

        // add message to the database
        Session session1 = HibernateUtils.sessionFactory.openSession();
        session1.getTransaction().begin();
        session1.save(testMessage1);
        session1.getTransaction().commit();
        session1.close();

        // add message to the database
        Session session2 = HibernateUtils.sessionFactory.openSession();
        session2.getTransaction().begin();
        session2.save(testMessage2);
        session2.getTransaction().commit();
        session2.close();

        // add message to the database
        Session session3 = HibernateUtils.sessionFactory.openSession();
        session3.getTransaction().begin();
        session3.save(testMessage3);
        session3.getTransaction().commit();
        session3.close();

        // get size of list of all messages in the database
        int size_after = HibernateUtils.fetchMessages().size();

        // ASSERT

        assertTrue(size_before + 3 == size_after);
    }
}