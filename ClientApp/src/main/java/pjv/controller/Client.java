package pjv.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pjv.model.Message;
import pjv.view.ClientWindow;

import java.lang.reflect.Type;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains methods where new instance of Socket is created.
 * It contains methods for sending public and private messages and methods for receiving messages from server.
 *
 * @author Michal Pechník and Oľga Ostashchuk
 * @version 1.0
 */
public class Client {

    // declare logger
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());


    // arrayList which contains names of currently existing chatRoom names
    private ArrayList<String> chatRoomNames;
    // arrayList which contains Message instances sent in chosen chatRoom after entering the chatRoom on this day
    private ArrayList<Message> messages;

    // client socket
    private Socket socket;
    // incoming messages Scanner
    private Scanner input;
    // outComing messages Writer
    private PrintWriter output;


    // variable which stores information whether the client username is unique in the database;
    private boolean check;


    /**
     * The Client class constructor.
     * It initializes socket with specified port number and IP address. Then it initializes incoming messages scanner
     * and outComing messages PrintWriter.
     *
     * @param address String specified IP address for socket instance
     * @param port int specified port number for socket instance
     */
    public Client(String address, int port) {

        // configure logger
        this.configureLogger();

        // establish a connection
        try {
            // connect to the server
            socket = new Socket(address, port);
            // initialize incoming messages Scanner
            input = new Scanner(socket.getInputStream());
            // initialize outComing messages PrintWriter
            output = new PrintWriter(socket.getOutputStream());

            // logger message
            LOGGER.info("Connected to server!");

            check = false;


        } catch(UnknownHostException u) {
            LOGGER.warning(u.getMessage());
            System.exit(1);
        }
        catch(IOException i) {
            LOGGER.warning(i.getMessage());
            System.exit(1);
        }
    }

    /**
     * This method configures Client class logger.
     */
    private void configureLogger() {
        FileHandler logFile;
        try {
            logFile = new FileHandler("client_logs.xml");
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(logFile);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.ALL, null, ex);
        }
    }

    /**
     * This method converts Message instance to String in order to prepare it for sending to server.
     *
     * @param message Message to be sent by client
     */
    public String prepareMessage(Message message) {
        Gson gson = new Gson();
        String strMessage = gson.toJson(message);
        return strMessage;
    }

    /**
     * This method converts ArrayList instance to String in order to prepare it for sending to server.
     *
     * @param list ArrayList of String values to be sent by client
     */
    public String prepareArrayList(ArrayList<String> list) {
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
     * This method parses String message to the ArrayList instance containing String instances.
     *
     * @param strList String to be parsed
     * @return Parsed Arraylist instance
     */
    public static ArrayList<String> parseArrayListOfString(String strList) {
        Gson g = new Gson();
        return g.fromJson(strList, ArrayList.class);
    }

    /**
     * This method parses String message to the ArrayList instance containing Message instances.
     *
     * @param strList String to be parsed
     * @return Parsed Arraylist instance
     */
    public static ArrayList<Message> parseArrayListOfMessage(String strList) {
        Type typeMyType = new TypeToken<ArrayList<Message>>() {
        }.getType();
        Gson g = new Gson();
        return g.fromJson(strList, typeMyType);
    }

    /**
     * This method sends String message to the server.
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
     * This method receives message with chatRoom names from server and parses it to ArrayList instance containing
     * String values.
     *
     * @return Arraylist with names of chatRooms
     */
    public ArrayList<String> receiveArrayListOfString() {
        try {
            // create new list
            ArrayList<String> strChatRooms = new ArrayList<>();
            // variable which stores information whether the client has received message from server
            boolean hasReceived = false;
            // while client has not received
            while (!hasReceived) {
                // if there is an incoming message, process it
                if (input.hasNext()) {
                    // read the incoming message from the Scanner instance
                    String inString = input.nextLine();
                    // check the input in order to know whether server app was closed or client was deleted
                    if (!inString.equals("##session##end##")) {
                        // logger message
                        LOGGER.info("ChatRoom names received!");
                        // parse the incoming String to Arraylist instance and save it
                        strChatRooms = parseArrayListOfString(inString);
                        //change value of variable
                        hasReceived = true;
                    } else {
                        // close connection
                        closeConnection();
                        System.exit(2);
                    }
                }
            }
            // return list
            return strChatRooms;
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return null;
    }

    /**
     * This method receives messages sent in chosen chatRoom on this day before entering the chatRoom and
     * parses it to ArrayList instance containing Message instances.
     *
     * @return ArrayList of Message instances
     */
    public ArrayList<Message> receiveArrayListOfMessages() {
        try {
            // create new list
            ArrayList<Message> messages = new ArrayList<>();
            // variable which stores information whether the client has received message from server
            boolean hasReceived = false;
            // while client has not received
            while (!hasReceived) {
                // if there is an incoming message, process it
                if (input.hasNext()) {
                    // read the incoming message from the Scanner instance
                    String inString = input.nextLine();
                    // check the input in order to know whether server app was closed or client was deleted
                    if (!inString.equals("##session##end##")) {
                        // logger message
                        LOGGER.info("Old messages received!");
                        // parse the incoming String to Arraylist instance and save it
                        messages = parseArrayListOfMessage(inString);
                        //change value of variable
                        hasReceived = true;
                    } else {
                        // close connection
                        closeConnection();
                        System.exit(0);
                    }
                }
            }
            // return list
            return messages;
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return null;
    }

    /**
     * This method receives message from server and parses it to Message instance.
     *
     * @return Message instance
     */
    public Message receiveMessage() {
        try {
            // create new Message instance
            Message message = new Message();
            // variable which stores information whether the client has received message from server
            boolean hasReceived = false;
            // while client has not received
            while (!hasReceived) {
                // if there is an incoming message, process it
                if (input.hasNext()) {
                    // read the incoming message from the Scanner instance
                    String inString = input.nextLine();
                    // check the input in order to know whether server app was closed or client was deleted
                    if (!inString.equals("##session##end##")) {
                        // parse the incoming String to Message instance and save it
                        message = parseMessage(inString);
                        //change value of variable
                        hasReceived = true;
                    } else {
                        // close connection
                        closeConnection();
                        System.exit(0);
                    }
                }
            }
            // return message
            return message;
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return null;
    }

    /**
     * This method receives message from server as a response to sending username.
     *
     * @return String message
     */
    public String receiveString() {
        try {
            // new String instance
            String response = new String();
            // variable which stores information whether the client has received message from server
            boolean hasReceived = false;
            // while client has not received
            while (!hasReceived) {
                // if there is an incoming message, process it
                if (input.hasNext()) {
                    // read the incoming message from the Scanner instance
                    String inString = input.nextLine();
                    // check the input in order to know whether server app was closed or client was deleted
                    if (!inString.equals("##session##end##")) {
                        // save the incoming String
                        response = inString;
                        //change value of variable
                        hasReceived = true;
                    } else {
                        // close connection
                        closeConnection();
                        System.exit(0);
                    }
                }
            }
            // return String instance
            return response;
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return null;
    }

    /**
     * This method loads list of chatRoom names from server.
     */
    public void loadChatRooms() {
        try {
            // receive and save the input message
            setChatRoomNames(receiveArrayListOfString());
            // refresh combobox of chatRoom names
            ClientWindow.refreshRooms();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * This method loads list of old messages of chosen ChatRoom from server.
     */
    public void loadOldMessages() {
        try {
            // save all messages received from server
            setMessages(receiveArrayListOfMessages());
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * This method loads String message from server as a response to checking username in the database.
     */
    public void loadString() {
        try {
            String response = receiveString();
            // if username is available
            if (response.equals("yes")) {
                setCheck(true);
            }
            // if username is not available
            if (response.equals("no")) {
                setCheck(false);
            }
        } catch(Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * This method sets list of messages sent in client's chatroom on this day, after entering the chatroom.
     *
     * @param messages ArrayList list of Message instances
     */
    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    /**
     * This method gets list of messages sent in client's chatroom on this day, after entering the chatroom.
     *
     * @return list of Message instances
     */
    public ArrayList<Message> getMessages() {
        return messages;
    }

    /**
     * This method sets list of names of all existing chatRooms.
     *
     * @param chatRoomNames ArrayList list of string of chatRoom names
     */
    public void setChatRoomNames(ArrayList chatRoomNames) {
        this.chatRoomNames = chatRoomNames;
    }

    /**
     * This method gets list of names of all existing chatRooms.
     *
     * @return list of string of chatRoom names
     */
    public ArrayList<String> getChatRoomNames() {
        return chatRoomNames;
    }

    /**
     * This method sets value of check variable of Client instance.
     *
     * @param check boolean
     */
    public void setCheck(boolean check) {
        this.check = check;
    }

    /**
     * This method gets value of check variable of Client instance.
     *
     * @return boolean
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * This method ends communication and connection with server.
     */
    public void closeConnection() {
        try {
            input.close();
        } catch (Exception e) {}
        try {
            output.close();
        } catch(Exception e) {}
        try{
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // logger message
        LOGGER.info("Client has ended!");
    }
}
