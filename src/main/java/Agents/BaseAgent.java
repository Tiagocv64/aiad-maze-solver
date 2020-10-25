package Agents;

import Maze.Maze;
import Maze.MazeRunner;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class BaseAgent extends Agent{

    MazeRunner mazeRunner;
    int currentY, currentX;

    protected void setup() {
        Object[] args = getArguments();
        this.mazeRunner = (MazeRunner) args[0];
        this.currentY = this.mazeRunner.getInitialY();
        this.currentX = this.mazeRunner.getInitialX();

        registerAgentToDF();

        addBehaviour(new SearchingBehaviour(this));
        addBehaviour(new ListeningBehaviour(this));

        sendMessageToAllAgents("OLA");
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
            System.out.println("Current pos: " + baseAgent.currentY + " " + baseAgent.currentX);

            boolean[] possibleMoves = baseAgent.mazeRunner.getPossibleMovesFromPosition(baseAgent.currentY, baseAgent.currentX);

            if (possibleMoves[Maze.NORTH]){
                System.out.println("Moving NORTH");
                currentY--;
            } else if (possibleMoves[Maze.WEST]){
                System.out.println("Moving WEST");
                currentX--;
            } else if (possibleMoves[Maze.SOUTH]){
                System.out.println("Moving SOUTH");
                currentY++;
            } else if (possibleMoves[Maze.EAST]){
                System.out.println("Moving EAST");
                currentX++;
            }

            sendMessageToAllAgents(getLocalName() + ": " + baseAgent.currentY + " " + baseAgent.currentX);
        }
        public boolean done() {
            return n > 5;
        }
    }

    class ListeningBehaviour extends Behaviour {
        private int n = 0;
        private BaseAgent baseAgent;

        ListeningBehaviour(BaseAgent baseAgent){
            this.baseAgent = baseAgent;
        }

        public void action() {

            n++;
            ACLMessage msg = receive();
            if(msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() +
                        ": recebi " + msg.getContent());
            }
        }

        public boolean done() {
            return n > 10;
        }
    }

}