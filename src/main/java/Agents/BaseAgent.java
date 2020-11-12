package Agents;

import Maze.Maze;
import Maze.Position;
import Maze.MazeRunner;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.proto.ContractNetInitiator;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class BaseAgent extends Agent{

    MazeRunner mazeRunner;
    Position previousPosition;
    Position position;
    Set<Position> visited = new HashSet<Position>();
    Stack<Position> toVisit = new Stack<Position>();

    protected void setup() {
        Object[] args = getArguments();
        this.mazeRunner = (MazeRunner) args[0];
        this.position = this.mazeRunner.getPosition();
        this.previousPosition= new Position(-1 , -1);
        this.toVisit.push(this.position);

        registerAgentToDF();

        addBehaviour(new SearchingBehaviour(this));
        addBehaviour(new ListeningBehaviour(this));

        sendMessageToMaze("OLA");
//        sendMessageToAllAgents("OLA");
    }

    @Override
    protected void takeDown() {
        // retira registo no DF
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    public void startContract(){
        // Fill the CFP message
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

        // TODO get all agents names
        String[] names = {"selfish0", "reasonable0", "supportive0"};

        for (String name : names) {
            msg.addReceiver(new AID(name, AID.ISLOCALNAME));
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent("dummy-action");

        addBehaviour(new ContractInitiator(this, msg));
    }

    private void sendMessageToMaze(String message){
        // pesquisa DF por agentes "ping"
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Maze");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            // envia mensagem "pong" inicial a todos os agentes "ping"
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for (int i = 0; i < result.length; ++i)
                msg.addReceiver(result[i].getName());
            msg.setContent(message);
            send(msg);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToAllAgents(String message){
        // pesquisa DF por agentes "ping"
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Agente");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            // envia mensagem "pong" inicial a todos os agentes "ping"
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for (int i = 0; i < result.length; ++i)
                msg.addReceiver(result[i].getName());
            msg.setContent(message);
            send(msg);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void registerAgentToDF(){
        // regista agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    class SearchingBehaviour extends Behaviour {
        private BaseAgent baseAgent;
        private int n = 0;

        SearchingBehaviour(BaseAgent baseAgent){
            this.baseAgent = baseAgent;
        }

        public void action() {
            System.out.println(++n + " I am doing something!");
            System.out.println("Current pos: " + baseAgent.position.getX() + " " + baseAgent.position.getY());
            previousPosition = position;
            boolean[] possibleMoves = baseAgent.mazeRunner.getPossibleMovesFromPosition(baseAgent.position.getY(), baseAgent.position.getX());

            System.out.println(Arrays.toString(toVisit.toArray()));

            Position next = null;
            if (possibleMoves[Maze.SOUTH] && !visited.contains(new Position(position.getX(), position.getY() + 1))){
                next = new Position(position.getX(), position.getY() + 1);
            }
            else if (possibleMoves[Maze.EAST] && !visited.contains(new Position(position.getX() + 1, position.getY()))) {
                next = new Position(position.getX() + 1, position.getY());
            }
            else if (possibleMoves[Maze.NORTH] && !visited.contains(new Position(position.getX(), position.getY() - 1))) {
                next = new Position(position.getX(), position.getY() - 1);
            }
            else if (possibleMoves[Maze.WEST] && !visited.contains(new Position(position.getX() - 1, position.getY()))){
                next = new Position(position.getX() - 1, position.getY());
            }

            if (next == null) { // dead end
                next = toVisit.pop();
                System.out.println("DEAD END");
            } else {
                toVisit.push(next);
            }

            mazeRunner.updatePosition(position, next);
            position = next;
            System.out.println("Next pos: " + position.getX() + " " + position.getY());

//            sendMessageToAllAgents(getLocalName() + ": " + baseAgent.position.getX() + " " + baseAgent.position.getY());
            visited.add(position);

//            if (Math.random()*100 < 25){
//                this.baseAgent.startContract();
//            }
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        public boolean done() {
            return n > 20;
        }
    }

    class ListeningBehaviour extends Behaviour {
        private BaseAgent baseAgent;

        ListeningBehaviour(BaseAgent baseAgent){
            this.baseAgent = baseAgent;
        }

        public void action() {

            ACLMessage msg = receive();
            if(msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() +
                        ": recebi " + msg.getContent());
            }
        }

        public boolean done() {
            return false;
        }
    }

    class ContractInitiator extends ContractNetInitiator {

        public ContractInitiator(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if (responses.size() < 3) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing "+(3 - responses.size())+" responses");
            }
            // Evaluate proposals.
            int bestProposal = -1;
            AID bestProposer = null;
            ACLMessage accept = null;
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    int proposal = Integer.parseInt(msg.getContent());
                    if (proposal > bestProposal) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                }
            }
            // Accept the proposal of the best proposer
            if (accept != null) {
                System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
        }
    }

}