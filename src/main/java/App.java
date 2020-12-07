import Agents.*;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.SimInit;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.*;
import java.util.HashSet;


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
    private OpenSequenceGraph completionTimeGraph;
    private OpenSequenceGraph exploredCellsByAgentGraph;
    private ContainerController container;
    private MazeAgent mazeAgent;
    private FileWriter reasonableCSV;
    private FileWriter supportiveCSV;
    private FileWriter selfishCSV;
    private HashSet<Integer> finishedReasonable;
    private HashSet<Integer> finishedSupportive;
    private HashSet<Integer> finishedSelfish;

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
        this.openCSVFiles();

        System.out.println("app running");
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            flsuhCSVFiles();
            closeCSVFiles();
        }));
    }

    private void openCSVFiles() {
        File directory = new File("logs");
        if (! directory.exists()){
            directory.mkdir();
        }
        try {
            reasonableCSV = new FileWriter("logs" + File.separatorChar + "reasonable.csv", true);
            supportiveCSV = new FileWriter("logs" + File.separatorChar + "supportive.csv", true);
            selfishCSV = new FileWriter("logs" + File.separatorChar + "selfish.csv", true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeCSVFiles() {
        try {
            reasonableCSV.close();
            supportiveCSV.close();
            selfishCSV.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flsuhCSVFiles() {
        try {
            reasonableCSV.flush();
            supportiveCSV.flush();
            selfishCSV.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        flsuhCSVFiles();
        try {
            launchAgents();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void launchAgents() throws StaleProxyException {
        MazeAgent mazeAgent = new MazeAgent(this.mazeSize, this.doorsNumber);
        this.mazeAgent = mazeAgent;
        container.acceptNewAgent("maze", mazeAgent).start();

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

        finishedReasonable = new HashSet<>();
        finishedSupportive = new HashSet<>();;
        finishedSelfish = new HashSet<>();;
    }


    @Override
    public void begin() {
        super.begin();
        buildPercentageExploredGraph();
        buildExploredCellsByAgentGraph();
        buildEffortGraph();
        buildCompletionTimeGraph();
        buildSchedule();
    }

    private void buildExploredCellsByAgentGraph() {
        if (exploredCellsByAgentGraph != null) exploredCellsByAgentGraph.dispose();
        exploredCellsByAgentGraph = new OpenSequenceGraph("Number explored cells by agent", this);
        exploredCellsByAgentGraph.setAxisTitles("time", "nº cells");

        exploredCellsByAgentGraph.addSequence("Reasonable Cells", new Sequence() {
            public double getSValue() {
                double cellsExplored = 0;
                for (int i = 0; i < reasonableAgentList.size(); i++) {
                    cellsExplored += reasonableAgentList.get(i).visited.size();
                }
                return cellsExplored/reasonableAgentList.size();
            }
        }, Color.GREEN);

        exploredCellsByAgentGraph.addSequence("Supportive Cells", new Sequence() {
            public double getSValue() {
                double cellsExplored = 0;
                for (int i = 0; i < supportiveAgentList.size(); i++) {
                    cellsExplored += supportiveAgentList.get(i).visited.size();
                }
                return cellsExplored/supportiveAgentList.size();
            }
        }, Color.BLUE);

        exploredCellsByAgentGraph.addSequence("Selfish Cells", new Sequence() {
            public double getSValue() {
                double cellsExplored = 0;
                for (int i = 0; i < selfishAgentList.size(); i++) {
                    cellsExplored += selfishAgentList.get(i).visited.size();
                }
                return cellsExplored/selfishAgentList.size();
            }
        }, Color.RED);

        exploredCellsByAgentGraph.display();
    }

    private void buildPercentageExploredGraph() {
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

    private void buildEffortGraph() {
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

    private void buildCompletionTimeGraph() {
        if (completionTimeGraph != null) completionTimeGraph.dispose();
        completionTimeGraph = new OpenSequenceGraph("Completion Time by Agent Type", this);
        completionTimeGraph.setAxisTitles("time", "average time");

        completionTimeGraph.addSequence("Reasonable Average Time", new Sequence() {
            public double getSValue() {
                double totalTime = 0;
                int agentsCompleted = 0;
                for (int i = 0; i < reasonableAgentList.size(); i++) {
                    if (reasonableAgentList.get(i).hasFinished()) {
                        double completionTime = reasonableAgentList.get(i).getActionCounter();
                        totalTime += completionTime;
                        agentsCompleted++;
                        if (!finishedReasonable.contains(i)) {
                            try {
                                reasonableCSV.append("" + completionTime);
                                reasonableCSV.append(",");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            finishedReasonable.add(i);
                        }

                    }
                }
                if (totalTime != 0)
                    return totalTime / agentsCompleted;
                else
                    return 0;
            }
        }, Color.GREEN);

        completionTimeGraph.addSequence("Supportive Average Time", new Sequence() {
            public double getSValue() {
                double totalTime = 0;
                int agentsCompleted = 0;
                for (int i = 0; i < supportiveAgentList.size(); i++) {
                    if (supportiveAgentList.get(i).hasFinished()) {
                        double completionTime = supportiveAgentList.get(i).getActionCounter();
                        totalTime += completionTime;
                        agentsCompleted++;
                        if (!finishedSupportive.contains(i)) {
                            try {
                                supportiveCSV.append("" + completionTime);
                                supportiveCSV.append(",");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            finishedSupportive.add(i);
                        }
                    }
                }
                if (totalTime != 0)
                    return totalTime / agentsCompleted;
                else
                    return 0;
            }
        }, Color.BLUE);

        completionTimeGraph.addSequence("Selfish Average Time", new Sequence() {
            public double getSValue() {
                double totalTime = 0;
                int agentsCompleted = 0;
                for (int i = 0; i < selfishAgentList.size(); i++) {
                    if (selfishAgentList.get(i).hasFinished()) {
                        double completionTime = selfishAgentList.get(i).getActionCounter();
                        totalTime += completionTime;
                        agentsCompleted++;
                        if (!finishedSelfish.contains(i)) {
                            try {
                                selfishCSV.append("" + completionTime);
                                selfishCSV.append(",");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            finishedSelfish.add(i);
                        }
                    }
                }
                if (totalTime != 0)
                    return totalTime / agentsCompleted;
                else
                    return 0;
            }
        }, Color.RED);

        completionTimeGraph.display();
    }

    private void buildSchedule() {
        getSchedule().scheduleActionBeginning(0, new MainAction());
        getSchedule().scheduleActionAtInterval(1, effortGraph, "step", getSchedule().LAST);
        getSchedule().scheduleActionAtInterval(2, percentageExploredGraph, "step", getSchedule().LAST);
        getSchedule().scheduleActionAtInterval(2, completionTimeGraph, "step", getSchedule().LAST);
        getSchedule().scheduleActionAtInterval(2, exploredCellsByAgentGraph, "step", getSchedule().LAST);
    }

    class MainAction extends BasicAction {

        public void execute() {
            mazeAgent.getPanel().repaint();
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

