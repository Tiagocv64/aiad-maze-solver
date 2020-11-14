import java.awt.Color;
import java.util.Scanner;
import java.util.InputMismatchException;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class App {
    public static void main(String[] args) throws StaleProxyException {

        Scanner input = new Scanner(System.in);
        int size = rangeCheck("Maze size (in blocks): ", input, 15, 40);
        int doors = rangeCheck("Doors present: ", input, 0, 6);
        boolean enoughAgents = false;
        int selfishNumber = 0;
        int reasonableNumber = 0;
        int supportiveNumber = 0;
        while (!enoughAgents) {
            selfishNumber = rangeCheck("Number of selfish agents: ", input, 0, 10);
            reasonableNumber = rangeCheck("Number of reasonable agents: ", input, 0, 10);
            supportiveNumber = rangeCheck("Number of supportive agents: ", input, 0, 10);
            int totalAgents = selfishNumber + reasonableNumber + supportiveNumber;
            int doorOpeners = reasonableNumber + supportiveNumber;
            if (totalAgents > 20) {
                System.out.println("Total number of agents (" + totalAgents + ") needs to be 20 or less");
                continue;
            }
            if (doors > doorOpeners) {
                System.out.println("Total number of reasonable + supportive agents (" + doorOpeners + ") needs to be bigger than the amount of doors in the maze (" + doors + ")");
                continue;
            }
            if (doors >= totalAgents) {
                System.out.println("Total number of agents (" + totalAgents + ") needs to be bigger than the amount of doors in the maze (" + doors + ")");
                continue;
            }
            enoughAgents = true;
        }

        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        AgentContainer container = rt.createMainContainer(p);

        AgentController mazeAgent = container.createNewAgent("maze", "Agents.MazeAgent", new Object[] {size, doors});
        mazeAgent.start();

        AgentController[] selfishAgents = new AgentController[selfishNumber];
        for (int i = 0; i < selfishNumber; i++) {
            selfishAgents[i] = container.createNewAgent("selfish" + i, "Agents.SelfishAgent", new Object[] {Color.RED});
            selfishAgents[i].start();
        }
        AgentController[] reasonableAgents = new AgentController[reasonableNumber];
        for (int i = 0; i < reasonableNumber; i++) {
            reasonableAgents[i] = container.createNewAgent("reasonable" + i, "Agents.ReasonableAgent", new Object[] {Color.GREEN});
            reasonableAgents[i].start();
        }
        AgentController[] supportiveAgents = new AgentController[supportiveNumber];
        for (int i = 0; i < supportiveNumber; i++) {
            supportiveAgents[i] = container.createNewAgent("supportive" + i, "Agents.SupportiveAgent", new Object[] {Color.BLUE});
            supportiveAgents[i].start();
        }

    }
    private static int rangeCheck(String question, Scanner input, int min, int max) {
        boolean acceptedNumber = false;
        int number = min;
        while (!acceptedNumber) {
            System.out.println(question);
            try {
                number = input.nextInt();
            }
            catch (InputMismatchException exception) {
                System.out.println("Please input a number ranging from " + min + " to " + max);
                input.nextLine();
                continue;
            }
            if (number < min || number > max) {
                System.out.println("Please input a number ranging from " + min + " to " + max);
                continue;
            }
            acceptedNumber = true;
        }
        return number;
    }
}
