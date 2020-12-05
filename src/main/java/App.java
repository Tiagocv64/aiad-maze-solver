import Agents.*;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.SimInit;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class App extends Repast3Launcher {
    private static final boolean BATCH_MODE = true;
    private int mazeSize;
    private int doorsNumber;
    private int selfishAgents;
    private int reasonableAgents;
    private int supportiveAgents;
    private ArrayList<BaseAgent> agentList;
    private ArrayList<ReasonableAgent> reasonableAgentList;
    private ArrayList<SupportiveAgent> supportiveAgentList;
    private ArrayList<SelfishAgent> selfishAgentList;
    private OpenSequenceGraph effortGraph;
    private OpenSequenceGraph percentageExploredGraph;
    private ContainerController container;


    public App() {
        super();
        this.mazeSize = 15;
        this.doorsNumber = 3;
        this.selfishAgents = 2;
        this.reasonableAgents = 2;
        this.supportiveAgents = 2;
        this.agentList = new ArrayList<>();
        this.reasonableAgentList = new ArrayList<>();
        this.supportiveAgentList = new ArrayList<>();
        this.selfishAgentList = new ArrayList<>();
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
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void launchAgents() throws StaleProxyException {
        MazeAgent mazeAgent = new MazeAgent(this.mazeSize, this.doorsNumber);
        container.acceptNewAgent("maze", mazeAgent).start();
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < this.selfishAgents; i++) {
            SelfishAgent selfishAgent = new SelfishAgent(Color.RED);
            container.acceptNewAgent("selfish" + i, selfishAgent).start();
            agentList.add(selfishAgent);
            selfishAgentList.add(selfishAgent);
        }

        for (int i = 0; i < this.reasonableAgents; i++) {
            ReasonableAgent reasonableAgent= new ReasonableAgent(Color.GREEN);
            container.acceptNewAgent("reasonable" + i, reasonableAgent).start();
            agentList.add(reasonableAgent);
            reasonableAgentList.add(reasonableAgent);
        }

        for (int i = 0; i < this.supportiveAgents; i++) {
            SupportiveAgent supportiveAgent = new SupportiveAgent(Color.BLUE);
            container.acceptNewAgent("supportive" + i, supportiveAgent).start();
            agentList.add(supportiveAgent);
            supportiveAgentList.add(supportiveAgent);
        }
    }


    @Override
    public void begin() {
        super.begin();
        buildPercentageExploredHistogram();
        buildEffortHistogram();
        buildSchedule();
    }

    private void buildPercentageExploredHistogram() {
        if (percentageExploredGraph != null) percentageExploredGraph.dispose();
        percentageExploredGraph = new OpenSequenceGraph("Percentage explored labyrinth", this);
        percentageExploredGraph.setAxisTitles("time", "explored %");

        percentageExploredGraph.addSequence("Explored percentage", new Sequence() {
            public double getSValue() {
                double explored = 0;
                double total = 0;

                BaseAgent baseAgent = agentList.get(0);

                if (baseAgent.agentMazeInfo == null)
                    return 0.0;

                for (int i = 0; i < baseAgent.agentMazeInfo.cellsInfo.length; i++){
                    for (int j = 0; j < baseAgent.agentMazeInfo.cellsInfo.length; j++){
                        if (baseAgent.agentMazeInfo.cellsInfo[i][j].getState() == AgentMazeInfo.CellInfo.EXPLORED){
                            explored++;
                        }
                        total++;
                    }
                }

                return explored / total * 100.0;
            }
        });

        percentageExploredGraph.display();
    }

    private void buildEffortHistogram() {
        if (effortGraph != null) effortGraph.dispose();
        effortGraph = new OpenSequenceGraph("Effort by Agent Type", this);
        effortGraph.setAxisTitles("time", "effort");

        effortGraph.addSequence("Reasonable Effort", new Sequence() {
            public double getSValue() {
                double effort = 0;
                for (int i = 0; i < reasonableAgentList.size(); i++) {
                    effort += reasonableAgentList.get(i).getEffort();
                }
                return effort;
            }
        }, Color.GREEN);

        effortGraph.addSequence("Supportive Effort", new Sequence() {
            public double getSValue() {
                double effort = 0;
                for (int i = 0; i < supportiveAgentList.size(); i++) {
                    effort += supportiveAgentList.get(i).getEffort();
                }
                return effort;
            }
        }, Color.BLUE);

        effortGraph.addSequence("Selfish Effort", new Sequence() {
            public double getSValue() {
                double effort = 0;
                for (int i = 0; i < selfishAgentList.size(); i++) {
                    effort += selfishAgentList.get(i).getEffort();
                }
                return effort;
            }
        }, Color.RED);

        effortGraph.display();
    }

    private void buildSchedule() {
        getSchedule().scheduleActionBeginning(0, new MainAction());
        getSchedule().scheduleActionAtInterval(1, effortGraph, "step", getSchedule().LAST);
        getSchedule().scheduleActionAtInterval(2, percentageExploredGraph, "step", getSchedule().LAST);
    }

    class MainAction extends BasicAction {

        public void execute() {
            for (int i = 0; i < agentList.size(); i++) {
                if (agentList.get(i).getMazeRunner() == null) {
                    return;
                }
            }
            for (int i = 0; i < agentList.size(); i++) {
                agentList.get(i).action();
            }
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

