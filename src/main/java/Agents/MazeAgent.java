package Agents;

import Maze.Maze;
import Maze.MazePanel;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;

public class MazeAgent extends Agent {

    Maze maze;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        int mazeSize = (Integer) args[0];
        int mazeDoors = (Integer) args[1];
        maze = (Maze) args[2];

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
        MazePanel panel = new MazePanel(maze); // Constructs the panel to hold the maze
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
            if(msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() +
                        ": recebi " + msg.getContent());

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("RESPONSE FROM MAZE");
                send(reply);
            }
        }

        public boolean done() {
            return false;
        }
    }
}
