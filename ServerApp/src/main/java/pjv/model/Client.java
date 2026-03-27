package pjv.model;
import javax.persistence.*;
import java.util.Objects;

/**
 * Client entity class.
 * This class is mapped by the Hibernate.
 * It contains all required information about the client.
 *
 * @author Oľga Ostashchuk and Michal Pechník
 * @version 1.0
 */
@Entity
@Table(name = "Client")
public class Client {

    // ID of client
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "clientId")
    private int clientId;

    // username of client
    @Column(name = "clientUserName", unique = true)
    private String clientUserName;

    // ID of chatRoom which client has chosen
    @Column(name = "clientChatRoomId")
    private int clientChatRoomId;


    /**
     * This method gets ID of Client instance.
     *
     * @return Client's ID
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * This method sets ID of Client instance.
     *
     * @param clientId int
     */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * This method gets username of Client instance.
     *
     * @return Client's username
     */
    public String getClientUserName() {
        return clientUserName;
    }

    /**
     * This method sets username of Client instance.
     *
     * @param clientUserName String
     */
    public void setClientUserName(String clientUserName) {
        this.clientUserName = clientUserName;
    }

    /**
     * This method gets ID of chatRoom of Client instance.
     *
     * @return Client's chatRoom ID
     */
    public int getClientChatRoomId() {
        return clientChatRoomId;
    }

    /**
     * This method sets ID of chatRoom of Client instance.
     *
     * @param clientChatRoomId int
     */
    public void setClientChatRoomId(int clientChatRoomId) {
        this.clientChatRoomId = clientChatRoomId;
    }

    /**
     * This method overrides equals() method of Object class
     * to indicate whether some other object is "equal to" this one.
     * Returns true if the arguments are deeply equal to each other and false otherwise.
     *
     * @param o Object
     * @return boolean value
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return clientId == client.clientId &&
                clientChatRoomId == client.clientChatRoomId &&
                Objects.equals(clientUserName, client.clientUserName);
    }
}
