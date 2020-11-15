package Agents;

import jade.core.AID;

import java.io.Serializable;

public class AgentMessage implements Serializable {

    public static final String ASK_MAZE_INFO = "ASK_MAZE_INFO";
    public static final String ANSWER_MAZE_INFO = "ANSWER_MAZE_INFO";
    public static final String ASK_UPDATE_POS = "ASK_UPDATE_POS";
    public static final String REQUEST_OPEN_DOOR = "REQUEST_OPEN_DOOR";
    public static final String INFORM_AGENTS_OF_MOVE = "INFORM_AGENTS_OF_MOVE";
    public static final String INFORM_AGENTS_OF_BUTTON= "INFORM_AGENTS_OF_BUTTON";
    public static final String INFORM_DOOR_FOUND= "INFORM_DOOR_FOUND";
    public static final String INFORM_DOOR_OPEN = "INFORM_DOOR_OPEN";
    public static final String PROPOSE = "PROPOSE";
    public static final String LOOKING_FOR_BUTTON = "LOOKING_FOR_BUTTON";
    public static final String OPEN_DOOR = "OPEN_DOOR";
    public static final String EXIT_FOUND = "EXIT_FOUND";

    private final AID sender;  // Who sent the message
    private final String description;  // What is in the message
    private final Serializable content;  // Content of message

    AgentMessage(AID sender, String description, Serializable content){
        this.sender = sender;
        this.description = description;
        this.content = content;
    }

    public AID getSender() {
        return sender;
    }

    public String getDescription() {
        return description;
    }

    public Serializable getContent() {
        return content;
    }
}
