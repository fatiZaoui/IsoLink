package edu.weijunyong.satedgesim.TasksGenerator;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import edu.weijunyong.satedgesim.DataCentersManager.DataCenter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;public class Task extends CloudletSimple {
	private double offloadingTime;
	private String type; 
	
	private long inputFileSize;
	private double deadline;
	private String architecture;

	private double taskfinishTime;
	private double maxLatency;
	private DataCenter device;
	private long containerSize;
	private DataCenter orchestrator;
	private double receptionTime = -1; // the time when the task, or the corresponding container has been received by
										// the offloading destination
	private DataCenter registry;
	private int applicationID;
	private Status failureReason;
	private double maxDelay;
	//private double containerSize;
	private double requestSize;
	private double resultsSize;
	private double taskLength;
	private int requiredCore;
	private double poissonInterarrival;

	private String applicationName;
	

    // Getters and Setters
	public Task() {
	    super(0, 0, 0); // Call the parent constructor with default values
	}

	public static enum Status {
		FAILED_DUE_TO_LATENCY, FAILED_BECAUSE_DEVICE_DEAD, FAILED_DUE_TO_DEVICE_MOBILITY,
		NOT_GENERATED_BECAUSE_DEVICE_DEAD, FAILED_NO_RESSOURCES, NULL
	}



	public Task(int id, long cloudletLength, long pesNumber) {
		super(id, cloudletLength, pesNumber);
	}
	public String getType() {
	    return type;
	}

	public void setType(String type) {
	    this.type = type;
	}


	public void setTime(double time) {
		this.offloadingTime = time;
	}

	public double getTime() {
		return offloadingTime;
	}
	
	public void setTaskFinishTime(double time) {
		this.taskfinishTime = time;
	}

	public double getTaskFinishTime() {
		return taskfinishTime;
	}

	public double getMaxLatency() {
		return maxLatency;
	}

	public void setMaxLatency(double maxLatency) {
		this.maxLatency = maxLatency;
	}

	public DataCenter getEdgeDevice() {
		return device;
	}

	public void setEdgeDevice(DataCenter dev) {
		this.device = dev;
	}




	public void setOrchestrator(DataCenter orch) {
		this.orchestrator = orch;
	}

	public DataCenter getOrchestrator() {
		return orchestrator;
	}

	public double getReceptionTime() {
		return receptionTime;
	}

	public void setReceptionTime(double time) {
		receptionTime = time;
	}

	public DataCenter getRegistry() {
		return registry;
	}

	public void setRegistry(DataCenter registry) {
		this.registry = registry;
	}

	public int getApplicationID() {
		return applicationID;
	}

	public void setApplicationID(int applicationID) {
		this.applicationID = applicationID;
	}

	public Status getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(Status status) {
		this.setStatus(Cloudlet.Status.FAILED);
		this.failureReason = status;
	}
	public long getInputFileSize() {
	    return inputFileSize;
	}

	public void setInputFileSize(long inputFileSize) {
	    this.inputFileSize = inputFileSize;
	}

	public double getDeadline() {
	    return deadline;
	}

	public void setDeadline(double deadline) {
	    this.deadline = deadline;
	}

	public String getArchitecture() {
	    return architecture;
	}

	public void setArchitecture(String architecture) {
	    this.architecture = architecture;
	}

	public double getMaxDelay() { return maxDelay; }
	public void setMaxDelay(double maxDelay) { this.maxDelay = maxDelay; }

	public long getContainerSize() { return containerSize; }
    public void setContainerSize(long containerSize) { this.containerSize = containerSize; }

	public double getRequestSize() { return requestSize; }
	public void setRequestSize(double requestSize) { this.requestSize = requestSize; }

	public double getResultsSize() { return resultsSize; }
	public void setResultsSize(double resultsSize) { this.resultsSize = resultsSize; }

	public double getTaskLength() { return taskLength; }
	public void setTaskLength(double taskLength) { this.taskLength = taskLength; }

	public int getRequiredCore() { return requiredCore; }
	public void setRequiredCore(int requiredCore) { this.requiredCore = requiredCore; }

	public double getPoissonInterarrival() { return poissonInterarrival; }
	public void setPoissonInterarrival(double poissonInterarrival) { this.poissonInterarrival = poissonInterarrival; }

	
	public String getApplicationName() {
	    return applicationName;
	}

	public void setApplicationName(String applicationName) {
	    this.applicationName = applicationName;
	}
	
}

