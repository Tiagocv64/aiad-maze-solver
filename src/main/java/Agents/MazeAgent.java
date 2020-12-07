package Agents;

import Maze.Maze;
import Maze.MazePanel;
import Maze.MazeRunner;
import Maze.Position;
import sajas.core.Agent;
import sajas.core.behaviours.Behaviour;
import sajas.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MazeAgent extends Agent {

    Maze maze;
    MazeRunner mazeRunner;
    MazePanel panel;

    public MazeAgent(int mazeSize, int doorsNumber) {
        maze = new Maze(mazeSize, doorsNumber);
        mazeRunner = new MazeRunner(maze);
    }

    public MazePanel getPanel() {
        return panel;
    }


    @Override
    protected void setup() {

        registerMazeToDF();

        addBehaviour(new ListeningBehaviour(maze));

        drawMaze();
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

    private void drawMaze(){
        JFrame frame = new JFrame("Maze");
        panel = new MazePanel(maze); // Constructs the panel to hold the maze
        JScrollPane scrollPane = new JScrollPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(maze.windowSize());
        frame.pack();
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void registerMazeToDF(){
        // regista agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Maze");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    class ListeningBehaviour extends Behaviour {
        private Maze maze;

        ListeningBehaviour(Maze maze){
            this.maze = maze;
        }

        public void action() {

            ACLMessage msg = receive();
            while(msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                AgentMessage agentMessage = null;
                try {
                    agentMessage = (AgentMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

                switch (agentMessage.getDescription()){
                    case AgentMessage.ASK_MAZE_INFO:
                        System.out.println("someone asked maze info");
                        try {
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContentObject(new AgentMessage(getAID(), AgentMessage.ANSWER_MAZE_INFO,
                                        mazeRunner));
                            send(reply);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    case AgentMessage.ASK_UPDATE_POS:

                        Object[] contents = (Object[]) agentMessage.getContent();
                        Position current = (Position) contents[0];
                        Position next = (Position) contents[1];
                        AgentInfo info = (AgentInfo) contents[2];
                        mazeRunner.updatePosition(current, next, info);

                        break;
                    case AgentMessage.OPEN_DOOR:
                        Integer doorNumber = (Integer) agentMessage.getContent();
                        maze.openDoor(doorNumber);
                        break;
                    case AgentMessage.EXIT_FOUND:
                        maze.lightPath();
                        break;
                    default:
                }
                msg = receive();
            }
        }

        public boolean done() {
            return false;
        }
    }
}
