package edu.weijunyong.satedgesim.TasksOrchestration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class Application {
    private String name;
    private double maxDelay;
    private double containerSize;
    private double requestSize;
    private double resultsSize;
    private double taskLength;
    private int requiredCore;
    private double poissonInterarrival;
    private String taskType; 

    // Constructor
    public Application(String name, double maxDelay, double containerSize, double requestSize,
                       double resultsSize, double taskLength, int requiredCore, double poissonInterarrival, String taskType) {
        this.name = name;
        this.maxDelay = maxDelay;
        this.containerSize = containerSize;
        this.requestSize = requestSize;
        this.resultsSize = resultsSize;
        this.taskLength = taskLength;
        this.requiredCore = requiredCore;
        this.poissonInterarrival = poissonInterarrival;
        this.taskType = taskType;
    }

    
    public String getName() {
        return name;
    }

    public double getMaxDelay() {
        return maxDelay;
    }

    public double getContainerSize() {
        return containerSize;
    }

    public double getRequestSize() {
        return requestSize;
    }

    public double getResultsSize() {
        return resultsSize;
    }

    public double getTaskLength() {
        return taskLength;
    }

    public int getRequiredCore() {
        return requiredCore;
    }

    public double getPoissonInterarrival() {
        return poissonInterarrival;
    }

    public String getTaskType() {
        return taskType;
    }
}
