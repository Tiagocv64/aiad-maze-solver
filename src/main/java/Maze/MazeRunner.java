package Maze;

public class MazeRunner {
    Maze maze;
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


    public Position getPosition() {
        return position;
    }
}
