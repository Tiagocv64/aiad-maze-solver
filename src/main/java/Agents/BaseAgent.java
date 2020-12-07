package Agents;

import Maze.Button;
import Maze.*;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sajas.core.Agent;
import sajas.core.behaviours.Behaviour;
import sajas.domain.DFService;
import sajas.proto.ContractNetInitiator;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;


public abstract class BaseAgent extends Agent {

    MazeRunner mazeRunner = null;
    Position previousPosition;
    Position position;
    public Set<Position> visited = new HashSet<Position>();
    Stack<Position> toVisit = new Stack<Position>();
    Boolean isHandlingRequest = false;
    Boolean isWaiting = false;
    Integer waitingDoor = -1;
    Integer actionCounter = 0;
    Boolean standingOnButton = false;
    AgentInfo info;
    public AgentMazeInfo agentMazeInfo;
    Integer buttonToFind = -1; // initial goal is to find the end of the maze, but can also be to find a button
    Integer effort = 0;
    Stack<Position> pathToButton = null;
    ListeningBehaviour listeningBehaviour;
    Position next = null;
    Boolean finished = false;


    public BaseAgent(Color color) {
        this.info = new AgentInfo(color);
        this.position = new Position(0 , 0);
        this.previousPosition = new Position(-1 , -1);
        this.toVisit.push(this.position);
    }

    public Integer getEffort() {
        return effort;
    }

    public Integer getActionCounter() {
        return actionCounter;
    }

    public Boolean hasFinished() {
        return finished;
    }


