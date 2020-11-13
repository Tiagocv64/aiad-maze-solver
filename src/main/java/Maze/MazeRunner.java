package Maze;


import Agents.AgentInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

public class MazeRunner implements Serializable {
    public Maze maze;
    private Position position;

    public MazeRunner(Maze maze){
        this.maze = maze;
        this.position = new Position(0, 0);
    }

    public boolean[] getPossibleMovesFromPosition(int y, int x){
        boolean[] possible = new boolean[4];
        for (int i = 0; i < 4; i++)
            possible[i] = getCellFromPosition(y, x).walls[i] == maze.N * maze.N;
        return possible;
    }

    private Maze.Cell getCellFromPosition(int y, int x){
        return maze.cells[y*maze.N+x];
    }

    public Stack<Position> getPathBetween(Position p1, Position p2) {

        return maze.getPath(p1.getX() + p1.getY()*maze.N, p2.getX() + p2.getY()*maze.N);
    }


    public Position getPosition() {
        return position;
    }

    public void updatePosition(Position current, Position next, AgentInfo info) {
        maze.updatePosition(current, next, info);
    }

    public Door hasDoor(Position position) {
        return maze.hasDoor(position);
    }

    public Button hasButton(Position position) {
        return maze.hasButton(position);
    }

}
