package edu.weijunyong.satedgesim.TasksOrchestration;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.cloudbus.cloudsim.vms.Vm;

import edu.weijunyong.satedgesim.DataCentersManager.DataCenter;
import edu.weijunyong.satedgesim.ScenarioManager.simulationParameters;
import edu.weijunyong.satedgesim.SimulationManager.SimLog;
import edu.weijunyong.satedgesim.SimulationManager.SimulationManager;
import edu.weijunyong.satedgesim.TasksGenerator.Task;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class DefaultEdgeOrchestrator extends Orchestrator {

	public DefaultEdgeOrchestrator(SimulationManager simulationManager) {
		super(simulationManager);
	}
	
	public static int FindVmId_TP =0;
	public static int Counttask = 0;
	

	int numberOfTasks = 100; 
	protected int findVM(String[] architecture, Task task) {
		if ("ROUND_ROBIN".equals(algorithm)) {
			return roundRobin(architecture, task);
		} else if ("TRADE_OFF".equals(algorithm)) {
			return tradeOff(architecture, task);
		} else if ("TRADI_POLLING".equals(algorithm)) {
			return TradiPolling(architecture, task);
		} else if ("WEIGHT_GREEDY".equals(algorithm)) {
			return weightGreedy(architecture, task);
		} else if ("RANDOM_VM".equals(algorithm)) {
			return RandomVm(architecture, task);
		} else if ("ISOLINK".equals(algorithm)) {
			return ISOLINK( architecture, task);
			//return ISOLINK(architecture, numberOfTasks, task.getTaskLength(), task.getRequiredCore());
		} else {
			SimLog.println("");
			SimLog.println("Default Orchestrator- Unknnown orchestration algorithm '" + algorithm
					+ "', please check the simulation parameters file...");
			// Cancel the simulation
			Runtime.getRuntime().exit(0);
		}
		return -1;
	}
	// Counters for generated tasks
	private Map<String, Integer> taskTypeCounters = new HashMap<>();
	private Map<String, Integer> taskApplicationCounters = new HashMap<>();


	private int ISOLINK(String[] architecture, Task task) {
	    // Check if the task object is null
	    if (task == null) {
	        System.err.println("Error: Task object is null.");
	        return -1; // Return an invalid VM index
	    }

	    // Read application data from XML
	    List<Application> applications = readApplicationsFromXML();

	    // Define the type of tasks and link them to the nearest application.
	    String matchedApplication = classifyAndMatchApplication(task, applications);
	    String taskType = classifyTaskBasedOnProperties(task.getMaxDelay(), task.getContainerSize(), task.getRequiredCore(), task.getLength());
	    
	    task.setType(taskType); 
	    // === Update Counters ===
	    taskTypeCounters.put(taskType, taskTypeCounters.getOrDefault(taskType, 0) + 1);
	    taskApplicationCounters.put(matchedApplication, taskApplicationCounters.getOrDefault(matchedApplication, 0) + 1);


	    /*// Debug info for the task
	    System.out.println("Task Info Received for Offloading:");
	    System.out.println("Task ID: " + task.getId());
	    System.out.println("Length: " + task.getLength());
	    System.out.println("Task Type: " + task.getType());
	    System.out.println("---------------------------------------------");*/

	    // Initialize lists to store delays and number of tasks
	    List<Double> disdelay = new ArrayList<>();
	    List<Double> exedelay = new ArrayList<>();
	    List<Double> vmnum = new ArrayList<>();

	    // Determine weights based on updated task type
	    double w_delay, w_exec;
	    if (task.getType().equalsIgnoreCase("real-time")) {
	        w_delay = 0.7;
	        w_exec = 0.3;
	    } else if (task.getType().equalsIgnoreCase("latency-tolerant")) {
	        w_delay = 0.4;
	        w_exec = 0.6;
	    } else { // heavy or others
	        w_delay = 0.3;
	        w_exec = 0.7;
	    }

	    // Calculate delays and number of tasks for each VM
	    for (int i = 0; i < orchestrationHistory.size(); i++) {
	        try {
	            double disdelay_temp = SimulationManager.getdistance(
	                ((DataCenter) vmList.get(i).getHost().getDatacenter()), 
	                task.getEdgeDevice()) / simulationParameters.WAN_PROPAGATION_SPEED;
	            disdelay.add(disdelay_temp);

	            double exedelay_temp = task.getLength() / vmList.get(i).getMips();
	            exedelay.add(exedelay_temp);

	            vmnum.add((double) orchestrationHistory.get(i).size());
	        } catch (Exception e) {
	            System.err.println("Error calculating metrics for VM " + i + ": " + e.getMessage());
	            disdelay.add(Double.MAX_VALUE);
	            exedelay.add(Double.MAX_VALUE);
	            vmnum.add(Double.MAX_VALUE);
	        }
	    }

	    // Standardize the metrics
	    List<Double> disdelay_stand = standardization(disdelay);
	    List<Double> exedelay_stand = standardization(exedelay);

	    // Find the best VM for the task
	    int vm = -1;
	    double min = Double.MAX_VALUE;
	    double min_factor;

	    for (int i = 0; i < orchestrationHistory.size(); i++) {
	        if (offloadingIsPossible(task, vmList.get(i), architecture)) {
	            min_factor = w_delay * disdelay_stand.get(i) + w_exec * exedelay_stand.get(i);

	            if (min > min_factor) {
	                min = min_factor;
	                vm = i;
	            }
	        }
	    }

	    // Return the index of the selected VM
	    return vm;
	}

	
	private String classifyAndMatchApplication(Task task, List<Application> applications) {
	    if (task == null) {
	        System.err.println("Error: Task is null.");
	        return "unknown";
	    }

	    double taskMaxDelay = task.getMaxDelay();
	    double taskContainerSize = task.getContainerSize();
	    int taskRequiredCore = task.getRequiredCore();
	    long taskLength = task.getLength();

	    String matchedApplicationName = "No_Match";
	    double bestScore = Double.MAX_VALUE;

	    for (Application app : applications) {
	        double score = 0.0;
	        score += Math.abs(app.getMaxDelay() - taskMaxDelay);
	        score += Math.abs(app.getContainerSize() - taskContainerSize) / 1000.0;
	        score += Math.abs(app.getRequiredCore() - taskRequiredCore) * 10.0;
	        score += Math.abs(app.getTaskLength() - taskLength) / 10000.0;

	        if (score < bestScore) {
	            bestScore = score;
	            matchedApplicationName = app.getName();
	        }
	    }

	   // System.out.println("Best Matched Application for Task ID " + task.getId() + ": " + matchedApplicationName);

	   return matchedApplicationName;
	    //return classifyTaskBasedOnProperties(taskMaxDelay, taskContainerSize, taskRequiredCore, taskLength);
	}

	// Classify the task
	private String classifyTaskBasedOnProperties(double maxDelayValue, double containerSizeValue, int requiredCoreValue, double taskLength) {
	    final int SMALL_CONTAINER_THRESHOLD = 30000; // 30MB
	    final int MEDIUM_CONTAINER_THRESHOLD = 60000; // 60MB
	    final int SMALL_TASK_LENGTH = 100000; // 100K MI

	    if (maxDelayValue <= 10 && containerSizeValue <= SMALL_CONTAINER_THRESHOLD && requiredCoreValue < 2 && taskLength <= SMALL_TASK_LENGTH) {
	        return "real-time"; 
	    } else if (maxDelayValue <= 30 && containerSizeValue <= MEDIUM_CONTAINER_THRESHOLD) {
	        return "latency-tolerant"; 
	    } else {
	        return "heavy"; 
	    }
	}

	// Method to read applications from XML
	private List<Application> readApplicationsFromXML() {
	    List<Application> applications = new ArrayList<>();
	    try {
	        // read file XML
	        File xmlFile = new File("SatEdgeSim/settings/applications.xml");
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document = builder.parse(xmlFile);

	        
	        document.getDocumentElement().normalize();

	        // Read all the apps
	        NodeList applicationList = document.getElementsByTagName("application");

	        for (int i = 0; i < applicationList.getLength(); i++) {
	            Node node = applicationList.item(i);

	            if (node.getNodeType() == Node.ELEMENT_NODE) {
	                Element element = (Element) node;

	                String name = element.getAttribute("name");
	                double maxDelay = Double.parseDouble(element.getElementsByTagName("max_delay").item(0).getTextContent());
	                double containerSize = Double.parseDouble(element.getElementsByTagName("container_size").item(0).getTextContent());
	                double requestSize = Double.parseDouble(element.getElementsByTagName("request_size").item(0).getTextContent());
	                double resultsSize = Double.parseDouble(element.getElementsByTagName("results_size").item(0).getTextContent());
	                double taskLength = Double.parseDouble(element.getElementsByTagName("task_length").item(0).getTextContent());
	                int requiredCore = Integer.parseInt(element.getElementsByTagName("required_core").item(0).getTextContent());
	                double poissonInterarrival = Double.parseDouble(element.getElementsByTagName("poisson_interarrival").item(0).getTextContent());

	                //Classify the task
	                String taskType = classifyTaskBasedOnProperties(maxDelay, containerSize, requiredCore, taskLength);

	                
	                //System.out.println("Application Loaded: " + name + " (Type: " + taskType + ")");

	                // create Application
	                Application app = new Application(name, maxDelay, containerSize, requestSize,
	                        resultsSize, taskLength, requiredCore, poissonInterarrival, taskType);

	                applications.add(app);
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("Error reading applications from XML: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return applications;
	}
	public void printTaskStatistics(String fileSuffix) {
	    String basePath = "SatEdgeSim/output/";

	    
	    try {
	        FileWriter typeWriter = new FileWriter(basePath + "TaskType_" + fileSuffix + ".txt");
	        typeWriter.write("=== Task Type Distribution ===\n");
	        for (Map.Entry<String, Integer> entry : taskTypeCounters.entrySet()) {
	            typeWriter.write("Type: " + entry.getKey() + " | Count: " + entry.getValue() + "\n");
	        }
	        typeWriter.close();
	    } catch (IOException e) {
	        System.err.println("Error writing TaskType file:");
	        e.printStackTrace();
	    }

	    try {
	        FileWriter appWriter = new FileWriter(basePath + "Application_" + fileSuffix + ".txt");
	        appWriter.write("=== Application Distribution ===\n");
	        for (Map.Entry<String, Integer> entry : taskApplicationCounters.entrySet()) {
	            appWriter.write("Application: " + entry.getKey() + " | Count: " + entry.getValue() + "\n");
	        }
	        appWriter.close();
	    } catch (IOException e) {
	        System.err.println("Error writing Application file:");
	        e.printStackTrace();
	    }
	}

	
	//orchestrationHistory.size() = vmList.size()
	
	
	//Comprehensive weighted greedy algorithm
	//è¯„ä»·æŒ‡æ ‡ç±»åž‹ä¸€è‡´åŒ–ï¼Œæ— é‡�çº²åŒ–ï¼ŒåŠ¨æ€�åŠ æ�ƒï¼Œç»¼å�ˆè¯„ä»·
	private int weightGreedy(String[] architecture, Task task) {
		//èŽ·å�–æ ·æœ¬å€¼
		List<Double> disdelay = new ArrayList<>();	//ç¬¬ä¸€åˆ—
		List<Double> exedelay = new ArrayList<>();	//ç¬¬äºŒåˆ—
		List<Double> vmnum = new ArrayList<>();	//ç¬¬ä¸‰åˆ—
		List<Double> energylim = new ArrayList<>();	//ç¬¬å››åˆ—
		for (int i = 0; i < orchestrationHistory.size(); i++) {
			//ä¼ æ’­å»¶æ—¶
			double disdelay_tem = SimulationManager.getdistance(((DataCenter) vmList.get(i).getHost().getDatacenter())
					, task.getEdgeDevice())/simulationParameters.WAN_PROPAGATION_SPEED;
			disdelay.add(disdelay_tem);
			//å¤„ç�†å»¶æ—¶
			double exedelay_tem = task.getLength()/vmList.get(i).getMips();
			exedelay.add(exedelay_tem);
			//VMè¿�è¡Œçš„ä»»åŠ¡æ•°
			vmnum.add((double)orchestrationHistory.get(i).size());
			//vmçš„èƒ½è€—
			double energyuse =10*(Math.log10(((DataCenter) vmList.get(i).getHost().getDatacenter()).getEnergyModel().getTotalEnergyConsumption()));
			energylim.add(energyuse);	
		}
		//æ ‡å‡†åŒ–ï¼ˆå½’ä¸€åŒ–ï¼‰
		List<Double> disdelay_stand = new ArrayList<>();	//ç¬¬ä¸€åˆ—
		List<Double> exedelay_stand = new ArrayList<>();	//ç¬¬äºŒåˆ—
		List<Double> vmnum_stand = new ArrayList<>();	//ç¬¬ä¸‰åˆ—
		List<Double> energylim_stand = new ArrayList<>();	//ç¬¬å››åˆ—
		disdelay_stand = standardization(disdelay);
		exedelay_stand = standardization(exedelay);
		vmnum_stand = standardization(vmnum);
		energylim_stand = standardization(energylim);
		
		//åŠ æ�ƒç»¼å�ˆè¯„å®š
		int vm = -1;
		double min = -1;
		double min_factor;// vm with minimum assigned tasks;
		double a=0.3, b=0.3, c=0.25, d=0.15;
		// get best vm for this task
		for (int i = 0; i < orchestrationHistory.size(); i++) {
			if (offloadingIsPossible(task, vmList.get(i), architecture)) {
				
				min_factor = a*disdelay_stand.get(i) + b*exedelay_stand.get(i) + c*vmnum_stand.get(i) + d*energylim_stand.get(i);
				if (min == -1) { // if it is the first iteration
					min = min_factor;
					// if this is the first time, set the first vm as the
					vm = i; // best one
				} else if (min > min_factor) { // if this vm has more cpu mips and less waiting tasks
					// idle vm, no tasks are waiting
					min = min_factor;
					vm = i;
				}
			}
		}
		// assign the tasks to the found vm
		return vm;
	}
	
	public List<Double> standardization (List<Double> Pre_standar){	//æž�å€¼å·®æ³•æ ‡å‡†åŒ–
		List<Double> standard = new ArrayList<>();
		double premax = Collections.max(Pre_standar);
		double premin = Collections.min(Pre_standar);
		for(int k=0; k<Pre_standar.size(); k++) {
			double temp =(Pre_standar.get(k)-premin)/(premax-premin);
			standard.add(temp);
		}
		return standard;
	}
	
	
	
	
	private int tradeOff(String[] architecture, Task task) {
		int vm = -1;
		double min = -1;
		double new_min;// vm with minimum assigned tasks;

		// get best vm for this task
		for (int i = 0; i < orchestrationHistory.size(); i++) {
			if (offloadingIsPossible(task, vmList.get(i), architecture)) {
				double latency = 1;
				double energy = 1;
				if (((DataCenter) vmList.get(i).getHost().getDatacenter())
						.getType() == simulationParameters.TYPES.CLOUD) {
					latency = 1.6;
					energy = 1.1;
				} else if (((DataCenter) vmList.get(i).getHost().getDatacenter())
						.getType() == simulationParameters.TYPES.EDGE_DEVICE) {
					energy = 1.4;
				}
				new_min = (orchestrationHistory.get(i).size() + 1) * latency * energy * task.getLength() / vmList.get(i).getMips();
				if (min == -1) { // if it is the first iteration
					min = new_min;
					// if this is the first time, set the first vm as the
					vm = i; // best one
				} else if (min > new_min) { // if this vm has more cpu mips and less waiting tasks
					// idle vm, no tasks are waiting
					min = new_min;
					vm = i;
				}
			}
		}
		// assign the tasks to the found vm
		return vm;
	}
	
	
	//æ‰§è¡Œä»»åŠ¡æœ€å°‘çš„vm
	private int roundRobin(String[] architecture, Task task) {
		List<Vm> vmList = simulationManager.getServersManager().getVmList();
		int vm = -1;
		int minTasksCount = -1; // vm with minimum assigned tasks;
		// get best vm for this task
		for (int i = 0; i < orchestrationHistory.size(); i++) {
			if (offloadingIsPossible(task, vmList.get(i), architecture)) {
				if (minTasksCount == -1) {
					minTasksCount = orchestrationHistory.get(i).size();
					// if this is the first time, set the first vm as the best one
					vm = i;
				} else if (minTasksCount > orchestrationHistory.get(i).size()) {
					minTasksCount = orchestrationHistory.get(i).size();
					// new min found, so we choose it as the best VM
					vm = i;
					break;
				}
			}
		}
		// assign the tasks to the found vm
		return vm;
	}
	
	//è½®è¯¢ç®—æ³•è™šæ‹Ÿæœºç¼–å�·è½®è¯¢å�ªè€ƒè™‘èƒ½å�¦å»ºé“¾ä¸�è€ƒè™‘èµ„æº�æƒ…å†µ
	private int TradiPolling(String[] architecture, Task task) {
		List<Vm> vmList = simulationManager.getServersManager().getVmList();
		List<Task> tasksList = simulationManager.getTasksList();
		List<Integer>  minfindvmid = new ArrayList<>();		//è®°å½•å�¯ä»¥è°ƒåº¦çš„è™šæ‹Ÿæœº
		boolean flag = false;
		//å½“è½®è¯¢å�˜é‡�æ¯”è™šæ‹Ÿæœºç¼–å�·å¤§çš„æ—¶å€™ï¼Œå�–ä½™
		if(FindVmId_TP> vmList.size()-1) {
			FindVmId_TP = (FindVmId_TP+1) % vmList.size();
		}
		// get best vm for this task
		for (int i = 0; i < orchestrationHistory.size(); i++) {
			if (offloadingIsPossible(task, vmList.get(i), architecture)) {
				flag = true;
				minfindvmid.add(i); //æŠŠå�¯ä»¥è¿›è¡Œè°ƒåº¦çš„è™šæ‹Ÿæœºå…ˆè®°ä¸‹æ�¥
				if(FindVmId_TP <=i) {	//é�‡åˆ°ä¸ŽFindVmId_TPç›¸ç­‰æˆ–è€…å¤§çš„Idç»“æ�Ÿå¾ªçŽ¯
					FindVmId_TP = i;
					break;
				}
			} else{ 
				if((i == vmList.size()-1) && flag) {	//å½“FindVmId_TPè¿‡å¤§ä¸”æ¯”å®ƒå¤§çš„vmæ²¡æœ‰ç¬¦å�ˆçš„
					FindVmId_TP = minfindvmid.get(0);	//FindVmId_TPå�–ç¬¬ä¸€ä¸ªæ»¡è¶³çš„
					flag = false;
					
				}
			}
		}
		// assign the tasks to the found vm
		int vm = FindVmId_TP;
		FindVmId_TP++;
		Counttask++;
		//System.out.println("task "+task.getId() + "vm id is: " + vm+". taskcount: "+Counttask);
		if(Counttask == tasksList.size()) {
			FindVmId_TP=0;
			Counttask=0;
		}
		return vm;
	}
	
	
	//éš�æœºä¸€ä¸ªvm
	private int RandomVm(String[] architecture, Task task) {
		List<Vm> vmList = simulationManager.getServersManager().getVmList();
		int vm = -1;
		int RandomCount = 0; // random time;
		// get random vm for this task
		while(RandomCount<orchestrationHistory.size()) {
			double d = Math.random();
			int index = (int)(d*(orchestrationHistory.size()-1));
			if (offloadingIsPossible(task, vmList.get(index), architecture)) {
				vm = index;
				break;
			}
			RandomCount++;
		}
		
		//ä¸‡ä¸€æ²¡æœ‰éš�æœºå‡ºæ�¥
		if(RandomCount>=orchestrationHistory.size()) {
			for (int i = 0; i < orchestrationHistory.size(); i++) {
				if (offloadingIsPossible(task, vmList.get(i), architecture)) {
					vm =i;
					break;
				}
			}
			RandomCount = 0;
		}
		// assign the tasks to the found vm
		return vm;
	}
	
	

	@Override
	public void resultsReturned(Task task) { 
		
	}

}
