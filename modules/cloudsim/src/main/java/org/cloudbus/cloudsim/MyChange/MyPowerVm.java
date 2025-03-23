package org.cloudbus.cloudsim.MyChange;

import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.power.PowerVm;

public class MyPowerVm extends PowerVm {
    


    /**
     * Instantiates a new PowerVm.
     *
     * @param id                 the id
     * @param userId             the user id
     * @param mips               the mips
     * @param pesNumber          the pes number
     * @param ram                the ram
     * @param bw                 the bw
     * @param size               the size
     * @param priority           the priority
     * @param vmm                the vmm
     * @param cloudletScheduler  the cloudlet scheduler
     * @param schedulingInterval the scheduling interval
     */
    public MyPowerVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size, int priority, String vmm, CloudletScheduler cloudletScheduler, double schedulingInterval
    ) {
        super(id, userId, mips, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);

    }



    /**
     * Adds a VM state history entry.
     *
     * @param time the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isInMigration the is in migration
     * @param cloudlets 
     */
    public void addStateHistoryEntry(
            double time,
            double allocatedMips,
            double requestedMips,
            double allocatedRam,
            double requestedRam,
            double allocatedBw,
            double requestedBw,
            double allocatedStorage,
            boolean isInMigration) {
        MyPowerVmEntry newState = new MyPowerVmEntry(
                time,
                allocatedMips,
                requestedMips,
                isInMigration,
                allocatedRam,
                requestedRam,
                allocatedBw,
                requestedBw,
                allocatedStorage
                
                );
        if (!getStateHistory().isEmpty()) {
            VmStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
    }
}
