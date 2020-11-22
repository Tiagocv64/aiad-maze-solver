import Agents.BaseAgent;
import Agents.ReasonableAgent;
import Agents.SelfishAgent;
import Agents.SupportiveAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.AgentController;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DTorus;

import java.awt.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;


public class App extends Repast3Launcher {
    private static final boolean BATCH_MODE = true;
    private int mazeSize;
    private int doorsNumber;
    private int selfishAgents;
    private int reasonableAgents;
    private int supportiveAgents;
    private ArrayList<BaseAgent> agentList;
    private DisplaySurface dsurf;
    private Object2DTorus space;
    private OpenSequenceGraph plot;
    private ContainerController container;


    public App() {
        super();
        this.mazeSize = 15;
        this.doorsNumber = 3;
        this.selfishAgents = 2;
        this.reasonableAgents = 2;
        this.supportiveAgents = 2;
        this.agentList = new ArrayList<>();
    }


    @Override
    public String getName() {
        return "aiad-maze-solver";
    }

    @Override
    public String[] getInitParam() {
        return new String[]{"mazeSize", "doorsNumber", "selfishAgents", "reasonableAgents", "supportiveAgents"};
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
    protected void launchJADE() {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        container = rt.createMainContainer(p);
        try {
            launchAgents();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void launchAgents() throws StaleProxyException {
        AgentController mazeAgent = container.createNewAgent("maze", "Agents.MazeAgent", new Object[] {this.getMazeSize(), this.getDoorsNumber()});
        mazeAgent.start();

        for (int i = 0; i < this.selfishAgents; i++) {
            System.out.println("selfish");
            SelfishAgent selfishAgent = new SelfishAgent(Color.RED);
            container.acceptNewAgent("selfish" + i, selfishAgent).start();
            agentList.add(selfishAgent);
        }

        for (int i = 0; i < this.reasonableAgents; i++) {
            ReasonableAgent reasonableAgent= new ReasonableAgent(Color.RED);
            container.acceptNewAgent("reasonable" + i, reasonableAgent).start();
            agentList.add(reasonableAgent);
        }

        for (int i = 0; i < this.supportiveAgents; i++) {
            SupportiveAgent supportiveAgent = new SupportiveAgent(Color.RED);
            container.acceptNewAgent("supportive" + i, supportiveAgent).start();
            agentList.add(supportiveAgent);
        }
    }


    @Override
    public void begin() {
        super.begin();
        buildDisplay();
        buildSchedule();
    }



    private void buildDisplay() {
        // space and display surface
        if (dsurf != null) dsurf.dispose();
        dsurf = new DisplaySurface(this, "Maze Display");
        registerDisplaySurface("Maze Display", dsurf);
        space = new Object2DTorus(mazeSize, mazeSize);

        Object2DDisplay display = new Object2DDisplay(space);
        // display.setObjectList(agentList);
        dsurf.addDisplayableProbeable(display, "Agents Space");
        dsurf.display();
    }

    private void buildSchedule() {
        getSchedule().scheduleActionBeginning(0, new MainAction());
        getSchedule().scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
        // schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
    }

    class MainAction extends BasicAction {

        public void execute() {
            // System.out.println("tick");
        }

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

