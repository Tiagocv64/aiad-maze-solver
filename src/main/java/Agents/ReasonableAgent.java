package Agents;

import Maze.Button;
import Maze.Position;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

import java.io.IOException;

public class ReasonableAgent extends BaseAgent{

    public void setup(){
        super.setup();
        System.out.println("I'm reasonable");


        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );

        addBehaviour(new ContractResponder(this, template));
    }

    class ContractResponder extends ContractNetResponder {

        public ContractResponder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            System.out.println("Received cfp " + getLocalName());

            if (!isHandlingRequest) {
                // Agent calculates distance between current position and button
                int distance = 0;
                try {
                    AgentMessage agentMessage = (AgentMessage) cfp.getContentObject();
                    buttonToFind = (Integer) ((Object[]) agentMessage.getContent())[0];
                    Button button = mazeRunner.maze.getButton(buttonToFind);
                    distance = mazeRunner.getPathBetween(position,  new Position(button.getCell() % mazeRunner.maze.N, button.getCell() / mazeRunner.maze.N)).size();
                    System.out.println("Agent "+getLocalName()+": Proposing "+distance + " to " + cfp.getSender().getLocalName());
                    ACLMessage propose = cfp.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                    propose.setContentObject(new AgentMessage(getAID(), AgentMessage.PROPOSE, distance));
                    return propose;
                } catch (UnreadableException | IOException e) {
                    e.printStackTrace();
                }

            }
            else {
                // We refuse to provide a proposal
                throw new RefuseException("evaluation-failed");
            }

            return null;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println("Received accept proposal");
            isHandlingRequest = true;
            if (true) { // do something
                // set goal to button position
                System.out.println("Agent: " + getLocalName() + ": Looking for button");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                try {
                    AgentMessage proposeMessage = (AgentMessage) propose.getContentObject();
                    effort += (Integer) proposeMessage.getContent();
                    inform.setContentObject(new AgentMessage(getAID(), AgentMessage.LOOKING_FOR_BUTTON, null));
                    Button button = mazeRunner.maze.getButton(buttonToFind);
                    pathToButton = mazeRunner.getPathBetween(position,  new Position(button.getCell() % mazeRunner.maze.N, button.getCell() / mazeRunner.maze.N));
                } catch (IOException | UnreadableException e) {
                    e.printStackTrace();
                }
                return inform;
            }
            else {
                System.out.println("Agent "+getLocalName()+": Action execution failed");
                throw new FailureException("unexpected-error");
            }
        }

        @Override
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent "+getLocalName()+": Proposal rejected");
        }
    }

}

