package edu.weijunyong.satedgesim.TasksOrchestration;
 
import org.cloudbus.cloudsim.vms.Vm;

import edu.weijunyong.satedgesim.TasksGenerator.Task;

public class VmTaskMapItem {

	private Task task;
	private Vm vm;

	public VmTaskMapItem(Vm vm, Task task) {
		 this.setVm(vm);
		 this.setTask(task);
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Vm getVm() {
		return vm;
	}

	public void setVm(Vm vm) {
		this.vm = vm;
	}

}
