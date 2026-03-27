package pjv.utils;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import pjv.model.*;


/**
 * This class contains static methods which utilize Hibernate framework. This methods can
 * fetch, persist and delete data from the PostgreSQL database.
 *
 * @author Oľga Ostashchuk and Michal Pechník
 * @version 1.0
 */
public class HibernateUtils {


    // SessionFactory
    public static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * This method returns configured SessionFactory instance.
     *
     * @return Configured SessionFactory instance
     */
    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure();
            return configuration.buildSessionFactory(new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build());

        } catch (Exception e) {
            throw new RuntimeException("There was an error building the factory");
        }
    }

    /**
     * This method fetches ChatRoom data from the database.
     *
     * @return List of ChatRoom instances
     */
    public static List fetchChatRooms() {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // fetch chatRooms data from the database
        List chatRooms = session.createCriteria(ChatRoom.class).list();
        //close session
        session.close();
        return chatRooms;
    }

    /**
     * This method persists ChatRoom instance to the database.
     *
     * @param chatRoom ChatRoom instance to be stored into the database.
     */
    public static void persistChatRoom(ChatRoom chatRoom) {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // save chatRoom data to the database
        session.save(chatRoom);
        session.getTransaction().commit();
        //close session
        session.close();
    }

    /**
     * This method deletes ChatRoom instance from the database.
     *
     * @param chatRoom ChatRoom instance to be deleted from the database.
     */
    public static void deleteChatRoom(ChatRoom chatRoom) {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // delete chatRoom data from the database
        session.delete(chatRoom);
        session.getTransaction().commit();
        //close session
        session.close();
    }

    /**
     * This method fetches Message data from the database.
     *
     * @return List of Message instances
     */
    public static List fetchMessages() {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // fetch messages data from the database
        List messages = session.createCriteria(Message.class).list();
        //close session
        session.close();
        return messages;
    }

    /**
     * This method persists Message instance to the database.
     *
     * @param message Message instance to be stored into the database.
     */
    public static void persistMessage(Message message) {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // save message data to the database
        session.save(message);
        session.getTransaction().commit();
        //close session
        session.close();
    }

    /**
     * This method deletes Message instance from the database.
     *
     * @param message Message instance to be deleted from the database.
     */
    public static void deleteMessage(Message message) {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // delete message data from the database
        session.delete(message);
        session.getTransaction().commit();
        //close session
        session.close();
    }

    /**
     * This method fetches Client data from the database.
     *
     * @return List of Client instances
     */
    public static List fetchClients() {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // fetch messages data from the database
        List clients = session.createCriteria(Client.class).list();
        //close session
        session.close();
        return clients;
    }

    /**
     * This method persists Client instance to the database.
     *
     * @param client Client instance to be stored into the database.
     */
    public static void persistClient(Client client) {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // save client data to the database
        session.save(client);
        session.getTransaction().commit();
        //close session
        session.close();
    }

    /**
     * This method deletes Client instance from the database.
     *
     * @param client Client instance to be deleted from the database.
     */
    public static void deleteClient(Client client) {
        // open Hibernate session
        Session session = HibernateUtils.sessionFactory.openSession();
        // begin transaction
        session.getTransaction().begin();
        // delete client data from the database
        session.delete(client);
        session.getTransaction().commit();
        //close session
        session.close();
    }
}
