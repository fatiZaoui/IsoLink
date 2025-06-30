package edu.weijunyong.satedgesim.TasksGenerator;

import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel; 
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;

import edu.weijunyong.satedgesim.ScenarioManager.simulationParameters;
import edu.weijunyong.satedgesim.ScenarioManager.simulationParameters.TYPES;
import edu.weijunyong.satedgesim.SimulationManager.SimulationManager;

public class DefaultTasksGenerator extends TasksGenerator {
	public DefaultTasksGenerator(SimulationManager simulationManager) {
		super(simulationManager);
	}

	public List<Task> generate() {
		// get simulation time in seconds (excluding the initialization time)
		double simulationTime = simulationParameters.SIMULATION_TIME - simulationParameters.INITIALIZATION_TIME; //in seconds
		for (int dev = 0; dev < datacentersList.size(); dev++) { // for each device
			if (datacentersList.get(dev).getType() == TYPES.EDGE_DEVICE && datacentersList.get(dev).isGeneratingTasks()) {
				int app = new Random().nextInt(simulationParameters.APPS_COUNT); // pickup a random application type for
				double lamda = 	simulationParameters.APPLICATIONS_TABLE[app][6];		//poisson_interarrival												// every device
				datacentersList.get(dev).setApplication(app); // assign this application to that device
				int time = 0;
				while (time < simulationTime) {
					// generating tasks
					time += getPossionVariable(lamda);
					// Shift the time by the defined value "INITIALIZATION_TIME"
					// in order to start after generating all the resources
					time += simulationParameters.INITIALIZATION_TIME;
					insert(time, app, dev);
				}
			}
		}
		return this.getTaskList();
	}
	
	//poisson 
    private static int getPossionVariable(double lamda) {
		int x = 0;
		double y = Math.random(), cdf = getPossionProbability(x, lamda);
		while (cdf < y) {
			x++;
			cdf += getPossionProbability(x, lamda);
		}
		return x;
	}
 
	private static double getPossionProbability(int k, double lamda) {
		double c = Math.exp(-lamda), sum = 1;
		for (int i = 1; i <= k; i++) {
			sum *= lamda / i;
		}
		return sum * c;
	}

	private void insert(int time, int app, int dev) {
		double maxLatency = (long) simulationParameters.APPLICATIONS_TABLE[app][0]; // Load length from application file
		long length = (long) simulationParameters.APPLICATIONS_TABLE[app][3]; // Load length from application file
		long requestSize = (long) simulationParameters.APPLICATIONS_TABLE[app][1];
		long outputSize = (long) simulationParameters.APPLICATIONS_TABLE[app][2];
		int pesNumber = (int) simulationParameters.APPLICATIONS_TABLE[app][4];
		long containerSize = (int) simulationParameters.APPLICATIONS_TABLE[app][5]; // the size of the container 
		Task[] task = new Task[simulationParameters.TASKS_PER_EDGE_DEVICE_PER_MINUTES];
		int id;
		
		// generate tasks for every edge device
		for (int i = 0; i < simulationParameters.TASKS_PER_EDGE_DEVICE_PER_MINUTES; i++) {
			id = taskList.size();
			UtilizationModel utilizationModel = new UtilizationModelFull();
			task[i] = new Task(id, length, pesNumber);
			task[i].setFileSize(requestSize).setOutputSize(outputSize).setUtilizationModel(utilizationModel);
			task[i].setTime(time);
			task[i].setContainerSize(containerSize);
			task[i].setMaxLatency(maxLatency);
			task[i].setEdgeDevice(datacentersList.get(dev)); // the device that generate this task (the origin)
			//task[i].setRegistry(datacentersList.get(selected)); //set the closed cloud as registry
			
		    
		    task[i].setInputFileSize(requestSize);   
		    task[i].setDeadline(maxLatency);        
			
			
		    double deadline = task[i].getDeadline();
		    long taskLength = task[i].getLength();
		    double inputSize = task[i].getInputFileSize();

		    double maxDelayValue = task[i].getMaxDelay();
		    double containerSizeValue = task[i].getContainerSize();
		    int requiredCoreValue = task[i].getRequiredCore();
		    long taskLengthValue = task[i].getLength();

		    // Thresholds التي تناسب ملف applications.xml
		    final int SMALL_CONTAINER_THRESHOLD = 30000; // 30MB
		    final int MEDIUM_CONTAINER_THRESHOLD = 60000; // 60MB
		    final int SMALL_TASK_LENGTH = 100000; // 100K MI

		    if (maxDelayValue <= 10 && containerSizeValue <= SMALL_CONTAINER_THRESHOLD && requiredCoreValue < 2 && taskLengthValue <= SMALL_TASK_LENGTH) {
		    	task[i].setType("real-time"); 
		    } else if (maxDelayValue <= 30 && containerSizeValue <= MEDIUM_CONTAINER_THRESHOLD) {
		    	task[i].setType("latency-tolerant");
		    } else {
		    	task[i].setType("heavy");
		    }

		    

		    
		    
			taskList.add(task[i]);
			getSimulationManager().getSimulationLogger()
					.deepLog("BasicTasksGenerator, Task " + id + " with execution time " + time + " (s) generated.");
		}
	}

}