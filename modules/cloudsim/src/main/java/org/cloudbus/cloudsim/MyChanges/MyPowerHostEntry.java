package org.cloudbus.cloudsim.MyChanges;

import java.util.List;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.models.PowerModelRamDynamic;

public class MyPowerHostEntry extends HostStateHistoryEntry {
    private double allocatedRam;
    private double requestedRam;
    private double allocatedBw;
    private double requestedBw;
    private double allocatedStorage;
    private double ramPower;
    private List<PeEntry> peEntries;
    private List<PowerVm> vms; // Ensure VMs are stored
    private PowerModelRamDynamic powerModelRam;

    public MyPowerHostEntry(double time, double allocatedMips, double requestedMips, boolean isActive,
                            double allocatedRam, double requestedRam, double allocatedBw, double requestedBw, double allocatedStorage, 
                            List<PeEntry> peEntries, List<PowerVm> vms, double ramPower, PowerModelRamDynamic powerModelRam) {
        super(time, allocatedMips, requestedMips, isActive);
        this.allocatedRam = allocatedRam;
        this.requestedRam = requestedRam;
        this.allocatedBw = allocatedBw;
        this.requestedBw = requestedBw;
        this.allocatedStorage = allocatedStorage;
        this.peEntries = peEntries;

        // Debugging output
        if (vms == null || vms.isEmpty()) {
            System.err.println("⚠️ MyPowerHostEntry created with EMPTY VM LIST!");
        } else {
            System.out.println("✅ MyPowerHostEntry created with " + vms.size() + " VMs.");
        }

        this.vms = vms; // Store VMs properly
        this.ramPower = ramPower;
        this.powerModelRam = powerModelRam;
    }

    public double getRamUtilization() {
        double totalRam = this.allocatedRam;  // Total RAM in MB
        double usedRam = this.requestedRam;  // Used RAM in MB
    
        if (totalRam == 0) {
            return 0.0; // Avoid division by zero
        }
    
        double utilization = usedRam / totalRam; // Convert MB to fraction
        return Math.min(1.0, Math.max(0.0, utilization)); // Ensure it's within [0, 1]
    }

    public double getRamPower() {
        if (this.getPowerModelRam() == null) {
            return 0.0; // No RAM power model assigned
        }
        return this.getPowerModelRam().getPower(getRamUtilization());
    }

    public PowerModelRamDynamic getPowerModelRam() {
        return powerModelRam;
    }

    public void setRamPower(double ramPower) {
        this.ramPower = ramPower;
    }
    
    public List<PowerVm> getVms() {
        return vms;
    }
    public List<PeEntry> getPeEntries() {
        return peEntries;
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

    
    public void setPeEntries(List<PeEntry> peEntries) {
        this.peEntries = peEntries;
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
