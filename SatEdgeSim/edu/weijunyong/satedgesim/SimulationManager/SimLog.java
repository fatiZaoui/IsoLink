package edu.weijunyong.satedgesim.SimulationManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.cloudbus.cloudsim.vms.Vm;

import edu.weijunyong.satedgesim.MainApplication;
import edu.weijunyong.satedgesim.DataCentersManager.DataCenter;
import edu.weijunyong.satedgesim.Network.FileTransferProgress;
import edu.weijunyong.satedgesim.ScenarioManager.simulationParameters;
import edu.weijunyong.satedgesim.TasksGenerator.Task;

public class SimLog {
	public static final int NO_TIME = 0;
	public static final int SAME_LINE = 1;
	public static final int DEFAULT = 2;
	private List<String> resultsList = new ArrayList<String>();
	private DecimalFormat decimalFormat;
	private List<String> log = new ArrayList<String>();
	private String currentOrchArchitecture;
	private String currentOrchAlgorithm;
	private int currentEdgeDevicesCount;
	private String simStartTime;
	private SimulationManager simulationManager;
	private boolean isFirstIteration;

	// Tasks execution results
	private int generatedTasksCount = 0;
	private int tasksSent = 0;
	private int tasksFailed = 0;
	private int tasksFailedLatency = 0;
	private int tasksFailedMobility = 0;
	private int tasksFailedRessourcesUnavailable = 0;
	private int tasksFailedBeacauseDeviceDead = 0;
	private int notGeneratedBecDeviceDead = 0;
	private double totalExecutionTime = 0;
	private double totalWaitingTime = 0;
	private double totalEtedelayTime = 0;
	private int executedTasksCount = 0;
	private int EtedelayTasksCount = 0;
	private int tasksExecutedOnCloud = 0;
	private int tasksExecutedOnEdge = 0;
	private int tasksExecutedOnMist = 0;
	private int tasksFailedCloud = 0;
	private int tasksFailedEdge = 0;
	private int tasksFailedMist = 0;

	// Network utilization
	private double totalLanUsage = 0;
	private double totalWanUsage = 0;
	private double totalBandwidth = 0;
	private int transfersCount = 0;
	private double containersLanUsage = 0;
	private double containersWanUsage = 0;
	private double totalTraffic = 0;

	public SimLog(String time, boolean isFirstIteration) {
		this.setSimStartTime(time);
		this.isFirstIteration = isFirstIteration;

		// Use this format for all numbers
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
		otherSymbols.setDecimalSeparator('.'); // use the dot "." as separation symbole, since the comma ","
												// is used in csv files as a separator
		decimalFormat = new DecimalFormat("######.####", otherSymbols);

		if (isFirstIteration) {
			// Add the CSV file header
			resultsList.add("Orchestration architecture,Orchestration algorithm,Edge devices count,"
					+ "Tasks ETE delay (s),Average ETE delay (s),"
					+ "Tasks execution delay (s),Average execution delay (s),Tasks waiting time (s),"
					+ "Average wainting time (s),Generated tasks,Tasks successfully executed,Tasks success rate(%),"
					+ "Tasks failed rate(delay)(%),Tasks failed rate(device dead)(%),Tasks failed rate(mobility)(%),"
					+ "Task not executed (No resources available or long waiting time),Tasks failed (delay),Tasks failed (device dead),"
					+ "Tasks failed (mobility),Tasks not generated due to the death of devices,Total tasks executed (Cloud),"
					+ "Tasks successfully executed (Cloud),Total tasks executed (Edge),Tasks successfully executed (Edge),"
					+ "Total tasks executed (Mist),Tasks successfully executed (Mist),"
					+ "Network usage (s),Wan usage (s),Lan usage (s), Total network traffic (MBytes), Containers wan usage (s), Containers lan usage (s),Average bandwidth per task (Mbps),Average VM CPU usage (%),"
					+ "Average VM CPU usage (Cloud) (%),Average VM CPU usage (Edge) (%),Average VM CPU usage (Mist) (%),"
					+ "Energy consumption (dBW),Average energy consumption (dBW/Data center),Average energy consumption (dBW/Task),Cloud energy consumption (dBW),"
					+ "Average Cloud energy consumption (dBW/Data center),Edge energy consumption (dBW),Average Edge energy consumption (dBW/Data center),"
					+ "Mist energy consumption (dBW),Average Mist energy consumption (dBW/Device),"
					+ "Energy consumption (W),Average energy consumption (W/Data center),Average energy consumption (W/Task),Cloud energy consumption (W),"
					+ "Average Cloud energy consumption (W/Data center),Edge energy consumption (W),Average Edge energy consumption (W/Data center),"
					+ "Mist energy consumption (W),Average Mist energy consumption (W/Device),Dead devices count,"
					+ "Average remaining power (Wh),Average remaining power (%), First edge device death time (s),"
					+ "List of remaining power (%) (only battery powered devices / 0 = dead),List of the time when each device died (s)");
		}
	}

