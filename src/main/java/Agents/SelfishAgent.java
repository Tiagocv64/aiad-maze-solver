package Agents;

import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;

public class SelfishAgent extends BaseAgent{

    public SelfishAgent(Color color) {
        super(color);
    }

    @Override
    public void setup(){
        super.setup();
        System.out.println("I'm selfish");

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );

    }

    // Selfish is not a contract responder because it never accepts a request

}

