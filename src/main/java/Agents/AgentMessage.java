package Agents;

import jade.core.AID;

import java.io.Serializable;

public class AgentMessage implements Serializable {

    public static final String ASK_MAZE_INFO = "ASK_MAZE_INFO";
    public static final String ANSWER_MAZE_INFO = "ANSWER_MAZE_INFO";
    public static final String ASK_UPDATE_POS = "ASK_UPDATE_POS";

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
