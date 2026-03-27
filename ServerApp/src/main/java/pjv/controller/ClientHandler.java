package pjv.controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import pjv.model.*;
import pjv.utils.*;
import pjv.view.ServerWindow;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles incoming client connection, enables sending and receiving messages from client.
 *
 * @author Michal Pechník and Oľga Ostashchuk
 * @version 1.0
 */
public class ClientHandler {

    // declare logger
    private final static Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    // connection with client
    private Socket socket;
    // incoming messages Scanner
    private Scanner input;
    // outComing messages Writer
    private PrintWriter output;

    // client instance
    private Client client;
    // variable which stores information whether the client has closed the application
    private boolean exitThread;
    // variable which stores information whether first message with client data was already received from client
    private boolean firstInputCompleted;

    /**
     * The ClientHandler class constructor. It initializes socket, incoming messages scanner
     * and outcoming messages PrintWriter.
     *
     * @param socket Socket
     */
    public ClientHandler(Socket socket) {

        // configure logger
        this.configureLogger();

        try {
            // initialize server socket
            this.socket = socket;
            // initialize incoming messages Scanner
            input = new Scanner(socket.getInputStream());
            // initialize outcoming messages PrintWriter
            output = new PrintWriter(socket.getOutputStream());


            firstInputCompleted = false;
            exitThread = false;

        } catch (IOException ex) {
            LOGGER.warning(ex.getMessage());
        }
    }

