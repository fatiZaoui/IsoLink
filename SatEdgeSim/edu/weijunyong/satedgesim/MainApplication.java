package edu.weijunyong.satedgesim;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudsimplus.util.Log;
import ch.qos.logback.classic.Level;

import edu.weijunyong.satedgesim.DataCentersManager.DataCenter;
import edu.weijunyong.satedgesim.DataCentersManager.DefaultDataCenter;
import edu.weijunyong.satedgesim.DataCentersManager.DefaultEnergyModel;
import edu.weijunyong.satedgesim.DataCentersManager.EnergyModel;
import edu.weijunyong.satedgesim.DataCentersManager.ServersManager;
import edu.weijunyong.satedgesim.LocationManager.DefaultMobilityModel;
import edu.weijunyong.satedgesim.LocationManager.Mobility;
import edu.weijunyong.satedgesim.Network.DefaultNetworkModel;
import edu.weijunyong.satedgesim.Network.NetworkModel;
import edu.weijunyong.satedgesim.ScenarioManager.FilesParser;
import edu.weijunyong.satedgesim.ScenarioManager.Scenario;
import edu.weijunyong.satedgesim.ScenarioManager.simulationParameters;
import edu.weijunyong.satedgesim.SimulationManager.ChartsGenerator;
import edu.weijunyong.satedgesim.SimulationManager.SimLog;
import edu.weijunyong.satedgesim.SimulationManager.SimulationManager;
import edu.weijunyong.satedgesim.TasksGenerator.DefaultTasksGenerator;
import edu.weijunyong.satedgesim.TasksGenerator.Task;
import edu.weijunyong.satedgesim.TasksGenerator.TasksGenerator;
import edu.weijunyong.satedgesim.TasksOrchestration.DefaultEdgeOrchestrator;
import edu.weijunyong.satedgesim.TasksOrchestration.Orchestrator;

public class MainApplication {
    protected static String simConfigfile = "SatEdgeSim/settings/simulation_parameters.properties";
    protected static String applicationsFile = "SatEdgeSim/settings/applications.xml";
    protected static String edgeDataCentersFile = "SatEdgeSim/settings/edge_datacenters.xml";
    protected static String edgeDevicesFile = "SatEdgeSim/settings/edge_devices.xml";
    protected static String cloudFile = "SatEdgeSim/settings/cloud.xml";
    protected static String outputFolder = "SatEdgeSim/output/";
    protected static String cloudlocationFile = "SatEdgeSim/settings/locationflie/cloud/cloud Fixed Position.csv";
    protected static String edgeDataCenterslocationFile = "SatEdgeSim/settings/locationflie/edge_datacenter/edge Fixed Position.csv";
    protected static String edgeDeviceslocationFile = "SatEdgeSim/settings/locationflie/edge_devices/mist Fixed Position.csv";

    protected int fromIteration;
    protected static int step = 1;
    protected static int cpuCores;
    protected static List<Scenario> Iterations = new ArrayList<>();
    protected static Class<? extends Mobility> mobilityManager = DefaultMobilityModel.class;
    protected static Class<? extends DataCenter> edgedatacenter = DefaultDataCenter.class;
    protected static Class<? extends TasksGenerator> tasksGenerator = DefaultTasksGenerator.class;
    protected static Class<? extends Orchestrator> orchestrator = DefaultEdgeOrchestrator.class;
    protected static Class<? extends EnergyModel> energyModel = DefaultEnergyModel.class;
    protected static Class<? extends NetworkModel> networkModel = DefaultNetworkModel.class;

    public static void main(String[] args) {
        launchSimulation();
    }

    public static void launchSimulation() {
        SimLog.println("Main- Loading simulation files...");

        FilesParser fp = new FilesParser();
        if (!fp.checkFiles(simConfigfile, edgeDevicesFile, edgeDataCentersFile, applicationsFile, cloudFile,
                cloudlocationFile, edgeDataCenterslocationFile, edgeDeviceslocationFile))
            Runtime.getRuntime().exit(0);

        if (!simulationParameters.DEEP_LOGGING)
            Log.setLevel(Level.OFF);
        else
            Log.setLevel(Level.ALL);

        Date startDate = Calendar.getInstance().getTime();

        for (int algorithmID = 0; algorithmID < simulationParameters.ORCHESTRATION_AlGORITHMS.length; algorithmID++) {
            for (int architectureID = 0; architectureID < simulationParameters.ORCHESTRATION_ARCHITECTURES.length; architectureID++) {
                for (int devicesCount = simulationParameters.MIN_NUM_OF_EDGE_DEVICES; devicesCount <= simulationParameters.MAX_NUM_OF_EDGE_DEVICES; devicesCount += simulationParameters.EDGE_DEVICE_COUNTER_STEP) {
                    Iterations.add(new Scenario(devicesCount, algorithmID, architectureID));
                }
            }
        }

        if (simulationParameters.PARALLEL) {
            cpuCores = Runtime.getRuntime().availableProcessors();
            List<MainApplication> simulationList = new ArrayList<>(cpuCores);

            for (int fromIteration = 0; fromIteration < Math.min(cpuCores, Iterations.size()); fromIteration++) {
                simulationList.add(new MainApplication(fromIteration, cpuCores));
            }

            simulationList.parallelStream().forEach(MainApplication::startSimulation);

        } else {
            new MainApplication(0, 1).startSimulation();
        }

        Date endDate = Calendar.getInstance().getTime();
        SimLog.println("Main- Simulation took : " + simulationTime(startDate, endDate));
        SimLog.println("Main- results were saved to the folder: " + outputFolder);
    }