	public void showIterationResults(List<Task> finishedTasks) {
		printTasksRelatedResults();
		printNetworkRelatedResults();
		printResourcesUtilizationResults(finishedTasks);
		StringBuilder s = new StringBuilder("\n");
		for (String value : log) {
			s.append(value).append("\n");
		}
		System.out.print(s);
		// update the log
		saveLog();
	}

	public void printTasksRelatedResults() {
		print("");
		print("------------------------------------------------------- OUTPUT -------------------------------------------------------");
		print("");
		print("SimLog- Tasks not sent because device died (low energy)                         : "
				+ padLeftSpaces(decimalFormat.format(notGeneratedBecDeviceDead / generatedTasksCount), 15) + " % ("
				+ notGeneratedBecDeviceDead + " tasks)");
		print("SimLog- Tasks sent from edge devices                                            : "
				+ padLeftSpaces("" + decimalFormat.format(((double) tasksSent * 100) / ((double) generatedTasksCount)),
						15)
				+ " % (" + tasksSent + " among " + generatedTasksCount + " generated tasks)");

		print("-------------------------------------All values below are based on the sent tasks-------------------------------------");
		print("SimLog- Tasks ETE delay                                                         :"
				+ padLeftSpaces(decimalFormat.format(totalEtedelayTime), 15) + " seconds");
		print("SimLog- Average tasks ETE delay                                                 :"
				+ padLeftSpaces(decimalFormat.format(totalEtedelayTime / EtedelayTasksCount), 15) + " seconds");
		print("SimLog- Tasks execution delay                                                   :"
				+ padLeftSpaces(decimalFormat.format(totalExecutionTime), 15) + " seconds");
		print("SimLog- Average tasks execution delay                                           :"
				+ padLeftSpaces(decimalFormat.format(totalExecutionTime / executedTasksCount), 15) + " seconds");
		print("SimLog- Tasks waiting time (from task submiting to the execution start)         :"
				+ padLeftSpaces(decimalFormat.format(totalWaitingTime), 15) + " seconds");
		print("SimLog- Average tasks waiting time (from task submiting to the execution start) :"
				+ padLeftSpaces(decimalFormat.format(totalWaitingTime / executedTasksCount), 15) + " seconds");
		print("SimLog- Tasks successfully executed                                             :"
				+ padLeftSpaces("" + decimalFormat.format((double) (tasksSent - tasksFailed) * 100 / tasksSent), 15)
				+ " % (" + (tasksSent - tasksFailed) + " among " + tasksSent + " sent tasks)");

		print("SimLog- Tasks failures");
		print("                                       Not executed due to resources unavailable:"
				+ padLeftSpaces(decimalFormat.format((double) tasksFailedRessourcesUnavailable * 100 / tasksSent), 15)
				+ " % (" + tasksFailedRessourcesUnavailable + " tasks)");
		print("                              Successfully executed but failed due to high delay:"
				+ padLeftSpaces(decimalFormat.format((double) tasksFailedLatency * 100 / tasksSent), 15) + " % ("
				+ tasksFailedLatency + " tasks from " + tasksSent + " successfully sent tasks)");
		print("                 Tasks execution results not returned because the device is dead:"
				+ padLeftSpaces(decimalFormat.format((double) tasksFailedBeacauseDeviceDead * 100 / tasksSent), 15)
				+ " % (" + tasksFailedBeacauseDeviceDead + " tasks)");
		print("                     Tasks execution results not returned due to device mobility:"
				+ padLeftSpaces(decimalFormat.format((double) tasksFailedMobility * 100 / tasksSent), 15) + " % ("
				+ tasksFailedMobility + " tasks)");

		print("SimLog- Tasks executed on each level                                :" + " Cloud="
				+ padLeftSpaces("" + tasksExecutedOnCloud, 15) + " tasks (where "
				+ (tasksExecutedOnCloud - tasksFailedCloud) + " were successfully executed)");
		print("                                                                      " + " Edge="
				+ padLeftSpaces("" + tasksExecutedOnEdge, 15) + " tasks (where " + (tasksExecutedOnEdge - tasksFailedEdge)
				+ " were successfully executed)");
		print("                                                                      " + " Mist="
				+ padLeftSpaces("" + tasksExecutedOnMist, 15) + " tasks (where "
				+ (tasksExecutedOnMist - tasksFailedMist) + " were successfully executed)");

		resultsList.add(currentOrchArchitecture + "," + currentOrchAlgorithm + "," + currentEdgeDevicesCount + ","
				+ decimalFormat.format(totalEtedelayTime) + ","
				+ decimalFormat.format(totalEtedelayTime / EtedelayTasksCount) + ","
				+ decimalFormat.format(totalExecutionTime) + ","
				+ decimalFormat.format(totalExecutionTime / executedTasksCount) + ","
				+ decimalFormat.format(totalWaitingTime) + ","
				+ decimalFormat.format(totalWaitingTime / executedTasksCount) + "," + generatedTasksCount + ","
				+ (tasksSent - tasksFailed) + "," + decimalFormat.format((double) (tasksSent - tasksFailed) * 100 / tasksSent) + ","
				+ decimalFormat.format((double) tasksFailedLatency * 100 / tasksSent)+ "," 
				+ decimalFormat.format((double) tasksFailedBeacauseDeviceDead * 100 / tasksSent)+ "," 
				+ decimalFormat.format((double) tasksFailedMobility * 100 / tasksSent)+ "," 
				+ tasksFailed + "," + tasksFailedLatency + ","+ tasksFailedBeacauseDeviceDead + ","
				+ tasksFailedMobility + "," + notGeneratedBecDeviceDead + ","+ tasksExecutedOnCloud + ","
				+ (tasksExecutedOnCloud - tasksFailedCloud) + "," + tasksExecutedOnEdge
				+ "," + (tasksExecutedOnEdge - tasksFailedEdge) + "," + tasksExecutedOnMist + ","
				+ (tasksExecutedOnMist - tasksFailedMist) + ",");
	}

