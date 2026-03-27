# Java Chat Application

A desktop chat application developed in Java as a client-server system. The project consists of two separate Java desktop applications: a server application and a client application.

The client connects to the server through socket-based communication. The server listens for incoming connections and handles each connected client in a separate thread. After connecting, the client receives the list of available chatrooms, logs in with a username, selects a chatroom, and can then exchange messages with other connected users.

The application supports both public and private messaging. Public messages are broadcast to all users in the same chatroom, while private messages are delivered only to the sender and the selected recipient. Messages are stored in a PostgreSQL database using Hibernate, and when a user joins a chatroom, previously stored messages are loaded from the server.

## Technologies

Java, Maven, PostgreSQL, Hibernate, JavaFX, JSON, SLF4J, JUnit, Mockito, PowerMock

## Key Concepts

- Client-server architecture
- Socket communication
- Multithreading
- Desktop GUI
- Database persistence

## Run Server

```bash
java --module-path /opt/javafx-sdk-14.0.1/lib --add-modules javafx.controls,javafx.fxml,javafx.media -jar ServerApp-1-jar-with-dependencies.jar
```
## Run Client

```bash
java --module-path /opt/javafx-sdk-14.0.1/lib --add-modules javafx.controls,javafx.fxml,javafx.media -jar ClientApp-1-jar-with-dependencies.jar
```

## Requirements

Please note that running the application requires JavaFX modules to be included, as shown in the commands above.
