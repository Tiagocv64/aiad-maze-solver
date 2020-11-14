package Maze;

import java.io.Serializable;
import java.util.List;

public class Button implements Serializable {
    private Door door;
    private int cell;
    private int number;
    private List<Position> pathDoorToButton;

    public Button(int cell, int number, List<Position> pathDoorToButton) {
        this.cell = cell;
        this.number = number;
        this.pathDoorToButton = pathDoorToButton;
    }

    public Door getDoor() {
        return this.door;
    }

    public void setDoor(Door door) {
        this.door = door;
    }

    public int getCell() {
        return cell;
    }

    public int getNumber() {
        return number;
    }

}