	public void printNetworkRelatedResults() {
		print("SimLog- Network usage                                                           :"
				+ padLeftSpaces(decimalFormat.format(totalLanUsage), 15) + " seconds (The total traffic: "
				+ decimalFormat.format(totalTraffic) + " (MBytes))");
		print("                                                                       " + " Wan="
				+ padLeftSpaces(decimalFormat.format(totalWanUsage), 15) + " seconds ("
				+ decimalFormat.format(totalWanUsage * 100 / totalLanUsage)
				+ " % of total usage, WAN used when downloading containers="
				+ decimalFormat.format(containersWanUsage * 100 / totalWanUsage) + " % of WAN usage)");
		print("                                                                       " + " Lan="
				+ padLeftSpaces(decimalFormat.format(totalLanUsage), 15) + " seconds ("
				+ decimalFormat.format(totalLanUsage * 100 / totalLanUsage)
				+ " % of total usage, LAN used when downloading containers="
				+ decimalFormat.format(containersLanUsage * 100 / totalLanUsage) + " % of LAN usage)");
		print("                                                   Average bandwidth per transfer="
				+ padLeftSpaces(decimalFormat.format(totalBandwidth / transfersCount), 15) + " Mbps  ");
		// Add these values to the las item of the results list
		resultsList.set(resultsList.size() - 1,
				resultsList.get(resultsList.size() - 1) + totalLanUsage + "," + totalWanUsage + "," + totalLanUsage
						+ "," + totalTraffic + "," + containersWanUsage + "," + containersLanUsage + ","
						+ (totalBandwidth / transfersCount) + ",");
	}

