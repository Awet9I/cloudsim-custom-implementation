package org.cloudbus.cloudsim.MyChange;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
 
 /**
  * A power-aware {@link DatacenterBroker}.
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
 public class MyPowerDatacenterBroker extends PowerDatacenterBroker {
 
     /**
      * Instantiates a new PowerDatacenterBroker.
      * 
      * @param name the name of the broker
      * @throws Exception the exception
      */
     public MyPowerDatacenterBroker(String name) throws Exception {
         super(name);
     }
 
     /*
      * fully overriden methos to create vm and submit cloudlets to them
      * enables resource aware vm allocation, 
      * waits for all vm creation treis to be processed to submit cloudlets
      */
     @Override
     protected void processVmCreate(SimEvent ev) {
         int[] data = (int[]) ev.getData();
         int datacenterId = data[0];
         int vmId = data[1];
         int result = data[2];
     
         Vm vm = VmList.getById(getVmList(), vmId);
         incrementVmsAcks(); // must track this manually now
     
         if (result == CloudSimTags.TRUE) {
             getVmsCreatedList().add(vm);
             getVmsToDatacentersMap().put(vm.getId(), datacenterId);
             getDatacenterRequestedIdsList().add(datacenterId);
             Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId +
                     " created successfully in Datacenter #" + datacenterId);
         } else {
             Log.printLine(CloudSim.clock() + ": " + getName() + ": Failed to create VM #" + vmId +
                     " in Datacenter #" + datacenterId);
     
             // Retry in next datacenter
             for (int nextDcId : getDatacenterIdsList()) {
                 if (!getDatacenterRequestedIdsList().contains(nextDcId)) {
                     getDatacenterRequestedIdsList().add(nextDcId);
                     createVmsInDatacenter(nextDcId);
                     Log.printLine(getName() + ": Retrying VM creation in Datacenter #" + nextDcId);
                     return; // 👈 don't continue — wait for next ACK
                 }
             }
         }
     
         // If all VM creation attempts (including retries) have been processed
         if (getVmsAcks() == getVmList().size()) {
             if (getVmsCreatedList().size() > 0) {
                 submitCloudlets();
             } else {
                 Log.printLine(CloudSim.clock() + ": " + getName() +
                         ": None of the required VMs could be created. Aborting.");
                 finishExecution();
             }
         }
     }


    @Override
    protected void processCloudletReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
    
        // Estimate memory activity and log energy
        Vm vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
        if (vm instanceof MyPowerVm) {
            double duration = cloudlet.getFinishTime() - cloudlet.getExecStartTime();
            ((MyPowerVm) vm).logCloudletMemoryUsage(cloudlet, duration, new PowerModelRamDataSheetBased());
        }
    
        super.processCloudletReturn(ev); // continue default handling
    }
}
 