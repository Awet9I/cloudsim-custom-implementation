package org.cloudbus.cloudsim.custom_implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.custom_implementation.MemoryAccessEstimator.MemoryActivity;
import org.cloudbus.cloudsim.power.PowerVm;

public class CustomPowerVm extends PowerVm {
    private Map<Integer, MemoryAccessEstimator.MemoryActivity> cloudletMemoryMap = new HashMap<>();
    private double cumulativeRamEnergyJoules = 0.0;


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
    public CustomPowerVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size, int priority, String vmm, CloudletScheduler cloudletScheduler, double schedulingInterval
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
            boolean isInMigration,
            double requestedBwFromCloudlet,
            double diskReadRate,
            double diskWriteRate) {
        CustomPowerVmEntry newState = new CustomPowerVmEntry(
                time,
                allocatedMips,
                requestedMips,
                isInMigration,
                allocatedRam,
                requestedRam,
                allocatedBw,
                requestedBw,
                allocatedStorage,
                requestedBwFromCloudlet,
                diskReadRate,
                diskWriteRate
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



    public void logCloudletMemoryUsage(Cloudlet cloudlet, double duration, PowerModelRamDataSheetBased ramModel) {
    MemoryActivity activity = MemoryAccessEstimator.estimateBitRates(cloudlet, duration);

    double allocatedMemory = getCurrentAllocatedRam();
    double power = ramModel.getPower(activity.readBitsPerSecond, activity.writeBitsPerSecond, allocatedMemory);
    double energy = power * duration;
    cumulativeRamEnergyJoules += energy;

    if (!getStateHistory().isEmpty()) {
        VmStateHistoryEntry latest = getStateHistory().get(getStateHistory().size() - 1);
        if (latest instanceof CustomPowerVmEntry) {
            CustomPowerVmEntry enriched = new CustomPowerVmEntry(
                latest.getTime(),
                latest.getAllocatedMips(),
                latest.getRequestedMips(),
                latest.isInMigration(),
                ((CustomPowerVmEntry) latest).getAllocatedRam(),
                ((CustomPowerVmEntry) latest).getRequestedRam(),
                ((CustomPowerVmEntry) latest).getAllocatedBw(),
                ((CustomPowerVmEntry) latest).getRequestedBw(),
                ((CustomPowerVmEntry) latest).getAllocatedStorage(),
                activity.readBitsPerSecond,
                activity.writeBitsPerSecond,
                power,
                ((CustomPowerVmEntry) latest).getRequestedBw(),
                ((CustomPowerVmEntry) latest).getDiskReadRate(),
                ((CustomPowerVmEntry) latest).getDiskWriteRate()
            );
            if (!getStateHistory().isEmpty()) {
                VmStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
                if (previousState.getTime() == latest.getTime()) {
                    getStateHistory().set(getStateHistory().size() - 1, enriched);
                    return;
                }
            
        }

        getStateHistory().add(enriched);
    } 
}
}
    

public double[] getCurrentRequestedMemoryBandwidth() {
    return new double[]{0.0, 0.0}; // stubbed; replace with dynamic tracking if needed
}


public double getCurrentRequestedBwFromCloudlet() {
    for (ResCloudlet rc : getCloudletScheduler().getCloudletExecList()) {
        Cloudlet cl = rc.getCloudlet();
        if (cl instanceof WorkloadAwareCloudlet) {
            return ((WorkloadAwareCloudlet) cl).getRequiredBandwidth();
        }
    }
    return 0.0;
}

public double getCurrentRequestedDiskWritRateFromCloudlet() {
    for (ResCloudlet rc : getCloudletScheduler().getCloudletExecList()) {
        Cloudlet cl = rc.getCloudlet();
        if (cl instanceof WorkloadAwareCloudlet) {
            return ((WorkloadAwareCloudlet) cl).getDiskWriteRate();
        }
    }
    return 0.0;
}

public double getCurrentRequestedDiskReadRateFromCloudlet() {
    for (ResCloudlet rc : getCloudletScheduler().getCloudletExecList()) {
        Cloudlet cl = rc.getCloudlet();
        if (cl instanceof WorkloadAwareCloudlet) {
            return ((WorkloadAwareCloudlet) cl).getDiskReadRate();
        }
    }
    return 0.0;
}


}