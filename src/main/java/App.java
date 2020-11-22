import java.awt.Color;
import java.util.*;

import Agents.BaseAgent;
import Agents.ReasonableAgent;
import Agents.SelfishAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.AgentController;
import sajas.wrapper.ContainerController;
import sajas.core.Runtime;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.*;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;


public class App extends SimModelImpl {
    private static final boolean BATCH_MODE = true;
    private int mazeSize;
    private int doorsNumber;
    private int selfishAgents;
    private int reasonableAgents;
    private int supportiveAgents;
    private ArrayList<BaseAgent> agentList;
    private Schedule schedule;
    private DisplaySurface dsurf;
    private Object2DTorus space;
    private OpenSequenceGraph plot;


    public App() {
        super();
        this.mazeSize = 15;
        this.doorsNumber = 3;
        this.selfishAgents = 2;
        this.reasonableAgents = 2;
        this.supportiveAgents = 2;
    }


    @Override
    public String getName() {
        return "aiad-maze-solver";
    }

    @Override
    public String[] getInitParam() {
        return new String[]{"mazeSize", "doorsNumber", "selfishAgents", "reasonableAgents", "supportiveAgents"};
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    public int getMazeSize() {
        return this.mazeSize;
    }

    public void setMazeSize(int mazeSize) {
        this.mazeSize = mazeSize;
    }

    public int getDoorsNumber() {
        return doorsNumber;
    }

    public void setDoorsNumber(int doorsNumber) {
        this.doorsNumber = doorsNumber;
    }

    public int getSelfishAgents() {
        return selfishAgents;
    }

    public void setSelfishAgents(int selfishAgents) {
        this.selfishAgents = selfishAgents;
    }

    public int getReasonableAgents() {
        return reasonableAgents;
    }

    public void setReasonableAgents(int reasonableAgents) {
        this.reasonableAgents = reasonableAgents;
    }

    public int getSupportiveAgents() {
        return supportiveAgents;
    }

    public void setSupportiveAgents(int supportiveAgents) {
        this.supportiveAgents = supportiveAgents;
    }

    @Override
    public void setup() {
        schedule = new Schedule();
        if (dsurf != null) dsurf.dispose();
        dsurf = new DisplaySurface(this, "Maze Display");
        registerDisplaySurface("Maze Display", dsurf);
    }

    @Override
    public void begin() {
        buildModel();
        buildDisplay();
        buildSchedule();
    }

    private void buildModel() {
        agentList = new ArrayList<BaseAgent>();
        space = new Object2DTorus(mazeSize, mazeSize);
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        ContainerController container = rt.createMainContainer(p);

        try {
            DisplaySurface displaySurf = new DisplaySurface(this, "Labyrinth Model");
            registerDisplaySurface("Labyrinth Model", displaySurf);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        for (int i = 0; i < this.selfishAgents; i++) {
            //SelfishAgent selfishAgent = new SelfishAgent(Color.RED);
            //container.acceptNewAgent("selfish" + i, selfishAgent).start();
        }

        for (int i = 0; i < this.reasonableAgents; i++) {
            //ReasonableAgent reasonableAgent= new ReasonableAgent(Color.RED);
            //container.acceptNewAgent("selfish" + i, ReasonableAgent).start();
        }

        for (int i = 0; i < this.supportiveAgents; i++) {
            //SelfishAgent selfishAgent = new SelfishAgent(Color.RED);
            //container.acceptNewAgent("selfish" + i, ReasonableAgent).start();
        }




    }

    private void buildDisplay() {
        // space and display surface
        Object2DDisplay display = new Object2DDisplay(space);
        display.setObjectList(agentList);
        dsurf.addDisplayableProbeable(display, "Agents Space");
        dsurf.display();
    }






    protected void launchJADE() {
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");
        ContainerController container = rt.createMainContainer(profile);
        try {
            DisplaySurface displaySurf = new DisplaySurface(this, "Labyrinth Model");
            registerDisplaySurface("Labyrinth Model", displaySurf);
            System.out.println("Launch Agents");
            launchAgents(container);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void buildSchedule() {
        schedule.scheduleActionBeginning(0, new MainAction());
        schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
        // schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
    }

    class MainAction extends BasicAction {

        public void execute() {
            System.out.println("tick");
        }

    }


    private void launchAgents(ContainerController container) throws StaleProxyException {
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

    public static void main(String[] args) {
        boolean runMode = !BATCH_MODE;

        // create a simulation
        SimInit init = new SimInit();

        // create a model
        App model = new App();

        // load model into simulation
        init.loadModel(model, null, runMode);
    }
}

