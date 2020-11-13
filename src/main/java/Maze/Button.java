package Maze;

import java.io.Serializable;

public class Button implements Serializable {
    private Door door;
    private int cell;
    private int number;

    public Button(int cell, int number) {
        this.cell = cell;
        this.number = number;
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