    /**
     * This method configures ClientHandler class logger.
     */
    private void configureLogger() {
        FileHandler logFile;
        try {
            logFile = new FileHandler("client_handler_logs.xml");
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(logFile);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.ALL, null, ex);
        }
    }

    /**
     * This method takes input message from client.
     */
    public void takeMessageFromClient() {

        // if there is available incoming message
       if(input.hasNext()) {
           if (isFirstInputCompleted()) {
               // take Message instance
               takeMessage();
           } else {
               // take ArrayList instance
               takeInputClientData();
           }
       } else {
           return;
       }
    }

    /**
     * This method saves message from client, parses it to Message instance, saves the message to the database
     * and sends to all connected clients in the same chatRoom if receiver's name is not stated, otherwise server checks
     * the receiver's name in database and sends the message only to sender and receiver.
     */
    public void takeMessage() {
        // read the incoming message from the Scanner instance
        String inString = input.nextLine();

        // check whether client has closed the application
        if (!inString.equals("##session##end##")) {
            // create new Message instance
            Message message = parseMessage(inString);
            // set client id of Message instance
            message.setFromClientId(client.getClientId());
            // set chatRoom id of Message instance
            message.setToChatRoomId(client.getClientChatRoomId());
            // set client's name of Message instance
            message.setFromClientName(client.getClientUserName());

            // check if receiver's name is set
            if (message.getToClientName() != null) {
                // check if client with that receiver's name is in the database and in the same chatRoom
                for (Object o : HibernateUtils.fetchClients()) {
                    Client client1 = (Client) o;

                    // if client with that receiver's name is in the database and in the same chatRoom
                    if (client1.getClientUserName().equals(message.getToClientName()) &&
                            client1.getClientChatRoomId() == client.getClientChatRoomId()) {
                        // save message to the database
                        HibernateUtils.persistMessage(message);
                        // find sender and receiver of message and send them the message
                        for (ClientHandler clientHandler : Server.getClientHandlers()) {
                            if ((clientHandler.client.getClientUserName().equals(message.getFromClientName())) ||
                                    (clientHandler.client.getClientUserName().equals(message.getToClientName()))) {
                                // send message to client
                                clientHandler.sendMessage(prepareMessage(message));
                            }
                        }
                        try {
                            // sleep for 100ms
                            Thread.sleep(100);
                            return;
                        } catch (Exception e) {
                            LOGGER.warning(e.getMessage());
                            return;
                        }
                    }
                } // do not save the message to the database and do not send the message
                    return;
            } else {
                // save message to the database
                HibernateUtils.persistMessage(message);
                // find all clients connected in the same chatRoom
                for (ClientHandler clientHandler : Server.getClientHandlers()) {
                    if (clientHandler.client.getClientChatRoomId() == client.getClientChatRoomId()) {
                        // send message to next client in the same chatRoom
                        clientHandler.sendMessage(prepareMessage(message));
                    }
                }
                try {
                    // sleep for 100ms
                    Thread.sleep(100);
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        } else {
            setExitThread(true);
        }
    }

    /**
     * This method saves message from client, parses it to ArrayList instance and checks the input username in the
     * database. Then it sends answer to client.
     */
    public void takeInputClientData() {
        // create new list
        ArrayList<String> clientData = new ArrayList<>();
        // read the incoming message from the Scanner instance
        String inString = input.nextLine();

        // check whether client has closed the application
        if (!inString.equals("##session##end##")) {
            // parse the incoming String to Arraylist instance and save it
           clientData = parseArrayList(inString);

           // check the username in the database
           if (checkUniquenessOfUserName(clientData.get(0))) {
               // logger message
               LOGGER.info("Username accepted!");
               // create Client instance
               client = new Client();
               // set checked username to Client instance
               client.setClientUserName(clientData.get(0));

               // find the chosen chatRoom by name and save its id to Client instance
               try {
                   client.setClientChatRoomId(ChatRoomHandler.findChatRoom(clientData.get(1)).getChatRoomId());
               } catch (IOException e) {
                   sendMessage("no");
                   return;
               }
               // confirm that first input from client is done
               firstInputCompleted = true;
               // save the Client instance to the database
               HibernateUtils.persistClient(client);

               // send confirmation message to client
               sendMessage("yes");

               // refresh lists
               Platform.runLater(new Runnable() {
                   @Override
                   public void run() {
                       ServerWindow.refreshLists();
                   }
               });

               // send all old messages of selected chatRoom to connected client
               sendAllMessages();
               return;
           }
           sendMessage("no");
        } else {
            setExitThread(true);
        }
    }

    /**
     * This method sends all public messages sent in the selected chatRoom on this day and sends private
     * massages sent in the selected chatRoom on this day in case the client is their sender or receiver.
     */
    public void sendAllMessages() {
        // create list
        ArrayList<Message> allMessages = new ArrayList<>();

        // find messages sent in selected chatRoom on this day
        for(Object o : HibernateUtils.fetchMessages()) {
            Message message = (Message) o;

            // date of message
            int year = message.getDateTime().getYear();
            int month = message.getDateTime().getMonthValue();
            int day = message.getDateTime().getDayOfMonth();

            LocalDate date = LocalDate.of(year, month, day);

            // compare chatRoom id and date of sending
            if (message.getToChatRoomId() == client.getClientChatRoomId() && date.isEqual(LocalDate.now())) {

                // check if receiver's name is set
                if (message.getToClientName() != null) {
                    // check if client is the sender or receiver
                    if (client.getClientUserName().equals(message.getFromClientName()) ||
                            client.getClientUserName().equals(message.getToClientName())) {
                        // add message to list
                        allMessages.add(message);
                    }
                } else {
                    // add message to list
                    allMessages.add(message);
                }
            }
        }
        // send list
        sendMessage(prepareArrayListOfMessage(allMessages));
    }

    /**
     * This method checks whether the username chosen by client is already in the database.
     *
     * @param userName String username of client to be checked
     * @return boolean value
     */
    public boolean checkUniquenessOfUserName(String userName) {
        boolean unique = true;
        // get usernames of all clients
        for (Object client: HibernateUtils.fetchClients()) {
            Client client1 = (Client) client;
            // compare username from the database with input username
            if (userName.equals(client1.getClientUserName())) {
                // if both usernames are equal then return false
                unique = false;
                break;
            }
        }
        return unique;
    }

    /**
     * This method sends list of names of currently existing chatRooms to connected client.
     */
    public void sendChatRoomsToClient() {
        try {
            // create new list
            ArrayList<String> chatRoomNames = new ArrayList<String>();

            // get names of all existing chatRooms and save them to list
            for(Object o : HibernateUtils.fetchChatRooms()) {
                ChatRoom chatRoom = (ChatRoom) o;
                chatRoomNames.add(chatRoom.getChatRoomName());
            }
            // logger message
            LOGGER.info("ChatRoom names sent!");
            // send the list to connected client
            sendMessage(prepareArrayList(chatRoomNames));
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * This method converts Message instance to String in order to prepare it for sending to client.
     *
     * @param message Message to be sent to client
     */
    public String prepareMessage(Message message) {
        Gson gson = new Gson();
        String strMessage = gson.toJson(message);
        return strMessage;
    }

    /**
     * This method converts ArrayList instance of String values to String in order to prepare it for sending to client.
     *
     * @param list ArrayList of String values to be sent to client
     */
    public String prepareArrayList(ArrayList<String> list) {
        Gson gson = new Gson();
        String strList = gson.toJson(list);
        return strList;
    }

    /**
     * This method converts ArrayList instance of Message values to String in order to prepare it for sending to client.
     *
     * @param list ArrayList of Message instances to be sent to client
     */
    public String prepareArrayListOfMessage(ArrayList<Message> list) {
        Gson gson = new Gson();
        String strList = gson.toJson(list);
        return strList;
    }

    /**
     * This method parses String message to the Message instance.
     *
     * @param strMessage String message to be parsed
     * @return Parsed Message instance
     */
    public static Message parseMessage(String strMessage) {
        Gson g = new Gson();
        return g.fromJson(strMessage, Message.class);
    }

    /**
     * This method parses String message to the ArrayList instance.
     *
     * @param strList String to be parsed
     * @return Parsed Arraylist instance
     */
    public static ArrayList<String> parseArrayList(String strList) {
        Gson g = new Gson();
        return g.fromJson(strList, ArrayList.class);
    }

    /**
     * This method sends String message to connected client
     *
     * @param strMessage String message to be sent.
     */
    public void sendMessage(String strMessage) {
        try {
            // send message
            output.println(strMessage);
            output.flush();
        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage());
        }
    }

    /**
     * This method closes communication and connection with connected client.
     */
    public void closeConnection() {
        try {
            // close incoming messages Scanner
            input.close();
            // close outComing messages PrintWriter
            output.close();
            // close server socket
            socket.close();
        } catch (IOException i) {
            LOGGER.warning(i.getMessage());
        }
    }

    /**
     *  This method sets the value of variable which stores data whether client has closed the application.
     *
     * @param exitThread boolean
     */
    public void setExitThread(boolean exitThread) {
        this.exitThread = exitThread;
    }

    /**
     * This method gets the value of variable which stores data whether client has closed the application.
     *
     * @return boolean value
     */
    public boolean isExitThread() {
        return exitThread;
    }

    /**
     * This method sets Client instance of connected client.
     *
     * @param client Client
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * This method gets Client instance of connected client.
     *
     * @return Client instance
     */
    public Client getClient() {
        return client;
    }

    /**
     * This method gets value of variable which stores information whether first message with client data
     * was already received from client.
     *
     * @return boolean value
     */
    public boolean isFirstInputCompleted() {
        return firstInputCompleted;
    }

    /**
     * This method sets value of variable which stores information whether first message with client data
     * was already received from client.
     *
     * @param firstInputCompleted boolean
     */
    public void setFirstInputCompleted(boolean firstInputCompleted) {
        this.firstInputCompleted = firstInputCompleted;
    }
}
