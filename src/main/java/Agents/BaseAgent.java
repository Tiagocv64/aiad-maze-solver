package Agents;

import Maze.Maze;
import Maze.MazeRunner;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class BaseAgent extends Agent{

    MazeRunner mazeRunner;
    int currentY, currentX;

    protected void setup() {
        Object[] args = getArguments();
        this.mazeRunner = (MazeRunner) args[0];
        this.currentY = this.mazeRunner.getInitialY();
        this.currentX = this.mazeRunner.getInitialX();
        addBehaviour(new SearchingBehaviour(this));
    }

    class SearchingBehaviour extends Behaviour {
        private BaseAgent baseAgent;
        private int n = 0;

        SearchingBehaviour(BaseAgent baseAgent){
            this.baseAgent = baseAgent;
        }

        public void action() {
            System.out.println(++n + " I am doing something!");
            System.out.println("Current pos: " + baseAgent.currentY + " " + baseAgent.currentX);

            boolean[] possibleMoves = baseAgent.mazeRunner.getPossibleMovesFromPosition(baseAgent.currentY, baseAgent.currentX);

            System.out.println(possibleMoves[0] + " " + possibleMoves[1] + " " + possibleMoves[2] + " " + possibleMoves[3]);
            if (possibleMoves[Maze.NORTH]){
                System.out.println("Moving NORTH");
                currentY--;
            } else if (possibleMoves[Maze.WEST]){
                System.out.println("Moving WEST");
                currentX--;
            } else if (possibleMoves[Maze.SOUTH]){
                System.out.println("Moving SOUTH");
                currentY++;
            } else if (possibleMoves[Maze.EAST]){
                System.out.println("Moving EAST");
                currentX++;
            }

        }
        public boolean done() { return n == 5;
        }
    }

}