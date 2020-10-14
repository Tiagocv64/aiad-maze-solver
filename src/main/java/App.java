import javax.swing.*;
import java.awt.*;
import java.io.*;
import Maze.*;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello boy");
        try
        {
            int size = 30;

            Maze maze = new Maze(size); // Constructs the maze object

            JFrame frame = new JFrame("Maze");
            MazePanel panel = new MazePanel(maze); // Constructs the panel to hold the
            // maze
            JScrollPane scrollPane = new JScrollPane(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(maze.windowSize());
            frame.pack();
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setVisible(true);
        }
        catch(NumberFormatException exception)
        {
            System.out.println("Input boyyyy") ;
        }

        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        AgentContainer container = rt.createMainContainer(p);

        try {
            AgentController acSelfish = container.createNewAgent("Tiago", "SelfishAgent", null);
            AgentController acSupportive = container.createNewAgent("Rafa", "SupportiveAgent", null);
            AgentController acReasonable = container.createNewAgent("Sousa", "ReasonableAgent", null);
            acSelfish.start();
            acSupportive.start();
            acReasonable.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
