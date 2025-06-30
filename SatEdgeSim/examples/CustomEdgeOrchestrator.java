package examples;

import org.cloudbus.cloudsim.cloudlets.Cloudlet.Status;

import edu.weijunyong.satedgesim.DataCentersManager.DataCenter;
import edu.weijunyong.satedgesim.SimulationManager.SimLog;
import edu.weijunyong.satedgesim.SimulationManager.SimulationManager;
import edu.weijunyong.satedgesim.TasksGenerator.Task;
import edu.weijunyong.satedgesim.TasksOrchestration.Orchestrator;

public class CustomEdgeOrchestrator extends Orchestrator {

	public CustomEdgeOrchestrator(SimulationManager simulationManager) {
		super(simulationManager);
	}

	protected int findVM(String[] architecture, Task task) {
		if ("INCEREASE_LIFETIME".equals(algorithm)) {
			return increseLifetime(architecture, task);
		} else {
			SimLog.println("");
			SimLog.println("Custom Orchestrator- Unknnown orchestration algorithm '" + algorithm
					+ "', please check the simulation parameters file...");
			// Cancel the simulation
			Runtime.getRuntime().exit(0);
		}
		return -1;
	}

	private int increseLifetime(String[] architecture, Task task) {
		int vm = -1;
		double minTasksCount = -1; // vm with minimum assigned tasks;
		double vmMips = 0;
		double weight = 0;
		double minWeight = 20;
		// get best vm for this task
		for (int i = 0; i < orchestrationHistory.size(); i++) {
			if (offloadingIsPossible(task, vmList.get(i), architecture)) {
				weight = 1;
				if (((DataCenter) vmList.get(i).getHost().getDatacenter()).isBattery()) {
					if (task.getEdgeDevice()
							.getBatteryLevel() > ((DataCenter) vmList.get(i).getHost().getDatacenter())
									.getBatteryLevel())
						weight = 20; // the destination device has lower remaining power than the task offloading
										// device,in this case it is better not to offload
										// that's why the weight is high (20)
					else
						weight = 15; // in this case the destination has higher remaining power, so it is okey to
										// offload tasks for it, if the cloud and the edge data centers are absent.
				} else
					weight = 1; // if it is not battery powered

				if (minTasksCount == 0)
					minTasksCount = 1;// avoid devision by 0

				if (minTasksCount == -1) { // if it is the first iteration
					minTasksCount = orchestrationHistory.get(i).size()
							- vmList.get(i).getCloudletScheduler().getCloudletFinishedList().size() + 1;
					// if this is the first time, set the first vm as the
					vm = i; // best one
					vmMips = vmList.get(i).getMips();
					minWeight = weight;
				} else if (vmMips / (minTasksCount * minWeight) < vmList.get(i).getMips()
						/ ((orchestrationHistory.get(i).size()
								- vmList.get(i).getCloudletScheduler().getCloudletFinishedList().size() + 1)
								* weight)) {
					// if this vm has more cpu mips and less waiting tasks
					minWeight = weight;
					vmMips = vmList.get(i).getMips();
					minTasksCount = orchestrationHistory.get(i).size()
							- vmList.get(i).getCloudletScheduler().getCloudletFinishedList().size() + 1;
					vm = i;
				}
			}
		}
		// assign the tasks to the vm found
		return vm;
	}

	@Override
	public void resultsReturned(Task task) {
		if(task.getStatus()==Status.FAILED) {
			System.err.println("CustomEdgeOrchestrator, task "+ task.getId()+" has been failed, failure reason is: "+ task.getFailureReason());
		}else {

			System.out.println("CustomEdgeOrchestrator, task "+ task.getId()+" has been successfully executed");
		}
		
	}

}
