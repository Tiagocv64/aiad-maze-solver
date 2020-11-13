package Agents;

import Maze.Maze;
import Maze.MazeRunner;
import Maze.Position;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class BaseAgent extends Agent{

    MazeRunner mazeRunner = null;
    Position previousPosition;
    Position position;
    Set<Position> visited = new HashSet<Position>();
    Stack<Position> toVisit = new Stack<Position>();
    Boolean isHandlingRequest = false;
    Boolean isWaiting = false;
    AgentInfo info;
    AgentMazeInfo agentMazeInfo;
    Integer buttonToFind = -1; // initial goal is to find the end of the maze, but can also be to find a button

    protected void setup() {
        Object[] args = getArguments();
        this.position = new Position(0 , 0);
        this.previousPosition = new Position(-1 , -1);
        this.toVisit.push(this.position);
        this.info = new AgentInfo((Color) args[0]);

        registerAgentToDF();

        addBehaviour(new ListeningBehaviour(this));

        sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.ASK_MAZE_INFO, ""));

        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        addBehaviour(new SearchingBehaviour(this));

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

    public void startContract(AgentMessage message){
        // Fill the CFP message
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

        System.out.println("Starting Contract: " + getLocalName());

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Agent");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (int i = 0; i < result.length; ++i) {
                if (!getLocalName().equals(result[i].getName().getLocalName())) {
                    msg.addReceiver(result[i].getName());
                    System.out.println("Receiver: " + result[i].getName().getLocalName());
                }
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 10 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            msg.setContentObject(message);
        } catch (FIPAException | IOException e) {
            e.printStackTrace();
        }


        addBehaviour(new ContractInitiator(this, msg));
    }

    private void sendMessageToMaze(AgentMessage message){
        // pesquisa DF por agentes "ping"
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Maze");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for (int i = 0; i < result.length; ++i)
                msg.addReceiver(result[i].getName());
            msg.setContentObject(message);
            send(msg);
        } catch (FIPAException | IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToAllAgents(AgentMessage message){
        // pesquisa DF por agentes "ping"
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Agent");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for (int i = 0; i < result.length; ++i)
                msg.addReceiver(result[i].getName());
            msg.setContentObject(message);
            send(msg);
        } catch (FIPAException | IOException e) {
            e.printStackTrace();
        }
    }

    private void registerAgentToDF(){
        // regista agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agent");
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


        public void searchGoal() {
            n++;
            previousPosition = position;
            boolean[] possibleMoves = baseAgent.mazeRunner.getPossibleMovesFromPosition(baseAgent.position.getY(), baseAgent.position.getX());

            /* TODO use this later
            Position next = null;
            if (possibleMoves[Maze.SOUTH] && position.getY() + 1 < mazeRunner.maze.N &&
                    baseAgent.agentMazeInfo.cellsInfo[position.getX()][position.getY() + 1].getState() == AgentMazeInfo.CellInfo.NO_INFORMATION &&
                    !visited.contains(new Position(position.getX(), position.getY() + 1))){
                next = new Position(position.getX(), position.getY() + 1);
            }
            else if (possibleMoves[Maze.EAST] &&position.getX() + 1 < mazeRunner.maze.N &&
                    baseAgent.agentMazeInfo.cellsInfo[position.getX() + 1][position.getY()].getState() == AgentMazeInfo.CellInfo.NO_INFORMATION &&
                    !visited.contains(new Position(position.getX() + 1, position.getY()))) {
                next = new Position(position.getX() + 1, position.getY());
            }
            else if (possibleMoves[Maze.NORTH] && position.getY() - 1 >= 0 &&
                    baseAgent.agentMazeInfo.cellsInfo[position.getX()][position.getY() - 1].getState() == AgentMazeInfo.CellInfo.NO_INFORMATION &&
                    !visited.contains(new Position(position.getX(), position.getY() - 1))) {
                next = new Position(position.getX(), position.getY() - 1);
            }
            else if (possibleMoves[Maze.WEST] && position.getX() - 1 >= 0 &&
                    baseAgent.agentMazeInfo.cellsInfo[position.getX() - 1][position.getY()].getState() == AgentMazeInfo.CellInfo.NO_INFORMATION &&
                    !visited.contains(new Position(position.getX() - 1, position.getY()))){
                next = new Position(position.getX() - 1, position.getY());
            }
            */

            Position next = null;

            List<Integer> directions = new ArrayList<>();
            directions.add(Maze.SOUTH);
            directions.add(Maze.EAST);
            directions.add(Maze.NORTH);
            directions.add(Maze.WEST);
            Random generator = new Random();
            int randomDirection;
            int[] randomDirections = new int [4];
            for (int i = 0; i < 4; i++) {
                randomDirection = directions.get(generator.nextInt(directions.size()));
                randomDirections[i] = randomDirection;
                directions.remove((Integer)randomDirection);
            }

            for (int i = 0; i < 4; i++) {
                if (randomDirections[i] == Maze.SOUTH) {
                    if (possibleMoves[Maze.SOUTH] && !visited.contains(new Position(position.getX(), position.getY() + 1))) {
                        next = new Position(position.getX(), position.getY() + 1);
                        break;
                    }
                }
                else if (randomDirections[i] == Maze.EAST) {
                    if (possibleMoves[Maze.EAST] && !visited.contains(new Position(position.getX() + 1, position.getY()))) {
                        next = new Position(position.getX() + 1, position.getY());
                        break;
                    }
                }
                else if (randomDirections[i] == Maze.NORTH) {
                    if (possibleMoves[Maze.NORTH] && !visited.contains(new Position(position.getX(), position.getY() - 1))) {
                        next = new Position(position.getX(), position.getY() - 1);
                        break;
                    }
                }
                else if (randomDirections[i] == Maze.WEST) {
                    if (possibleMoves[Maze.WEST] && !visited.contains(new Position(position.getX() - 1, position.getY()))) {
                        next = new Position(position.getX() - 1, position.getY());
                        break;
                    }
                }
            }

            if (next == null) { // dead end
                toVisit.pop();
                next = toVisit.pop();
                System.out.println("DEAD END");
            }

            toVisit.push(next);

            // verify if it is a door
            // verify this by message later
            int doorNumber = baseAgent.mazeRunner.hasDoor(next);
            if (doorNumber != -1) { // has door
                System.out.println("found door!!");
                isWaiting = true;
                // starts FIPA contract with all agents
                this.baseAgent.startContract(new AgentMessage(getAID(), AgentMessage.REQUEST_OPEN_DOOR, new Object[] {doorNumber}));
                return;
            }

            mazeRunner.updatePosition(position, next, info);

            sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.ASK_UPDATE_POS, new Object[] {position, next, info}));
            sendMessageToAllAgents(new AgentMessage(getAID(), AgentMessage.INFORM_AGENTS_OF_MOVE, next));

            position = next;

            visited.add(position);

        }

        public void searchButton() {

        }

        public void action() {
            if (mazeRunner == null || isWaiting){ // waits for maze info
                return;
            }

            // if
            if (isHandlingRequest && buttonToFind != -1) {
                searchButton();
                return;
            } else {
                searchGoal();
            }


            try {
                TimeUnit.MILLISECONDS.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        public boolean done() {
            return n > 40;
        }
    }

    class ListeningBehaviour extends Behaviour {
        private BaseAgent baseAgent;

        ListeningBehaviour(BaseAgent baseAgent){
            this.baseAgent = baseAgent;
        }

        public void action() {

            ACLMessage msg = receive();
            // TODO use this later
            // while(msg != null && msg.getPerformative() == ACLMessage.INFORM) {
            if(msg != null && msg.getPerformative() == ACLMessage.INFORM) {

                AgentMessage agentMessage = null;
                try {
                    agentMessage = (AgentMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

                // System.out.println(agentMessage.getSender());
                // System.out.println(agentMessage.getDescription());
                // System.out.println(agentMessage.getContent());

                switch (agentMessage.getDescription()){
                    case AgentMessage.ANSWER_MAZE_INFO:

                        mazeRunner = (MazeRunner) agentMessage.getContent();
                        agentMazeInfo = new AgentMazeInfo(mazeRunner.maze.N);

                        break;
                    case AgentMessage.INFORM_AGENTS_OF_MOVE:

                        // System.out.println(agentMessage.getDescription());
                        Position senderPosition = (Position) agentMessage.getContent();
                        baseAgent.agentMazeInfo.cellsInfo[senderPosition.getX()][senderPosition.getY()].setState(AgentMazeInfo.CellInfo.EXPLORED);

                        break;
                    default:
                }
                // TODO use later
//                msg = receive();
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
            if (responses.size() < 1) {
                // Nobody replied r within the specified timeout
                System.out.println("Timeout expired: 0 responses");
            }

            // Evaluate proposals.

            System.out.println("Agent " + getLocalName() + ": Handling proposals");
            int bestProposal = -1;
            AID bestProposer = null;
            ACLMessage accept = null;
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                System.out.println("Agent " + getLocalName() + ": Proposal from: " + msg.getSender().getName());
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    int proposal = Integer.parseInt(msg.getContent());
                    if (proposal > bestProposal) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    } else if (proposal == bestProposal && msg.getSender().getName().compareTo(bestProposer.getName()) < 0) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                }
            }
            // Accept the proposal of the best proposer
            if (accept != null) {
                System.out.println("Agent " + getLocalName() + ": Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);


            }
        }
    }

}