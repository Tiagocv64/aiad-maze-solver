package Maze;

import Agents.AgentInfo;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Maze implements Serializable
{
    public static final int CELL_WIDTH = 20; // maze square size
    public static final int MARGIN = 0; // buffer between window edge and maze
    public static final int DOT_SIZE = 10; // size of maze solution dot
    public static final int DOT_MARGIN = 5; // space between wall and dot
    public static final int PATH_DOT_SIZE = 6; // size of maze solution dot
    public static final int PATH_DOT_MARGIN = 7; // space between wall and dot
    public int N; // size of maze
    public Cell[] cells; // array containing all the cells in the maze
    private boolean[] path; // array representing the unique path solution
    private List<Color> colors = new ArrayList<>();
    private ConcurrentHashMap<Integer, Door> doors = new ConcurrentHashMap<Integer, Door>();
    private ConcurrentHashMap<Integer, Button> buttons = new ConcurrentHashMap<Integer, Button>();
    private ConcurrentHashMap<Position, Set<AgentInfo>> agentPositions = new ConcurrentHashMap<Position, Set<AgentInfo>>();
    private int doorsNumber;
    private boolean found = false;
    private boolean exitFound = false;

    public Maze(int size, int doors)
    {
        N = size;
        doorsNumber = doors;
        cells = new Cell[N * N]; // creates array of Cells
        colors.add(Color.MAGENTA);
        colors.add(Color.ORANGE);
        colors.add(Color.PINK);
        colors.add(Color.GRAY);
        colors.add(Color.YELLOW);
        colors.add(Color.BLACK);

        for (int i = 0; i < N * N; i++) // initializes array with Cell objects
        {
            cells[i] = new Cell();
        }

        if(N > 0)
        {
            makeWalls(); // updates wall information inside each Cell object
            clearWalls(); // destroys wall until a maze is formed

            path = new boolean[N * N];
            createPath();

        }

        resetCellsVisitors();
    }

    public void resetCellsVisitors()  {
        for (int i = 0; i < N * N; i++) // initializes array with Cell objects
        {
            cells[i].visitedBy = -1;
        }
    }

    public Door hasDoor(Position position) {
        if (doors.containsKey(position.getX() + position.getY() * N)) {
            return doors.get(position.getX() + position.getY() * N);
        }
        else {
            return null;
        }
    }

    public Button hasButton(Position position) {
        if (buttons.containsKey(position.getX() + position.getY() * N)) {
            return buttons.get(position.getX() + position.getY() * N);
        }
        else {
            return null;
        }
    }

    public Button getButton(Integer number) {
        return buttons.get(number);
    }

    public void updatePosition(Position current, Position next, AgentInfo info) {
        if (agentPositions.containsKey(current)) {
            if (agentPositions.get(current).contains(info)) {
                agentPositions.get(current).remove(info);

                if (agentPositions.get(current).size() == 0) {
                    agentPositions.remove(current);
                }
            }
        }

        if (!agentPositions.containsKey(next)) {
            agentPositions.put(next, new HashSet<>());
        }

        agentPositions.get(next).add(info);

    }

    public void openDoor(int doorNumber) {
        for (Map.Entry<Integer, Door> set : doors.entrySet()) {
            Door door = set.getValue();
            if (door.getNumber() == doorNumber) {
                door.openDoor();
                set.setValue(door);
            }
        }
    }

    public void lightPath() {
        this.exitFound = true;
    }

    public static class Cell implements Serializable // Class representing a cell in a maze.
    {
        int[] walls; // array representing north, south, east, west walls
        int visitedBy; // for running first breath search, saves the cell that visited this cell
        boolean isPath;

        public Cell()
        {
            walls = new int[4];
            visitedBy = -1;
            isPath = false;
        }
    }

    public static final int NORTH = 0 ;
    public static final int SOUTH = 1 ;
    public static final int EAST = 2 ;
    public static final int WEST = 3 ;


    public void makeWalls() // fills wall information in Cells, -1 represents a
    // border wall
    {
        for (int i = 0; i < N * N; i++) // set north,south,east,west walls
        {
            cells[i].walls[NORTH] = i - N;
            cells[i].walls[SOUTH] = i + N;
            cells[i].walls[EAST] = i + 1;
            cells[i].walls[WEST] = i - 1;
        }

        for (int i = 0; i < N; i++)
        {
            cells[i].walls[NORTH] = -1; // set in border north cells, north wall to -1
            cells[N * N - i - 1].walls[SOUTH] = -1; // set in border south cells, south
            // wall to -1
        }
        for (int i = 0; i < N * N; i += N)
        {
            cells[N * N - i - 1].walls[EAST] = -1; // set in border east cells, east
            // wall to -1
            cells[i].walls[WEST] = -1; // set in border west cells, west wall to -1
        }
    }

    public void clearWalls() // destroys walls with a modified version of
    // Kruskal's algorithm
    {
        int NumElements = N * N;

        DisjSets ds = new DisjSets(NumElements); // creates a disjoint set to
        // represent cells
        for (int k = 0; k < N * N; k++)
        {
            ds.find(k); // adds each cell to a single set
        }

        Random generator = new Random();
        while (ds.allConnected() == false) // while not all the elements in the
        // set are connected
        {
            int cell1 = generator.nextInt(N * N); // pick a random cell
            int wall = generator.nextInt(4);

            int cell2 = cells[cell1].walls[wall]; // pick a second random cell

            if (cell2 != -1 && cell2 != N * N) // if there exists a wall between
            // these two cells
            {
                if (ds.find(cell1) != ds.find(cell2)) // if cells do not belong to
                // the same set
                {
                    cells[cell1].walls[wall] = N * N; // destroy the wall between
                    // these two cells. N*N will
                    // represent no wall

                    if (wall == NORTH || wall == EAST)
                    {
                        cells[cell2].walls[wall + 1] = N * N;
                    }
                    if (wall == SOUTH || wall == WEST)
                    {
                        cells[cell2].walls[wall - 1] = N * N;
                    }

                    ds.union(ds.find(cell1), ds.find(cell2)); // make a union of the
                    // set of these two cells, through which a path has just been
                    // created
                }
            }
        }
    }

    public void createPath() // finds a path in the maze
    {
        Random generator = new Random();
        if( N != 1) //if maze is not of size 1
        {
            depthSearch(0); // executes a first breath search starting on the top left cell

            path[0] = true; // path starts on top left cell
            path[N * N - 1] = true; // path ends on bottom right cell

            int current = cells[N * N - 1].visitedBy; // start on the last, bottom right cell
            int doorSeparation = -1;
            int buttonDistance = 30;
            boolean createButtonNext = false;
            if (doorsNumber > 0) {
                doorSeparation = N / doorsNumber;
            }
            int distanceBetweenDoors = 0;
            Door door = null;
            boolean lastDoor = false;
            int lastDoorCell = -1;
            while (current != 0) // follows the path back to the starting cell
            {
                cells[current].isPath = true;
                if (createButtonNext && !lastDoor) {
                    Button button = createButton(current, buttonDistance, doorsNumber, lastDoorCell, new ArrayList<>());
                    button.setDoor(door);
                    door.setButton(button);
                    buttons.put(button.getCell(), button);
                    createButtonNext = false;
                }
                if (doorsNumber > 0 && distanceBetweenDoors > doorSeparation && generator.nextInt(99) > (N * 2)) {
                    Color randomColor = colors.get(generator.nextInt(colors.size()));
                    colors.remove(randomColor);
                    door = new Door(randomColor, doorsNumber);
                    lastDoorCell = current;
                    doors.put(current, door);
                    createButtonNext = true;
                    doorsNumber--;
                    if (doorsNumber == 0)
                        lastDoor = true;
                    distanceBetweenDoors = 0;
                }
                path[current] = true;
                current = cells[current].visitedBy;
                distanceBetweenDoors++;
            }
            if (createButtonNext) {
                Button button = createButton(current, 4, doorsNumber, lastDoorCell, new ArrayList<>());
                button.setDoor(door);
                door.setButton(button);
                buttons.put(button.getCell(), button);
            }
        }
        else // if maze is of size 1
        {
            path[0] = true ;
        }

        //cells[0].walls[WEST] = N * N; // destroys west wall on top left cell
        cells[N * N - 1].walls[EAST] = N * N; // destroys east wall on bottom right
        // cell
    }

    public Button createButton(int cell, int distance, int number, int doorCell, List<Position> pathDoorToButton) {
        if (distance == 0) {
            if (cells[cell].isPath)
                return createButton(cell, 1, number, doorCell, pathDoorToButton);
            return new Button(cell, number, pathDoorToButton);
        }

        Cell startCell = cells[cell];
        Random generator = new Random();
        List<Integer> possibleDirections = new ArrayList<>();
        if (doorCell != (cell - N))
            possibleDirections.add(NORTH);
        if (doorCell != (cell + N))
            possibleDirections.add(SOUTH);
        if (doorCell != (cell + 1))
            possibleDirections.add(EAST);
        if (doorCell != (cell - 1))
            possibleDirections.add(WEST);
        int adjacent = -1;

        Integer direction = possibleDirections.get(generator.nextInt(possibleDirections.size()));
        while (startCell.walls[direction] != N * N) {
            possibleDirections.remove(direction);
            direction = possibleDirections.get(generator.nextInt(possibleDirections.size()));
        }

        if (direction == NORTH)
        {
            adjacent = cell - N;
        }
        if (direction == SOUTH)
        {
            adjacent = cell + N;
        }
        if (direction == EAST)
        {
            adjacent = cell + 1;
        }
        if (direction == WEST)
        {
            adjacent = cell - 1;
        }

        pathDoorToButton.add(new Position(adjacent % N, adjacent / N));

        return createButton(adjacent, distance - 1, number, doorCell, pathDoorToButton);
    }

    public void depthSearch(int cell) // executes a first breath search to find
    // a path in the maze
    {
        Cell startCell = cells[cell]; // current cell being checked

        for (int i = 0; i < 4; i++) // check if there is a path north, south,
        // east, or west
        {
            int adjacent = -1;

            if (startCell.walls[i] == N * N) // if there is no wall in north,
            // south, east or west direction
            {
                if (i == NORTH)
                {
                    adjacent = cell - N;
                }
                if (i == SOUTH)
                {
                    adjacent = cell + N;
                }
                if (i == EAST)
                {
                    adjacent = cell + 1;
                }
                if (i == WEST)
                {
                    adjacent = cell - 1;
                }

                if (cells[adjacent].visitedBy == -1)
                {
                    cells[adjacent].visitedBy = cell; // update information to
                    // store which cell has visited this one
                    depthSearch(adjacent);
                }
            }
        }
    }

    public Stack<Position> getPath(int cell, int goal) {
        resetCellsVisitors();
        Stack<Position> toVisit = new Stack<Position>();
        search(cell, goal);
        Cell current = cells[goal];
        toVisit.push(new Position(goal % N, goal / N));
        cells[cell].visitedBy = -1;
        found = false;
        while (current.visitedBy != -1) {
            toVisit.push(new Position(current.visitedBy % N, current.visitedBy / N));
            current = cells[current.visitedBy];
        }
        return toVisit;
    }

    private void search(int cell, int goal) // executes a first breath search to find
    // a path in the maze
    {
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(cell);
        cells[cell].visitedBy = 0;
        Cell current = null;

        while (queue.size() != 0) {
            cell = queue.poll();
            current = cells[cell];

            for (int i = 0; i < 4; i++) // check if there is a path north, south,
            // east, or west
            {
                int adjacent = -1;

                if (current.walls[i] == N * N) // if there is no wall in north,
                // south, east or west direction
                {
                    if (i == NORTH)
                    {
                        adjacent = cell - N;
                    }
                    if (i == EAST)
                    {
                        adjacent = cell + 1;
                    }
                    if (i == SOUTH)
                    {
                        adjacent = cell + N;
                    }
                    if (i == WEST)
                    {
                        adjacent = cell - 1;
                    }

                    if (adjacent >= (N * N) || adjacent < 0) // check if path goes out of bounds
                        continue;
                    if (cells[adjacent].visitedBy == -1 && !found) {
                        cells[adjacent].visitedBy = cell;
                        if (adjacent == goal && !found) {
                            found = true;
                            return;
                        }
                        queue.add(adjacent);
                    }
                }
            }

        }
    }

    public void draw(Graphics g) // draws a maze and its solution
    {
        g.setColor(Color.BLACK);

        for (int i = 0; i < N; i++)
        {
            int count = i;
            for (int j = 0; j < N; j++)
            {
                if (j != 0)
                {
                    count += N;
                }

                if (cells[count].walls[NORTH] != N * N) // if there exists a wall to the
                // north
                {
                    g.drawLine((i * CELL_WIDTH + MARGIN), (j * CELL_WIDTH + MARGIN),
                            ((i + 1) * CELL_WIDTH + MARGIN), (j * CELL_WIDTH + MARGIN));
                }

                if (cells[count].walls[SOUTH] != N * N) // if there exists a wall to the
                // south
                {
                    g.drawLine(i * CELL_WIDTH + MARGIN, (j + 1) * CELL_WIDTH
                            + MARGIN, (i + 1) * CELL_WIDTH + MARGIN, (j + 1) * CELL_WIDTH
                            + MARGIN);
                }

                if (cells[count].walls[EAST] != N * N) // if there exists a wall to the
                // east
                {
                    g.drawLine((i + 1) * CELL_WIDTH + MARGIN, j * CELL_WIDTH
                            + MARGIN, (i + 1) * CELL_WIDTH + MARGIN, (j + 1) * CELL_WIDTH
                            + MARGIN);
                }

                if (cells[count].walls[WEST] != N * N) // if there exists a wall to the
                // west
                {
                    g.drawLine(i * CELL_WIDTH + MARGIN, j * CELL_WIDTH + MARGIN, i
                            * CELL_WIDTH + MARGIN, (j + 1) * CELL_WIDTH + MARGIN);
                }
            }
        }

        g.setColor(Color.RED); // changes color to draw the dots
        for (int i = 0; i < N; i++)
        {
            int count = i;
            for (int j = 0; j < N; j++)
            {
                if (j != 0)
                {
                    count += N;
                }

                Position pos = new Position(i, j);
                if (agentPositions.containsKey(pos)) {
                    try {
                        g.setColor(agentPositions.get(pos).iterator().next().color);
                    } catch (NoSuchElementException | NullPointerException | ConcurrentModificationException e) {
                        g.setColor(Color.white);
                    }
                    g.fillRect(i * CELL_WIDTH + MARGIN + DOT_MARGIN, j * CELL_WIDTH
                            + MARGIN + DOT_MARGIN, DOT_SIZE, DOT_SIZE); // paint agent
                }

                if (path[count] && exitFound) { // if cell is part of the path
                    g.setColor(Color.BLACK);
                    g.fillOval(i * CELL_WIDTH + MARGIN + PATH_DOT_MARGIN, j * CELL_WIDTH + MARGIN + PATH_DOT_MARGIN, PATH_DOT_SIZE, PATH_DOT_SIZE); // paint path
                }

                if (doors.containsKey(count)) {
                    if (!doors.get(count).isOpen()) {
                        g.setColor(doors.get(count).getColor());
                        g.fillRect(i * CELL_WIDTH + 1, j * CELL_WIDTH + 1, CELL_WIDTH - 1, CELL_WIDTH - 1); // paint a door
                    }
                }

                if (buttons.containsKey(count)) {
                    g.setColor(buttons.get(count).getDoor().getColor());
                    g.fillOval(i * CELL_WIDTH + MARGIN + DOT_MARGIN, j * CELL_WIDTH
                            + MARGIN + DOT_MARGIN, DOT_SIZE, DOT_SIZE);
                }

                g.setColor(Color.RED);
            }
        }
    }

    public Dimension windowSize() // returns the ideal size of the window (for
    // JScrollPanes)
    {
        return new Dimension(N * CELL_WIDTH + MARGIN * 2 + 20, N * CELL_WIDTH + MARGIN
                * 2 + 40);
    }
}