    @Override
    protected void setup() {
        super.setup();
        registerAgentToDF();

        listeningBehaviour = new ListeningBehaviour(this);
        addBehaviour(listeningBehaviour);

        sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.ASK_MAZE_INFO, ""));


        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addBehaviour(new SearchingBehaviour(this));
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

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Agent");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (int i = 0; i < result.length; ++i) {
                if (!getLocalName().equals(result[i].getName().getLocalName())) {
                    msg.addReceiver(result[i].getName());
                }
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 6 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 4000));
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

        SearchingBehaviour(BaseAgent baseAgent) {
            this.baseAgent = baseAgent;
        }


        public void searchGoal() {
            n++;
            previousPosition = position;
            boolean[] possibleMoves = baseAgent.mazeRunner.getPossibleMovesFromPosition(baseAgent.position.getY(), baseAgent.position.getX());

            List<Integer> possibleMovesUnexplored = new ArrayList<Integer>();

            if (possibleMoves[Maze.SOUTH] &&
                    baseAgent.agentMazeInfo.getInfoCell(position.getX(), position.getY() + 1) == AgentMazeInfo.CellInfo.NO_INFORMATION)
                possibleMovesUnexplored.add(Maze.SOUTH);
            if (possibleMoves[Maze.EAST] &&
                    baseAgent.agentMazeInfo.getInfoCell(position.getX() + 1, position.getY()) == AgentMazeInfo.CellInfo.NO_INFORMATION)
                possibleMovesUnexplored.add(Maze.EAST);
            if (possibleMoves[Maze.NORTH] &&
                    baseAgent.agentMazeInfo.getInfoCell(position.getX(), position.getY() - 1) == AgentMazeInfo.CellInfo.NO_INFORMATION)
                possibleMovesUnexplored.add(Maze.NORTH);
            if (possibleMoves[Maze.WEST] &&
                    baseAgent.agentMazeInfo.getInfoCell(position.getX() - 1, position.getY()) == AgentMazeInfo.CellInfo.NO_INFORMATION)
                possibleMovesUnexplored.add(Maze.WEST);

            List<Integer> directions = new ArrayList<>();
            directions.add(Maze.SOUTH);
            directions.add(Maze.EAST);
            directions.add(Maze.NORTH);
            directions.add(Maze.WEST);
            Random generator = new Random();
            int randomDirection;
            int[] randomDirections = new int[4];
            for (int i = 0; i < 4; i++) {
                randomDirection = directions.get(generator.nextInt(directions.size()));
                randomDirections[i] = randomDirection;
                directions.remove((Integer) randomDirection);
            }

            for (int i = 0; i < 4; i++) {
                if (randomDirections[i] == Maze.SOUTH) {
                    if (possibleMoves[Maze.SOUTH] && !visited.contains(new Position(position.getX(), position.getY() + 1)) &&
                            (possibleMovesUnexplored.size() == 0 || possibleMovesUnexplored.contains(Maze.SOUTH))) {
                        next = new Position(position.getX(), position.getY() + 1);
                        break;
                    }
                } else if (randomDirections[i] == Maze.EAST) {
                    if (possibleMoves[Maze.EAST] && !visited.contains(new Position(position.getX() + 1, position.getY())) &&
                            (possibleMovesUnexplored.size() == 0 || possibleMovesUnexplored.contains(Maze.EAST))) {
                        next = new Position(position.getX() + 1, position.getY());
                        break;
                    }
                } else if (randomDirections[i] == Maze.NORTH) {
                    if (possibleMoves[Maze.NORTH] && !visited.contains(new Position(position.getX(), position.getY() - 1)) &&
                            (possibleMovesUnexplored.size() == 0 || possibleMovesUnexplored.contains(Maze.NORTH))) {
                        next = new Position(position.getX(), position.getY() - 1);
                        break;
                    }
                } else if (randomDirections[i] == Maze.WEST) {
                    if (possibleMoves[Maze.WEST] && !visited.contains(new Position(position.getX() - 1, position.getY())) &&
                            (possibleMovesUnexplored.size() == 0 || possibleMovesUnexplored.contains(Maze.WEST))) {
                        next = new Position(position.getX() - 1, position.getY());
                        break;
                    }
                }
            }

            if (next == null) { // dead end
                toVisit.pop();
                next = toVisit.pop();
            }

            toVisit.push(next);

            // verify if it is a door
            // verify this by message later
            Door door = baseAgent.mazeRunner.hasDoor(next);
            if (door != null && !agentMazeInfo.openDoors.contains(door.getNumber())) { // has door and its closed
                System.out.println("Agent " + getLocalName() + ": found door!");
                waitingDoor = door.getNumber();
                isWaiting = true;
                // starts FIPA contract with all agents
                // only if nobody started contract already (if nobody found the door first)
                if (!agentMazeInfo.foundDoors.contains(door.getNumber())) {
                    sendMessageToAllAgents(new AgentMessage(getAID(), AgentMessage.INFORM_DOOR_FOUND, door.getNumber()));
                    this.baseAgent.startContract(new AgentMessage(getAID(), AgentMessage.REQUEST_OPEN_DOOR, new Object[]{door.getButton().getCell()}));
                }

                return;
            }

            if (next.getX() >= agentMazeInfo.N || next.getY() >= agentMazeInfo.N) {
                System.out.println("Agent " + getLocalName() + ": finished maze!");
                sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.EXIT_FOUND, "exit"));
                finished = true;
                return;
            }

            mazeRunner.updatePosition(position, next, info);
            effort++;

            sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.ASK_UPDATE_POS, new Object[]{position, next, info}));
            sendMessageToAllAgents(new AgentMessage(getAID(), AgentMessage.INFORM_AGENTS_OF_MOVE, next));
            Button button = baseAgent.mazeRunner.hasButton(next);
            if (button != null) {
                sendMessageToAllAgents(new AgentMessage(getAID(), AgentMessage.INFORM_AGENTS_OF_BUTTON, button));
            }

            position = next;
            visited.add(position);
            next = null;
        }

        public void searchButton() {
            if (pathToButton.size() > 0) {
                Position next = pathToButton.pop();

                if (next != null) {
                    mazeRunner.updatePosition(position, next, info);
                    sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.ASK_UPDATE_POS, new Object[]{position, next, info}));
                    sendMessageToAllAgents(new AgentMessage(getAID(), AgentMessage.INFORM_AGENTS_OF_MOVE, next));
                    position = next;
                }

            } else {
                pathToButton = null;
                sendMessageToAllAgents(new AgentMessage(getAID(), AgentMessage.INFORM_DOOR_OPEN, baseAgent.mazeRunner.hasButton(position).getDoor().getNumber()));
                sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.OPEN_DOOR, baseAgent.mazeRunner.hasButton(position).getDoor().getNumber()));
                isHandlingRequest = false;
                buttonToFind = -1;
                standingOnButton = true;
            }
        }

        @Override
        public void action() {
            if (mazeRunner == null) {
                sendMessageToMaze(new AgentMessage(getAID(), AgentMessage.ASK_MAZE_INFO, ""));
                return;
            }

            actionCounter++;
            if (standingOnButton) { // waits for maze info
                return;
            }

            if (isHandlingRequest && buttonToFind != -1) {
                searchButton();
            } else if (isWaiting) {
                if (agentMazeInfo.openDoors.contains(waitingDoor)) {
                    System.out.println("Agent " + getLocalName() + ": door is now open!");
                    searchGoal();
                    isWaiting = false;
                }
            } else {
                searchGoal();
            }

           /* try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } */
        }

        @Override
        public boolean done() {
            return finished;
        }
    }


    class ListeningBehaviour extends Behaviour {
        private BaseAgent baseAgent;

        ListeningBehaviour(BaseAgent baseAgent){
            this.baseAgent = baseAgent;
        }

        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);

            while(msg != null) {

                AgentMessage agentMessage = null;

                try {
                    agentMessage = (AgentMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }


                switch (agentMessage.getDescription()) {
                    case AgentMessage.ANSWER_MAZE_INFO:
                        mazeRunner = (MazeRunner) agentMessage.getContent();
                        agentMazeInfo = new AgentMazeInfo(mazeRunner.maze.N);
                        baseAgent.agentMazeInfo.cellsInfo[0][0].setState(AgentMazeInfo.CellInfo.EXPLORED);
                        break;

                    case AgentMessage.INFORM_AGENTS_OF_MOVE:
                        Position senderPosition = (Position) agentMessage.getContent();
                        if (agentMazeInfo != null)
                            baseAgent.agentMazeInfo.cellsInfo[senderPosition.getX()][senderPosition.getY()].setState(AgentMazeInfo.CellInfo.EXPLORED);
                        break;

                    case AgentMessage.INFORM_AGENTS_OF_BUTTON:
                        Button button = (Button) agentMessage.getContent();
                        if (agentMazeInfo != null)
                            baseAgent.agentMazeInfo.foundButton(button);
                        break;

                    case AgentMessage.INFORM_DOOR_FOUND:
                        Integer foundDoor = (Integer) agentMessage.getContent();
                        if (agentMazeInfo != null)
                            agentMazeInfo.foundDoors.add(foundDoor);
                        break;

                    case AgentMessage.INFORM_DOOR_OPEN:
                        Integer openDoor = (Integer) agentMessage.getContent();
                        if (agentMazeInfo != null)
                            agentMazeInfo.openDoors.add(openDoor);
                        break;

                    case AgentMessage.LOOKING_FOR_BUTTON:
                        // do nothing (just wait)
                        break;
                    default:
                }

                msg = receive(mt);
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
                System.out.println("Agent " + getLocalName() + ": Timeout expired - 0 responses");
            }

            // Evaluate proposals.

            int bestProposal = 1000;
            AID bestProposer = null;
            ACLMessage accept = null;
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                System.out.println("Agent " + getLocalName() + ": Received proposal from: " + msg.getSender().getLocalName());
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    int proposal = 0;
                    try {
                        AgentMessage agentMessage = (AgentMessage) msg.getContentObject();
                        proposal = (Integer) agentMessage.getContent();
                    } catch (UnreadableException e1) {
                        e1.printStackTrace();
                    }
                    if (proposal < bestProposal) {
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
                System.out.println("Agent " + getLocalName() + ": Accepting proposal "+bestProposal+" from responder "+bestProposer.getLocalName());
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptances.addElement(accept);
            }

        }
    }

}