	public void printResourcesUtilizationResults(List<Task> finishedTasks) {
		int edgeDevicesCount = 0;
		double averageCpuUtilization = 0;
		double averageCloudCpuUtilization = 0;
		double averageMistCpuUtilization = 0;
		double averageEdgeCpuUtilization = 0;
		int deadEdgeDevicesCount = 0;
		double energyConsumption = 0;
		double cloudEnConsumption = 0;
		double mistEnConsumption = 0;
		double edgeEnConsumption = 0;
		double averageRemainingPowerWh = 0;
		double averageRemainingPower = 0;
		List<Double> remainingPower = new ArrayList<>();
		double firstDeviceDeathTime = -1; // -1 means invalid
		List<Double> devicesDeathTime = new ArrayList<>();
		int batteryPoweredDevicesCount = 0;
		int aliveBatteryPoweredDevicesCount = 0;
		List<? extends DataCenter> datacentersList = simulationManager.getServersManager().getDatacenterList();

		for (DataCenter dc : datacentersList) {
			if (dc.getType() == simulationParameters.TYPES.CLOUD) {
				cloudEnConsumption += dc.getEnergyModel().getTotalEnergyConsumption();
				averageCloudCpuUtilization += dc.getTotalCpuUtilization();
			} else if (dc.getType() == simulationParameters.TYPES.EDGE_DATACENTER) {
				edgeEnConsumption += dc.getEnergyModel().getTotalEnergyConsumption();
				averageEdgeCpuUtilization += dc.getTotalCpuUtilization();
			} else if (dc.getType() == simulationParameters.TYPES.EDGE_DEVICE) {
				mistEnConsumption += dc.getEnergyModel().getTotalEnergyConsumption();
				if (dc.getVmList().size() > 0) {
					// only devices with computing capability
					// the devices that have no VM are considered simple sensors, and will not be
					// counted here
					averageMistCpuUtilization += dc.getTotalCpuUtilization();
					edgeDevicesCount++;
				}
				if (dc.isDead()) {
					devicesDeathTime.add(dc.getDeathTime());
					remainingPower.add(0.0);
					deadEdgeDevicesCount++;
					if (firstDeviceDeathTime == -1)
						firstDeviceDeathTime = dc.getDeathTime();
					else if (firstDeviceDeathTime > dc.getDeathTime())
						firstDeviceDeathTime = dc.getDeathTime();
				} else {
					if (dc.isBattery()) {
						averageRemainingPowerWh += dc.getBatteryLevel();
						averageRemainingPower += dc.getBatteryLevelPercentage();
						remainingPower.add(dc.getBatteryLevelPercentage());
						aliveBatteryPoweredDevicesCount++;
					}
				}
			}
		}
		averageCpuUtilization = (averageCloudCpuUtilization + averageMistCpuUtilization + averageEdgeCpuUtilization)
				/ (edgeDevicesCount + simulationParameters.NUM_OF_EDGE_DATACENTERS
						+ simulationParameters.NUM_OF_CLOUD_DATACENTERS);
		batteryPoweredDevicesCount = aliveBatteryPoweredDevicesCount + deadEdgeDevicesCount;
		// escape from devision by 0
		if (aliveBatteryPoweredDevicesCount == 0)
			aliveBatteryPoweredDevicesCount = 1;
		averageCloudCpuUtilization = averageCloudCpuUtilization / simulationParameters.NUM_OF_CLOUD_DATACENTERS;
		averageEdgeCpuUtilization = averageEdgeCpuUtilization / simulationParameters.NUM_OF_EDGE_DATACENTERS;
		averageMistCpuUtilization = averageMistCpuUtilization / edgeDevicesCount;

		energyConsumption = cloudEnConsumption + edgeEnConsumption + mistEnConsumption;
		averageRemainingPower = averageRemainingPower / (double) aliveBatteryPoweredDevicesCount;
		averageRemainingPowerWh = averageRemainingPowerWh / (double) aliveBatteryPoweredDevicesCount;
		double averageCloudEnConsumption = cloudEnConsumption / simulationParameters.NUM_OF_CLOUD_DATACENTERS;
		double averageEdgeEnConsumption = edgeEnConsumption / simulationParameters.NUM_OF_EDGE_DATACENTERS;
		double averageMistEnConsumption = mistEnConsumption / simulationManager.getScenario().getDevicesCount();

		print("SimLog- Average vm CPU utilization                                              :"
				+ padLeftSpaces(decimalFormat.format(averageCpuUtilization), 15) + " %");
		print("SimLog- Average vm CPU utilization per level                             : Cloud="
				+ padLeftSpaces(decimalFormat.format(averageCloudCpuUtilization), 15) + " %");
		print("                                                                            Edge="
				+ padLeftSpaces(decimalFormat.format(averageEdgeCpuUtilization), 15) + " %");
		print("                                                                            Mist="
				+ padLeftSpaces(decimalFormat.format(averageMistCpuUtilization), 15) + " %");
		print("SimLog- Energy consumption                                                      :"
				+ padLeftSpaces(decimalFormat.format(energyConsumption), 20) + " W, "
				+ padLeftSpaces(decimalFormat.format(10*Math.log10(energyConsumption)), 10) + " dBW");
		print("                                                                                :(Average:"
				+ decimalFormat.format(energyConsumption / datacentersList.size()) + " W/data center(or device), "
				+ decimalFormat.format(10*Math.log10(energyConsumption / datacentersList.size())) + " dBW/data center(or device))");
		print("                                                                                :(Average:"
				+ decimalFormat.format(energyConsumption / (double) finishedTasks.size()) + " W/task, "
				+ decimalFormat.format(10*Math.log10(energyConsumption / (double) finishedTasks.size())) + " dBW/task)");
		print("SimLog- Energy Consumption per level                                     : Cloud="
				+ padLeftSpaces(decimalFormat.format(cloudEnConsumption), 20) + " W, "
				+ padLeftSpaces(decimalFormat.format(10*Math.log10(cloudEnConsumption)), 10) + " dBW");
		print("                                                                                :(Average:"
				+ decimalFormat.format(cloudEnConsumption / simulationParameters.NUM_OF_CLOUD_DATACENTERS) + " W/data center, "
				+ decimalFormat.format(10*Math.log10(cloudEnConsumption / simulationParameters.NUM_OF_CLOUD_DATACENTERS)) 
				+ " dBW/data center)");
		print("                                                                            Edge="
				+ padLeftSpaces(decimalFormat.format(edgeEnConsumption), 20) + " W, "
				+ padLeftSpaces(decimalFormat.format(10*Math.log10(edgeEnConsumption)), 10) + " dBW");
		print("                                                                                :(Average:"
				+ decimalFormat.format(edgeEnConsumption / simulationParameters.NUM_OF_EDGE_DATACENTERS) + " W/data center, "
				+ decimalFormat.format(10*Math.log10(edgeEnConsumption / simulationParameters.NUM_OF_EDGE_DATACENTERS)) 
				+ " dBW/data center)");
		print("                                                                            Mist="
				+ padLeftSpaces(decimalFormat.format(mistEnConsumption), 20) + " W, "
				+ padLeftSpaces(decimalFormat.format(10*Math.log10(mistEnConsumption)), 10) + " dBW");
		print("                                                                                :(Average:"
				+ decimalFormat.format(mistEnConsumption / currentEdgeDevicesCount) + " W/edge device, "
				+ decimalFormat.format(10*Math.log10(mistEnConsumption / currentEdgeDevicesCount)) + " dBW/edge device)");
		print("SimLog- Dead edge devices due to battery drain                                  :"
				+ padLeftSpaces(decimalFormat.format(deadEdgeDevicesCount), 15) + " devices (Among "
				+ batteryPoweredDevicesCount + " devices with batteries ("
				+ decimalFormat.format(((double) deadEdgeDevicesCount) * 100 / (double) batteryPoweredDevicesCount)
				+ " %))");
		print("SimLog- Average remaining power (devices with batteries that are still alive)   :"
				+ padLeftSpaces(decimalFormat.format(averageRemainingPowerWh), 15) + " Wh (Average: "
				+ decimalFormat.format(averageRemainingPower) + " %)");
		print("SimulationManager- the Scenario Simulation overflow times :"+SimulationManager.overflowtime);
		if (firstDeviceDeathTime != -1)
			print("SimLog- First device died at                                                 :"
					+ padLeftSpaces("" + firstDeviceDeathTime, 15) + " seconds");

		// Add these values to the las item of the results list
		resultsList.set(resultsList.size() - 1, resultsList.get(resultsList.size() - 1)
				+ decimalFormat.format(averageCpuUtilization) + "," + decimalFormat.format(averageCloudCpuUtilization)
				+ "," + decimalFormat.format(averageEdgeCpuUtilization) + ","
				+ decimalFormat.format(averageMistCpuUtilization) + "," + decimalFormat.format(10*Math.log10(energyConsumption)) + ","
				+ decimalFormat.format(10*Math.log10(energyConsumption / datacentersList.size())) + ","
				+ decimalFormat.format(10*Math.log10(energyConsumption / (double) finishedTasks.size())) + ","
				+ decimalFormat.format(10*Math.log10(cloudEnConsumption)) + "," + decimalFormat.format(10*Math.log10(averageCloudEnConsumption)) + ","
				+ decimalFormat.format(10*Math.log10(edgeEnConsumption)) + "," + decimalFormat.format(10*Math.log10(averageEdgeEnConsumption)) + ","
				+ decimalFormat.format(10*Math.log10(mistEnConsumption)) + "," + decimalFormat.format(10*Math.log10(averageMistEnConsumption)) + ","
				+ decimalFormat.format(energyConsumption) + "," + decimalFormat.format(energyConsumption / datacentersList.size()) + ","
				+ decimalFormat.format(energyConsumption / (double) finishedTasks.size()) + ","
				+ decimalFormat.format(cloudEnConsumption) + "," + decimalFormat.format(averageCloudEnConsumption) + ","
				+ decimalFormat.format(edgeEnConsumption) + "," + decimalFormat.format(averageEdgeEnConsumption) + ","
				+ decimalFormat.format(mistEnConsumption) + "," + decimalFormat.format(averageMistEnConsumption) + ","
				+ decimalFormat.format(deadEdgeDevicesCount) + "," + decimalFormat.format(averageRemainingPowerWh) + ","
				+ decimalFormat.format(averageRemainingPower) + "," + firstDeviceDeathTime + ","
				+ remainingPower.toString().replace(",", "-") + "," + devicesDeathTime.toString().replace(",", "-"));
	}

