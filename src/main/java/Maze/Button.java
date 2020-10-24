package Maze;

public class Button {
    private Door door;
    private int cell;

    public Button(int cell) {
        this.cell = cell;
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
}
