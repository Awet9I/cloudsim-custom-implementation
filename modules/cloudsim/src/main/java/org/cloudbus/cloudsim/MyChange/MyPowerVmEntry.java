package org.cloudbus.cloudsim.MyChange;

import java.util.List;

import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.VmStateHistoryEntry;

public class MyPowerVmEntry extends VmStateHistoryEntry {

    private double allocatedRam;
    private double requestedRam;
    private double allocatedBw;
    private double requestedBw;
    private double allocatedStorage;
    

    /**
     * Instantiates a new VmStateHistoryEntry
     *
     * @param time          the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isInMigration the is in migration
     */
    public MyPowerVmEntry(double time, double allocatedMips, double requestedMips, boolean isInMigration,
                          double allocatedRam, double requestedRam, double allocatedBw, double requestedBw, double allocatedStorage) {
        super(time, allocatedMips, requestedMips, isInMigration);

        this.allocatedRam = allocatedRam;
        this.requestedRam = requestedRam;
        this.allocatedBw = allocatedBw;
        this.requestedBw = requestedBw;
        this.allocatedStorage = allocatedStorage;
       
    }


    public double getAllocatedRam() {
        return allocatedRam;
    }
    public void setAllocatedRam(double allocatedRam) {
        this.allocatedRam = allocatedRam;
    }

    public double getRequestedRam() {
        return requestedRam;
    }
    public void setRequestedRam(double requestedRam) {
        this.requestedRam = requestedRam;
    }

    public double getAllocatedBw() {
        return allocatedBw;
    }
    public void setAllocatedBw(double allocatedBw) {
        this.allocatedBw = allocatedBw;
    }

    public double getRequestedBw() {
        return requestedBw;
    }
    public void setRequestedBw(double requestedBw) {
        this.requestedBw = requestedBw;
    }

    public double getAllocatedStorage() {
        return allocatedStorage;
    }
    public void setAllocatedStorage(double allocatedStorage) {
        this.allocatedStorage = allocatedStorage;
    }
}