	public String padLeftSpaces(String str, int n) {
		return String.format("%1$" + n + "s", str);
	}

	public void cleanOutputFolder(String outputFolder) throws IOException {
		// Clean the folder where the results files will be saved
		if (isFirstIteration) {
			print("SimLog- Cleaning the outputfolder...");
			isFirstIteration = false;
			Path dir = new File(outputFolder).toPath();
			deleteDirectoryRecursion(dir);
		}
	}

	private void deleteDirectoryRecursion(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursion(entry);
				}
			}
		}
		try {
			Files.delete(path);
		} catch (Exception e) {
			print("SimLog- Could not delete file/folder: " + path.toString());
		}
	}

	public void saveLog() {
		// writing results in csv file
		writeFile(getFileName(".csv"), getResultsList());

		if (!simulationParameters.SAVE_LOG) {
			print("SimLog- no log saving");
			Runtime.getRuntime().exit(0);
		}

		writeFile(getFileName(".txt"), log);

	}

	private List<String> getResultsList() {
		return this.resultsList;
	}

	public void writeFile(String fileName, List<String> Lines) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
			for (String str : Lines) {
				bufferedWriter.append(str);
				bufferedWriter.newLine();
			}
			Lines.clear();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFileName(String extension) {
		String outputFilesName = MainApplication.getOutputFolder() + "/" + simStartTime;
		new File(outputFilesName).mkdirs();
		if (simulationParameters.PARALLEL)
			outputFilesName += "/Parallel_simulation_" + simulationManager.getSimulationId();
		else
			outputFilesName += "/Sequential_simulation";

		return outputFilesName + extension;
	}

	public void print(String line, int flag) {
		String newLine = line;
		if (simulationManager == null) {
			System.out.println("    0.0" + " : " + newLine);
		} else {
			switch (flag) {
			case DEFAULT:
				if (simulationManager.getSimulation().clock() < simulationParameters.INITIALIZATION_TIME)
					newLine = padLeftSpaces("0", 7) + " (s) : " + newLine;
				else
					newLine = padLeftSpaces(decimalFormat.format(
							simulationManager.getSimulation().clock() - simulationParameters.INITIALIZATION_TIME), 7)
							+ " (s) : " + newLine;
				log.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + " - simulation time "
						+ newLine);
				break;
			case NO_TIME:
				log.add(newLine);
				break;
			case SAME_LINE:
				log.set(log.size() - 1, log.get(log.size() - 1) + newLine);
				break;
			default:
				break;
			}
		}
	}

	public void print(String line) {
		print(line, DEFAULT);
	}

	public static void println(String line) {
		System.out.println(line);
	}

	public void deepLog(String line) {
		if (simulationParameters.DEEP_LOGGING)
			print(line, DEFAULT);
	}

	public void deepLog(String line, int flag) {
		if (simulationParameters.DEEP_LOGGING) {
			print(line, flag);
		}
	}

	public void printWithoutTime(String line) {
		print(line, NO_TIME);
	}

	public void printSameLine(String line, String color) {
		if ("red".equalsIgnoreCase(color))
			System.err.print(line);
		else
			System.out.print(line);
	}

	public void printSameLine(String line) {
		System.out.print(line);
	}

	public int getGeneratedTasks() {
		return generatedTasksCount;
	}

	public void setGeneratedTasks(int generatedTasks) {
		this.generatedTasksCount = generatedTasks;
	}

	public void setCurrentOrchPolicy(String currentOrchPolicy) {
		this.currentOrchArchitecture = currentOrchPolicy;
	}

	public void initialize(SimulationManager simulationManager, int dev, int alg, int arch) {
		this.currentEdgeDevicesCount = dev;
		this.currentOrchAlgorithm = simulationParameters.ORCHESTRATION_AlGORITHMS[alg];
		this.currentOrchArchitecture = simulationParameters.ORCHESTRATION_ARCHITECTURES[arch];
		this.simulationManager = simulationManager;
	}

	public String getSimStartTime() {
		return simStartTime;
	}

	public void setSimStartTime(String simStartTime) {
		this.simStartTime = simStartTime;
	}

	public void incrementTasksSent() {
		this.tasksSent++;
	}

	public void incrementTasksFailed(Task task) {
		this.tasksFailed++;
		if (task.getVm() == Vm.NULL)
			return;
		simulationParameters.TYPES type = ((DataCenter) task.getVm().getHost().getDatacenter()).getType();
		
		if (type == simulationParameters.TYPES.CLOUD) {
			this.tasksFailedCloud++;
		} else if (type == simulationParameters.TYPES.EDGE_DATACENTER) {
			this.tasksFailedEdge++;
		} else if (type == simulationParameters.TYPES.EDGE_DEVICE) {
			this.tasksFailedMist++;
		}
	}

	public void incrementFailedBeacauseDeviceDead(Task task) {
		this.tasksFailedBeacauseDeviceDead++;
		incrementTasksFailed(task);
	}

	public void incrementNotGeneratedBeacuseDeviceDead() {
		this.notGeneratedBecDeviceDead++;
	}

	public void incrementTasksFailedLatency(Task task) {
		this.tasksFailedLatency++;
		incrementTasksFailed(task);
	}

	public void incrementTasksFailedMobility(Task task) {
		this.tasksFailedMobility++;
		incrementTasksFailed(task);
	}

	public void incrementTasksFailedLackOfRessources(Task task) {
		this.tasksFailedRessourcesUnavailable++;
		incrementTasksFailed(task);
	}

	public void getTasksExecutionInfos(Task task) {
		this.totalExecutionTime += task.getActualCpuTime();
		if (task.getReceptionTime() != -1) // the task is offloaded or a container has been downloaded
			// in this case don't include the network utilisation time in the waiting time
			// so the waiting time will be the time between the reception of the task (or
			// the conatiner) by the destination and the time of execution
			this.totalWaitingTime += task.getExecStartTime() - task.getReceptionTime();
		else
			// in case the task is not offloaded, and no container has been downloaded ( no
			// network usage)
			// the waitingdelay will be the difference between the tasks execution time and
			// the task generation time
			this.totalWaitingTime += task.getExecStartTime() - task.getTime();
		this.executedTasksCount++;
	}
	
	public void getTasksdelayInfos(Task task) {
		this.totalEtedelayTime += task.getTaskFinishTime() - task.getTime();
		this.EtedelayTasksCount++;
	}

	public void taskSentFromOrchToDest(Task task) {
		simulationParameters.TYPES type = ((DataCenter) task.getVm().getHost().getDatacenter()).getType();
		if (type == simulationParameters.TYPES.CLOUD) {
			this.tasksExecutedOnCloud++;
		} else if (type == simulationParameters.TYPES.EDGE_DATACENTER) {
			this.tasksExecutedOnEdge++;
		} else if (type == simulationParameters.TYPES.EDGE_DEVICE) {
			this.tasksExecutedOnMist++;
		}
	}

	public void updateNetworkUsage(FileTransferProgress transfer) {
		this.totalLanUsage += transfer.getLanNetworkUsage();
		this.totalWanUsage += transfer.getWanNetworkUsage();
		this.totalBandwidth += transfer.getAverageBandwidth() / 1000; // Kbits/s to Mbits/s
		this.totalTraffic += transfer.getFileSize() / 8000; // Kbits to Mbytes

		if (transfer.getTransferType() == FileTransferProgress.Type.CONTAINER) {
			this.containersLanUsage += transfer.getLanNetworkUsage();
			this.containersWanUsage += transfer.getWanNetworkUsage();
		}
		this.transfersCount++;

	}

}