    public MainApplication(int fromIteration, int step_) {
        this.fromIteration = fromIteration;
        step = step_;
    }

    public void startSimulation() {
        String startTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        int iteration = 1;
        int simulationId = fromIteration + 1;
        boolean isFirstIteration = true;
        SimulationManager simulationManager;
        SimLog simLog = null;
        try {
            for (int it = fromIteration; it < Iterations.size(); it += step) {
                simLog = new SimLog(startTime, isFirstIteration);

                if (simulationParameters.CLEAN_OUTPUT_FOLDER && isFirstIteration && fromIteration == 0) {
                    simLog.cleanOutputFolder(outputFolder);
                }
                isFirstIteration = false;

                CloudSim simulation = new CloudSim();

                simulationManager = new SimulationManager(simLog, simulation, simulationId, iteration,
                        Iterations.get(it));
                simLog.initialize(simulationManager, Iterations.get(it).getDevicesCount(),
                        Iterations.get(it).getOrchAlgorithm(), Iterations.get(it).getOrchArchitecture());

                ServersManager serversManager = new ServersManager(simulationManager, mobilityManager, energyModel,
                        edgedatacenter);
                serversManager.generateDatacentersAndDevices();
                simulationManager.setServersManager(serversManager);

                Constructor<?> TasksGeneratorConstructor = tasksGenerator.getConstructor(SimulationManager.class);
                TasksGenerator tasksGenerator = (TasksGenerator) TasksGeneratorConstructor
                        .newInstance(simulationManager);
                List<Task> tasksList = tasksGenerator.generate();
                simulationManager.setTasksList(tasksList);

                Constructor<?> OrchestratorConstructor = orchestrator.getConstructor(SimulationManager.class);
                Orchestrator edgeOrchestrator = (Orchestrator) OrchestratorConstructor.newInstance(simulationManager);
                simulationManager.setOrchestrator(edgeOrchestrator);

                Constructor<?> networkConstructor = networkModel.getConstructor(SimulationManager.class);
                NetworkModel networkModel = (NetworkModel) networkConstructor.newInstance(simulationManager);
                simulationManager.setNetworkModel(networkModel);

                simulationManager.startSimulation();

                //  Call printTaskStatistics after each simulation
                if (edgeOrchestrator instanceof DefaultEdgeOrchestrator) {
                    String suffix = simulationManager.getScenario().getDevicesCount() + "_devices";
                    ((DefaultEdgeOrchestrator) edgeOrchestrator).printTaskStatistics(suffix);
                }


                if (!simulationParameters.PARALLEL) {
                    simLog.print(simulationParameters.PAUSE_LENGTH + " seconds pause...");
                    for (int k = 1; k <= simulationParameters.PAUSE_LENGTH; k++) {
                        simLog.printSameLine(".");
                        Thread.sleep(1000);
                    }
                    SimLog.println("");
                }

                iteration++;
                SimLog.println("");
                SimLog.println("SimLog- Iteration finished...");
                SimLog.println("");
                SimLog.println(
                        "######################################################################################################################################################################");
            }

            SimLog.println("Main- Simulation Finished!");
            generateCharts(simLog);

        } catch (Exception e) {
            e.printStackTrace();
            SimLog.println("Main- The simulation has been terminated due to an unexpected error");
        }
    }

    protected void generateCharts(SimLog simLog) {
        if (simulationParameters.SAVE_CHARTS && !simulationParameters.PARALLEL && simLog != null) {
            SimLog.println("Main- Saving charts...");
            ChartsGenerator chartsGenerator = new ChartsGenerator(simLog.getFileName(".csv"));
            chartsGenerator.generate();
        }
    }

    private static String simulationTime(Date startDate, Date endDate) {
        long difference = endDate.getTime() - startDate.getTime();
        long seconds = difference / 1000 % 60;
        long minutes = difference / (60 * 1000) % 60;
        long hours = difference / (60 * 60 * 1000) % 24;
        long days = difference / (24 * 60 * 60 * 1000);
        String results = "";
        if (days > 0)
            results += days + " days, ";
        if (hours > 0)
            results += hours + " hours, ";
        results += minutes + " minutes, " + seconds + " seconds.";
        return results;
    }

    public static String getOutputFolder() {
        return outputFolder;
    }

    protected static void setCustomEdgeDataCenters(Class<? extends DataCenter> edgedatacenter2) {
        edgedatacenter = edgedatacenter2;
    }

    protected static void setCustomTasksGenerator(Class<? extends TasksGenerator> tasksGenerator2) {
        tasksGenerator = tasksGenerator2;
    }

    protected static void setCustomEdgeOrchestrator(Class<? extends Orchestrator> orchestrator2) {
        orchestrator = orchestrator2;
    }

    protected static void setCustomMobilityModel(Class<? extends Mobility> mobilityManager2) {
        mobilityManager = mobilityManager2;
    }

    protected static void setCustomEnergyModel(Class<? extends EnergyModel> energyModel2) {
        energyModel = energyModel2;
    }

    protected static void setCustomNetworkModel(Class<? extends NetworkModel> networkModel2) {
        networkModel = networkModel2;
    }
}

