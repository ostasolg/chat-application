package pjv.controller;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import pjv.model.*;
import pjv.view.*;
import pjv.utils.*;

/**
 * This class contains methods where new instance of ServerSocket is created.
 * Then it creates server Socket instance and waits for incoming client connections.
 * Each successfully accepted connection from the client is handled in new thread.
 *
 * @author Michal Pechník and Oľga Ostashchuk
 * @version 1.0
 */
public class Server implements Runnable {


    // declare logger
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    // declaration of the ServerSocket variable
    private ServerSocket server = null;
    // declaration of the Socket variable
    private Socket socket = null;
    // declaration of the ArrayList variable for storing information about all running threads
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();

    /**
     * The Server class constructor.
     * It initializes ServerSocket instance with specified port number and stop variable.
     *
     * @param port int specified port number for ServerSocket instance
     */
    public Server(int port) {

        // configure logger
        this.configureLogger();

        try {
            // create instance of ServerSocket with port number as parameter
            server = new ServerSocket(port);

            // logger message
            LOGGER.info("Server has started!");

        } catch(IOException i) {
            LOGGER.warning(i.getMessage());
        }
    }

    /**
     * This method waits for incoming clients and accepted connection handles in new thread.
     */
    public void waitForNewClients() {
        // infinitive loop for waiting for new clients
        while (true) {
            try {
                // accept new client
                socket = server.accept();
                // logger message
                LOGGER.info("Client accepted!");
                // create new Thread instance
                Thread userAccept = new Thread(this);
                // run the thread
                userAccept.start();
            } catch (Exception i) {
                i.printStackTrace();
            }
        }
    }

