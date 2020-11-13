package Agents;

import Maze.Button;
import Maze.Position;

import java.util.HashMap;
import java.util.List;

public class AgentMazeInfo {

    int N;
    CellInfo[][] cellsInfo;
    private HashMap<Integer, Button> buttons = new HashMap<>();


    public AgentMazeInfo(int size){
        this.N = size;
        cellsInfo = new CellInfo[size][size];
        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                cellsInfo[i][j] = new CellInfo(CellInfo.NO_INFORMATION);
            }
        }
    }

    public void foundButton(Button button) {
        if (!buttons.containsKey(button.getNumber())) {
            buttons.put(button.getNumber(), button);
        }
    }

    public List<Integer> getPathToButton(Position position, Integer button) {

    }

    public class CellInfo {
        public static final int NO_INFORMATION = 0;
        public static final int EXPLORED = 1;

        int state;

        CellInfo(){
            this.state = NO_INFORMATION;
        }

        CellInfo(int state){
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }
}
