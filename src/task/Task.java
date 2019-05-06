package task;

import java.util.Comparator;

public class Task {

    private int number;
    private String task;
    private int type;

    public Task(int number, String task, int type) {
        this.number = number;
        this.task = task;
        this.type = type;
    }

    public Task() {
        this.number = 0;
        this.task = "";
        this.type = 0;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