    /**
     * This method configures Server class logger.
     */
    private void configureLogger() {
        FileHandler logFile;
        try {
            logFile = new FileHandler("server_logs.xml");
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(logFile);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.ALL, null, ex);
        }
    }

    /**
     * This method removes existing chatRoom with specified name,
     * all clients of this chatRoom and all messages sent in this chatRoom.
     *
     * @param chatRoomName String specified name of existing chatRoom
     * @exception Exception if ChatRoom instance with specified name is not in the database.
     */
    public static void removeChatRoom(String chatRoomName) throws Exception{

        //find chatRoom with specified chatRoom name
        ChatRoom chatRoom = ChatRoomHandler.findChatRoom(chatRoomName);
        // size of clientHandlers list
        int size = clientHandlers.size();
        // list of clientHandlers to be removed
        ArrayList<ClientHandler> toRemove = new ArrayList<>();
        // find client to be deleted
        for (int i = 0; i < size; i++) {
            ClientHandler clientHandler = clientHandlers.get(i);
            try {
                // check if chatRoomId is set
                clientHandler.getClient().getClientChatRoomId();
                // check if client is from specified chatRoom
                if (clientHandler.getClient().getClientChatRoomId() == chatRoom.getChatRoomId()) {
                    // add clientHandler to the list to be removed
                    toRemove.add(clientHandler);
                    // send warning message to the client
                    clientHandler.sendMessage("##session##end##");
                    // logger message
                    LOGGER.info("Client disconnected!");
                    // remove client from the database
                    HibernateUtils.deleteClient(clientHandler.getClient());
                    // logger message
                    LOGGER.info("Client deleted!");
                }
            } catch (Exception e) {
                // continue in case connected client has not chosen chatRoom yet
                continue;
            }
        }
        // remove ClientHandlers from list of all ClientHandler instances
        clientHandlers.removeAll(toRemove);

        // fetch all messages from the database which were sent in the chatRoom
        for (Object o: HibernateUtils.fetchMessages()) {
            Message message = (Message) o;
            // check if message was sent in specified chatRoom
            if (message.getToChatRoomId() == chatRoom.getChatRoomId()) {
                // remove message from the database
                HibernateUtils.deleteMessage(message);
            }
        }
        // logger message
        LOGGER.info("Messages deleted!");
        // remove chatRoom from the database
        ChatRoomHandler.deleteChatRoomFromDBS(chatRoomName);
        // logger message
        LOGGER.info("Chatroom deleted!");
    }

    /**
     * This method sends warning message to client and removes client from the database.
     *
     * @param client Client specified client
     */
    public static void removeClient(Client client) {
        // list of client to remove
        ArrayList<ClientHandler> toRemoveClient = new ArrayList<>();
        // size of clientHandlers list
        int size = clientHandlers.size();
        // find client to be deleted by admin
        for (int i = 0; i < size; i++) {
            ClientHandler clientHandler = clientHandlers.get(i);
            if (clientHandler.getClient().getClientUserName().equals(client.getClientUserName())) {
                // send warning message to the client
                clientHandler.sendMessage("##session##end##");
                // logger message
                LOGGER.info("Client disconnected!");
                // remove client from the database
                HibernateUtils.deleteClient(client);
                // logger message
                LOGGER.info("Client deleted!");
                // remove clientHandler from the list of clientHandlers
                toRemoveClient.add(clientHandler);
                break;
            }
        }
        clientHandlers.removeAll(toRemoveClient);
    }

    /**
     * The method overrides run() method of the Runnable interface.
     * It handles incoming messages from the client and sends them to
     * all clients of the same chatRoom connected to the server.
     */
    @Override
    public void run() {

        // create new ClientHandler instance with the socket of incoming client connection as parameter
        ClientHandler clientHandler = new ClientHandler(socket);
        // add ClientHandler instance to List of all ClientHandler instances
        clientHandlers.add(clientHandler);

        try {
            // send all chatRoom names to connected client
            clientHandler.sendChatRoomsToClient();

            // while client is connected
            while (!clientHandler.isExitThread()) {
                //handle incoming message from client
                clientHandler.takeMessageFromClient();
                // sleep for 100ms
                Thread.sleep(100);
            }

            // delete client from the database in case client has closed the application
            if (clientHandler.isExitThread()) {
                // delete client from the database
                HibernateUtils.deleteClient(clientHandler.getClient());
                // logger message
                LOGGER.info("Client deleted!");
                // refresh lists
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ServerWindow.refreshLists();
                    }
                });
                // end communication with client
                clientHandler.closeConnection();
                // remove clientHandler instance from list of all ClientHandler instances
                clientHandlers.remove(clientHandler);
                // logger message
                LOGGER.info("Client disconnected!");
                return;
            }
        } catch (Exception e) {

            // remove clientHandler instance from list of all ClientHandler instances
            clientHandlers.remove(clientHandler);
            // logger message
            LOGGER.info("Client disconnected!");
            return;
        }
    }

    /**
     * This method gets list of all ClientHandler instances.
     *
     * @return list of ClientHandler instances
     */
    public static ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }


    /**
     * This method closes server socket.
     */
    public void endServer() {
        try {
            // close Socket instance
            socket.close();
        } catch (Exception ex) {
        }
        try {
            // close ServerSocket instance
            server.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // logger message
        LOGGER.info("Server has ended!");
    }


    /**
     * The method sends warning message to all connected clients and removes all clients from the database.
     */
    public void clearClients() {
        // send warning message to all clients
        for (ClientHandler clientHandler: clientHandlers) {
            clientHandler.sendMessage("##session##end##");
            // logger message
            LOGGER.info("Client disconnected!");
        }
        // delete all clients from the database
        for (Object o : HibernateUtils.fetchClients()) {
            Client client = (Client) o;
            HibernateUtils.deleteClient(client);
            // logger message
            LOGGER.info("Client deleted!");
        }
    }

    /**
     * This method gets ServerSocket instance.
     *
     * @return ServerSocket instance
     */
    public ServerSocket getServer() {
        return server;
    }

    /**
     * This method sets ServerSocket instance.
     *
     * @param  server ServerSocket
     */
    public void setServer(ServerSocket server) {
        this.server = server;
    }

    /**
     * This method gets Socket instance.
     *
     * @return Socket instance
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * This method sets Socket instance.
     *
     * @param socket Socket
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

