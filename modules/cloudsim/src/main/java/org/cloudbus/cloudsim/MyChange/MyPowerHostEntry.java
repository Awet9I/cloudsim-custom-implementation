package org.cloudbus.cloudsim.MyChange;

import java.util.List;

import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.HostStateHistoryEntry;

public class MyPowerHostEntry extends HostStateHistoryEntry {

     private double allocatedRam;
    private double requestedRam;
    private double allocatedBw;
    private double requestedBw;
    private double allocatedStorage;
    private List<PeEntry> peEntries;
    private List<PowerVm> vms;
    private double ramUtilization;

    /**
     * Instantiates a new host state history entry.
     *
     * @param time          the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isActive      the is active
     */
    public MyPowerHostEntry(double time, double allocatedMips, double requestedMips, boolean isActive,
                            double allocatedRam, double requestedRam, double allocatedBw, double requestedBw, double allocatedStorage, List<PeEntry> peEntries, List<PowerVm> vms, double ramUtilization) {
        super(time, allocatedMips, requestedMips, isActive);

        this.allocatedRam = allocatedRam;
        this.requestedRam = requestedRam;
        this.allocatedBw = allocatedBw;
        this.requestedBw = requestedBw;
        this.allocatedStorage = allocatedStorage;
        this.peEntries = peEntries;
        this.vms = vms;
        this.ramUtilization = ramUtilization;
    }

    public double getAllocatedRam() {
        return allocatedRam;
    }
    public void setAllocatedRam(double allocatedRam) {
        this.allocatedRam = allocatedRam;
    }

    public double getRamUtilization(){
        return ramUtilization;
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

    public List<PeEntry> getPeEntries() {
        return peEntries;
    }
    public void setPeEntries(List<PeEntry> peEntries) {
        this.peEntries = peEntries;
    }

    public List<PowerVm> getVms() {
        return vms;
    }
    public void setVms(List<PowerVm> vms) {
        this.vms = vms;
    }

    public double getAllocatedStorage() {
        return allocatedStorage;
    }
    public void setAllocatedStorage(double allocatedStorage) {
        this.allocatedStorage = allocatedStorage;
    }
    
}
