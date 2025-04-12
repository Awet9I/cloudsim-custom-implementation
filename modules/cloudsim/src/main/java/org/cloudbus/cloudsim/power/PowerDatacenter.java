/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.MyChange.MyPowerHost;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.vmplus.disk.HddCloudlet;
import org.cloudbus.cloudsim.vmplus.disk.HddCloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.vmplus.disk.HddHost;
import org.cloudbus.cloudsim.vmplus.disk.HddResCloudlet;
import org.cloudbus.cloudsim.vmplus.disk.HddVm;
import org.cloudbus.cloudsim.vmplus.util.CustomLog;

/**
 * PowerDatacenter is a class that enables simulation of power-aware data centers.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley &amp; Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenter extends Datacenter {

	/** The datacenter consumed power. */
	private double power;

	/** Indicates if migrations are disabled or not. */
	private boolean disableMigrations;

	/** The last time submitted cloudlets were processed. */
	private double cloudletSubmitted;

	/** The VM migration count. */
	private int migrationCount;

	private int count = 0;

	private PowerDatacenterBroker broker;

	/**
	 * Instantiates a new PowerDatacenter.
	 * 
	 * @param name the datacenter name
	 * @param characteristics the datacenter characteristics
	 * @param schedulingInterval the scheduling interval
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 * @throws Exception the exception
	 */
	public PowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval, PowerDatacenterBroker broker) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		this.broker = broker;
		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
	}


	/**
	 * second contructor without broker
	 * @param name
	 * @param characteristics
	 * @param vmAllocationPolicy
	 * @param storageList
	 * @param schedulingInterval
	 * @throws Exception
	 */
	public PowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		
		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
	}


	

	@Override
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();


		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			System.out.print(currentTime + " ");

			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			for (Vm vm : getVmList()) {
				if (vm instanceof HddVm) {
					HddVm hddVm = (HddVm) vm;
					Host host = hddVm.getHost();
	
					if (host instanceof HddHost) {
						HddHost hddHost = (HddHost) host;
	
						double ioThroughputPerSec = hddHost.getIoCapacity(); // Implement this method in HddHost
						double lastUpdateTime = getLastProcessTime();
	
						hddVm.getHddCloudletScheduler().updateAllDiskCloudlets(currentTime, ioThroughputPerSec, lastUpdateTime);
					}
				}
			}

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
						getVmList());

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null) {
							Log.formatLine(
									"%.2f: Migration of VM #%d to Host #%d is started",
									currentTime,
									vm.getId(),
									targetHost.getId());
						} else {
							Log.formatLine(
									"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									currentTime,
									vm.getId(),
									oldHost.getId(),
									targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration purposes, the other
						// half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(
								getId(),
								vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
								CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}

			setLastProcessTime(currentTime);
		}
	}


		/**
	 * Processes a Cloudlet submission.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		updateCloudletProcessing();

		try {
			Object data = ev.getData();

			// Case 1: HddCloudlet Handling
			if (data instanceof HddCloudlet) {
				HddCloudlet cl = (HddCloudlet) data;
				int userId = cl.getUserId();
				int vmId = cl.getVmId();

				Host host1 = getVmAllocationPolicy().getHost(vmId, userId);
				System.out.println(host1.getClass());
				HddHost host = (HddHost) getVmAllocationPolicy().getHost(vmId, userId);
				HddVm vm = (HddVm) host.getVm(vmId, userId);
				HddCloudletSchedulerTimeShared scheduler = vm.getCloudletScheduler();

				if (!vm.isOutOfMemory()) {

					List<HddResCloudlet> resCloudLets = scheduler.getCloudletExecList();
					System.out.println("MIP share: " + scheduler.getCurrentMipsShare());
					int vmUsedRam = 0;
					for (HddResCloudlet res : resCloudLets) {
						vmUsedRam += res.getCloudlet().getRam();
					}

					// If we have used all of the resources of this VM
					if (vmUsedRam + cl.getRam() > vm.getRam()) {
						scheduler.failAllCloudlets();
						scheduler.addFailedCloudlet(cl);
						vm.setOutOfMemory(true);

						CustomLog.printf("VM/Server %d on host %d in data center %s(%d) is out of memory. "
								+ "It will not be further available", vm.getId(), host.getId(), getName(), getId());
						} else {
							cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics().getCostPerBw());
							double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
							double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);
							if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
								estimatedFinishTime += fileTransferTime;
								send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
							}
							if (ack) {
								sendSubmitAck(cl, CloudSimTags.TRUE);
							}
						}
				} else {
					scheduler.addFailedCloudlet(cl);
					Log.formatLine("Cloudlet %d could not be submitted because "
							+ "VM/Server %d on host %d in data center %s(%d) is out of memory.",
							cl.getCloudletId(), vm.getId(), host.getId(), getName(), getId());
					if (ack) {
						sendSubmitAck(cl, CloudSimTags.FALSE);
					}
				}
			}

			// Case 2: Other Cloudlet Types (including WorkloadAwareCloudlet or base Cloudlet)
			else if (data instanceof Cloudlet) {
				Cloudlet cl = (Cloudlet) data;

				if (cl.isFinished()) {
					warnAlreadyCompleted(cl, ack);
					return;
				}

				cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics().getCostPerBw());
				int userId = cl.getUserId();
				int vmId = cl.getVmId();

				Host host = getVmAllocationPolicy().getHost(vmId, userId);
				Vm vm = host.getVm(vmId, userId);
				CloudletScheduler scheduler = vm.getCloudletScheduler();

				double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
				double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);

				if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
					estimatedFinishTime += fileTransferTime;
					send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
				}

				if (ack) {
					sendSubmitAck(cl, CloudSimTags.TRUE);
				}
			} else {
				Log.printLine(getName() + ": Unknown cloudlet type submitted.");
			}

		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();
		setCloudletSubmitted(CloudSim.clock());
	}

	private void sendSubmitAck(Cloudlet cl, int successFlag) {
		int[] data = new int[3];
		data[0] = getId();
		data[1] = cl.getCloudletId();
		data[2] = successFlag;
		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
	}
	
	private void warnAlreadyCompleted(Cloudlet cl, boolean ack) {
		String name = CloudSim.getEntityName(cl.getUserId());
		Log.printConcatLine(getName(), ": Warning - Cloudlet #", cl.getCloudletId(), " owned by ", name,
				" is already completed/finished.");
		Log.printLine("Therefore, it is not being executed again");
	
		if (ack) {
			sendSubmitAck(cl, CloudSimTags.FALSE);
		}
	
		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
	}
	


	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
         * @see #updateCloudetProcessingWithoutSchedulingFutureEventsForce() 
         * //TODO There is an inconsistence in the return value of this
         * method with return value of similar methods
         * such as {@link #updateCloudetProcessingWithoutSchedulingFutureEventsForce()},
         * that returns {@link Double#MAX_VALUE} by default.
         * The current method returns 0 by default.
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
		if (CloudSim.clock() > getLastProcessTime()) {
			return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
		return 0;
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return expected time of completion of the next cloudlet in all VMs of all hosts or
	 *         {@link Double#MAX_VALUE} if there is no future events expected in this host
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		Log.printLine("\n\n--------------------------------------------------------------\n\n");
		Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);


		for (Host host : this.getHostList()) {

				if(host instanceof PowerHost){
					PowerHost powerHost = (PowerHost) host;
					Log.printLine();

					double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
					if (time < minTime) {
						minTime = time;
					}

					Log.formatLine(
							"%.2f: [Host #%d] utilization is %.2f%%",
							currentTime,
							powerHost.getId(),
							powerHost.getUtilizationOfCpu() * 100);
			} else {
				Log.formatLine(
					"%.2f: [Host #%d] is not a PowerHost, skipping...",
					currentTime,
					host.getId());
			}
		}

		if (timeDiff > 0) {
			Log.formatLine(
					"\nEnergy consumption for the last time frame from %.2f to %.2f:",
					getLastProcessTime(),
					currentTime);

			for (Host host : this.getHostList()) {
				if(host instanceof PowerHost){
					PowerHost powerHost = (PowerHost) host;
					double previousUtilizationOfCpu = powerHost.getPreviousUtilizationOfCpu();
					double utilizationOfCpu = powerHost.getUtilizationOfCpu();
					double timeFrameHostEnergy = powerHost.getEnergyLinearInterpolation(
							previousUtilizationOfCpu,
							utilizationOfCpu,
							timeDiff);
					timeFrameDatacenterEnergy += timeFrameHostEnergy;

					Log.printLine();
					Log.formatLine(
							"%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
							currentTime,
							host.getId(),
							getLastProcessTime(),
							previousUtilizationOfCpu * 100,
							utilizationOfCpu * 100);
					Log.formatLine(
							"%.2f: [Host #%d] energy is %.2f W*sec",
							currentTime,
							host.getId(),
							timeFrameHostEnergy);
				}
			}

			Log.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
		}

		setPower(getPower() + timeFrameDatacenterEnergy);

		
		
		checkCloudletCompletion();

		
		
		/** Remove completed VMs **/
		for (Host host : this.getHostList()) {
			if(host instanceof PowerHost){
				PowerHost powerHost = (PowerHost) host;
				for (Vm vm : powerHost.getCompletedVms()) {
						
						getVmAllocationPolicy().deallocateHostForVm(vm);
						getVmList().remove(vm);
						Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
					
					
				}
		}
	}
		
		


		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}


	@Override
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();
		super.processVmMigrate(ev, ack);
		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
	}


	

	/*@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}*/

	/**
	 * Gets the power.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 * 
	 * @param power the new power
	 */
	protected void setPower(double power) {
		this.power = power;
	}

	public void releaseEmptyHosts(MyPowerHost host) {
        host.setPowerOn(false);
        Log.formatLine("%.2f: Host #%d is idle and can be turned off to save energy.",
                        CloudSim.clock(), host.getId());
            
    }

	/**
	 * Checks if PowerDatacenter is in migration.
	 * 
	 * @return true, if PowerDatacenter is in migration; false otherwise
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if migrations are disabled.
	 * 
	 * @return true, if  migrations are disable; false otherwise
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Disable or enable migrations.
	 * 
	 * @param disableMigrations true to disable migrations; false to enable
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 * 
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submitted.
	 * 
	 * @param cloudletSubmitted the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 * 
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	/**
	 * Sets the migration count.
	 * 
	 * @param migrationCount the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count.
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}

}
