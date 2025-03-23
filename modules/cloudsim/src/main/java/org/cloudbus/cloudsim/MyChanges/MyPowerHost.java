package org.cloudbus.cloudsim.MyChanges;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.Get;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.MyChanges.MyPowerHostEntry;
import org.cloudbus.cloudsim.MyChanges.PeEntry;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelRamDynamic;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;



public class MyPowerHost  extends PowerHost {
    private PowerModelRamDynamic powerModelRam; // Optional RAM power model
    
    public MyPowerHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
                List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
            super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
            //TODO Auto-generated constructor stub
            this.powerModelRam = null; // No RAM power modeling
        
        }
    
    // Constructor for both CPU and RAM power models
    public MyPowerHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
        List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel, PowerModelRamDynamic powerModelRam) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        this.powerModelRam = powerModelRam;
    }


        private long storageSize;

    /**
     * Instantiates a new PowerHost.
     *
     * @param id             the id of the host
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage capacity
     * @param peList         the host's PEs list
     * @param vmScheduler    the VM scheduler
     * @param powerModel
    
    public MyPowerHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        this.storageSize = storage;
    }
   */

    @Override
    public double updateCloudletsProcessing(double currentTime) {
        double smallerTime = super.updateCloudletsProcessing(currentTime);
        setPreviousUtilizationMips(getUtilizationMips());
        setUtilizationMips(0);
        double hostTotalRequestedMips = 0;
        double hostTotalRequestedRam = 0;
        double hostTotalRequestedBw = 0;
        double hostTotalAllocatedStorage = 0;


        double ramUtilization = getRamUtilization();
        double ramPower = getRamPower();

        try (FileWriter writer = new FileWriter("ram_power_log.csv", true)) {
            writer.write(currentTime + "," + getId() + "," + ramUtilization + "," + ramPower + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (GuestEntity vm : getGuestList()) {
            getGuestScheduler().deallocatePesForGuest(vm);
        }

        for (GuestEntity vm : getGuestList()) {
            getGuestScheduler().allocatePesForGuest(vm, vm.getCurrentRequestedMips());
        }

        for (GuestEntity vm : getGuestList()) {
            //int pes = vm.getNumberOfPes();
            double totalRequestedMips = vm.getCurrentRequestedTotalMips();
            double totalAllocatedMips = getGuestScheduler().getTotalAllocatedMipsForGuest(vm);

            double totalRequestedRam = vm.getCurrentRequestedRam();
            double totalAllocatedRam = vm.getRam();//getVmScheduler().getTotalAllocatedMipsForVm(vm);

            double totalRequestedBw = vm.getCurrentRequestedBw();
            double totalAllocatedBw = vm.getBw();

            double totalAllocatedStorage = vm.getSize(); // vm.getCurrentAllocatedSize();

            if (!Log.isDisabled()) {
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
                                + " (Host #" + vm.getHost().getId()
                                + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                        CloudSim.clock(),
                        totalAllocatedMips,
                        totalRequestedMips,
                        vm.getMips(),
                        totalRequestedMips / vm.getMips() * 100);

                List<Pe> pes = getGuestScheduler().getPesAllocatedForGuest(vm);
                StringBuilder pesString = new StringBuilder();
                for (Pe pe : pes) {
                    pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
                            .getTotalAllocatedMipsForGuest(vm)));
                }
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
                                + getNumberOfPes() + " * " + getGuestScheduler().getPeCapacity() + ")."
                                + pesString,
                        CloudSim.clock());
            }

            if (getGuestsMigratingIn().contains(vm)) {
                Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
                        + " is being migrated to Host #" + getId(), CloudSim.clock());
            } else {
                if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                    Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
                            + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                }
                if(vm instanceof MyPowerVm){
                    ((MyPowerVm) vm).addStateHistoryEntry(
                        currentTime,
                        totalAllocatedMips,
                        totalRequestedMips,
                        totalRequestedRam,
                        totalAllocatedRam,
                        totalRequestedBw,
                        totalAllocatedBw,
                        totalAllocatedStorage,
                        (vm.isInMigration() && !getVmsMigratingIn().contains(vm))
                            );
                }else{
                    vm.addStateHistoryEntry(
                            currentTime,
                            totalAllocatedMips,
                            totalRequestedMips,
                            (vm.isInMigration() && !getGuestsMigratingIn().contains(vm)));
                }

                if (vm.isInMigration()) {
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] VM #" + vm.getId() + " is in migration",
                            CloudSim.clock());
                    totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                }
            }

            setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
            hostTotalRequestedMips += totalRequestedMips;
            hostTotalRequestedRam += totalRequestedRam;
            hostTotalRequestedBw += totalRequestedBw;
            hostTotalAllocatedStorage += totalAllocatedStorage;
        }

        List<PowerVm> copy = new ArrayList<PowerVm>();
        copy.addAll(getGuestList());
        List<PeEntry> peEntries = new ArrayList<>();
       
        for (Pe pe : getPeList()){
            peEntries.add(
                    new PeEntry(
                            pe.getId(),
                            pe.getMips(),
                            pe.getPeProvisioner().getAvailableMips()
                    )
            );
        }

        addStateHistoryEntry(
                currentTime,
                getUtilizationMips(),
                hostTotalRequestedMips,
                (getUtilizationMips() > 0)
                );



        return smallerTime;
    }

    public PowerModelRamDynamic getPowerModelRam() {
        return powerModelRam;
    }
    //Get RAM utilization dynamically
    public double getRamUtilization() {
        return (double) getRamProvisioner().getUsedRam() / getRamProvisioner().getRam();
    }

    
    // Get RAM power if a model exists
    public double getRamPower() {
        if (powerModelRam != null) {
            return powerModelRam.getPower(getRamUtilization());
        }
        return 0; // No RAM power consumption if no model
    }

    /**
     * Adds a host state history entry.
     *
     * @param time the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isActive the is active
     */
    @Override
    public void addStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isActive) {
        // Capture the list of VMs currently on the host
        List<PowerVm> vmList = new ArrayList<>();
        for (GuestEntity vm : this.getGuestList()) {
            if (vm instanceof PowerVm) {
                vmList.add((PowerVm) vm);
            }
        }
    
        // Capture the list of PE states
        List<PeEntry> peEntries = new ArrayList<>();
        for (Pe pe : getPeList()) {
            peEntries.add(new PeEntry(pe.getId(), pe.getMips(), pe.getPeProvisioner().getAvailableMips()));
        }
    
        // Debugging output
        System.out.println("🔍 Captured " + vmList.size() + " VMs for MyPowerHostEntry.");
        System.out.println("🔍 Captured " + peEntries.size() + " PEs for MyPowerHostEntry.");

        double ramPower = (powerModelRam != null) ? getRamPower() : 0;
        System.out.println("Captured " + ramPower + "Ram power for MyPowerHostEntry.");
        // Create a new MyPowerHostEntry with complete information
        MyPowerHostEntry newState = new MyPowerHostEntry(
                time,
                allocatedMips,
                requestedMips,
                isActive,
                this.getGuestRamProvisioner().getRam(),
                this.getGuestRamProvisioner().getAvailableRam(),
                this.getBwProvisioner().getBw(),
                this.getBwProvisioner().getAvailableBw(),
                this.getStorageSize(),
                peEntries,
                vmList, // Store the VM list
                ramPower,
                powerModelRam
        );
    
        // Check if the last state was at the same time, and replace it if needed
        if (!getStateHistory().isEmpty()) {
            HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
    
        // Add the new state entry
        getStateHistory().add(newState);
    }

    public long getStorageSize() {
        return storageSize;
    }
    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }


   
}

