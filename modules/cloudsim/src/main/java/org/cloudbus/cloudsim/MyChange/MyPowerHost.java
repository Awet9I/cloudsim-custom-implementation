package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MyPowerHost extends PowerHostUtilizationHistory {
    private long storageSize;
    private PowerModel raPowerModel;
    private MemoryBandwidthProvisioner memoryBandwidthProvisioner;
    private NetworkPowerModel networkPowerModel;
    private PowerModelStorageProbabilistic storageProbabilistic;
    

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
     */
    public MyPowerHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel, PowerModel raPowerModel, MemoryBandwidthProvisioner memoryBandwidthProvisioner, NetworkPowerModel networkPowerModel, PowerModelStorageProbabilistic storageProbabilistic) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        this.storageSize = storage;
        this.raPowerModel = raPowerModel; 
        this.memoryBandwidthProvisioner = memoryBandwidthProvisioner;
        this.networkPowerModel = networkPowerModel;
        this.storageProbabilistic = storageProbabilistic;
    }


        @Override
    public boolean vmCreate(Vm vm) {
        boolean result = super.vmCreate(vm);
        if (result && vm instanceof MyPowerVm) {
            double[] bws = ((MyPowerVm) vm).getCurrentRequestedMemoryBandwidth();
            if (!memoryBandwidthProvisioner.allocateBandwidthForVm(vm, bws[0], bws[1])) {
                super.vmDestroy(vm);
                return false;
            }
        }
        return result;
    }

    @Override
    public void vmDestroy(Vm vm) {
        memoryBandwidthProvisioner.deallocateBandwidthForVm(vm);
        super.vmDestroy(vm);
    }

    @Override
    public void vmDestroyAll() {
        super.vmDestroyAll();
    }


    @Override
    public double updateVmsProcessing(double currentTime) {
        double smallerTime = super.updateVmsProcessing(currentTime);
        setPreviousUtilizationMips(getUtilizationMips());
        setUtilizationMips(0);
        double hostTotalRequestedMips = 0;
        double hostTotalRequestedRam = 0;
        double hostTotalRequestedBw = 0;
        double hostTotalAllocatedStorage = 0;

        for (Vm vm : getVmList()) {
            getVmScheduler().deallocatePesForVm(vm);
        }

        for (Vm vm : getVmList()) {
            getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
        }

        for (Vm vm : getVmList()) {
            //int pes = vm.getNumberOfPes();
            double totalRequestedMips = vm.getCurrentRequestedTotalMips();
            double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

            double totalRequestedRam = vm.getCurrentRequestedRam();
            double totalAllocatedRam = vm.getCurrentAllocatedRam();//getVmScheduler().getTotalAllocatedMipsForVm(vm);

            double totalRequestedBw = vm.getCurrentRequestedBw();
            double totalAllocatedBw = vm.getCurrentAllocatedBw();

            double totalAllocatedStorage = vm.getSize(); // vm.getCurrentAllocatedSize();

            if (!Log.isDisabled()) {
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
                                + " (Host #" + vm.getHost().getId()
                                + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                        CloudSim.clock(),
                        totalAllocatedMips,
                        totalRequestedMips,
                        vm.getHost().getMaxAvailableMips(),
                        totalRequestedMips / vm.getHost().getMaxAvailableMips() * 100);

                List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
                StringBuilder pesString = new StringBuilder();
                for (Pe pe : pes) {
                    pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
                            .getTotalAllocatedMipsForVm(vm)));
                }
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
                                + getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
                                + pesString,
                        CloudSim.clock());
            }

            if (getVmsMigratingIn().contains(vm)) {
                Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
                        + " is being migrated to Host #" + getId(), CloudSim.clock());
            } else {
                if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                    Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
                            + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                }
                if(vm instanceof MyPowerVm){
                    double requestedBwFromCloudlet = ((MyPowerVm) vm).getCurrentRequestedBwFromCloudlet();
                    double requestedDiskWriteRateFromCloudlet = ((MyPowerVm) vm).getCurrentRequestedDiskWritRateFromCloudlet();
                    double requestedDiskReadRateFromCloudlet = ((MyPowerVm) vm).getCurrentRequestedDiskReadRateFromCloudlet();
                    ((MyPowerVm) vm).addStateHistoryEntry(
                            currentTime,
                            totalAllocatedMips,
                            totalRequestedMips,
                            totalRequestedRam,
                            totalAllocatedRam,
                            totalRequestedBw,
                            totalAllocatedBw,
                            totalAllocatedStorage,
                            (vm.isInMigration() && !getVmsMigratingIn().contains(vm)),
                            requestedBwFromCloudlet,
                            requestedDiskWriteRateFromCloudlet,
                            requestedDiskReadRateFromCloudlet
                            
                            );
                   
                }else{
                    vm.addStateHistoryEntry(
                            currentTime,
                            totalAllocatedMips,
                            totalRequestedMips,
                            (vm.isInMigration() && !getVmsMigratingIn().contains(vm)));
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
        copy.addAll(getVmList());
        List<PeEntry> peEntries = new ArrayList<>();
        for (Pe pe : getPeList()){
            peEntries.add(
                    new PeEntry(
                            pe.getMips(),
                            pe.getPeProvisioner().getAvailableMips()
                    )
            );
        }

        addStateHistoryEntry(
                currentTime,
                getUtilizationMips(),
                hostTotalRequestedMips,
                getUtilizationOfRam(),
                hostTotalRequestedRam,
                getUtilizationOfBw(),
                hostTotalRequestedBw,
                hostTotalAllocatedStorage,
                (getUtilizationMips() > 0),
                peEntries,
                copy
                );



        return smallerTime;
    }

    public PowerModel getPowerModelRam(){
        return raPowerModel;
    }

    public double getRamUtilization(double allocatedRam,  double totalRam) {
        return  allocatedRam/totalRam;
    }


    /**
     * Adds a host state history entry.
     *
     * @param time the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isActive the is active
     */
    public
    void
    addStateHistoryEntry(double time, double allocatedMips, double requestedMips, double allocatedRam, double requestedRam, double allocatedBw, double requestedBw, double allocatedStorage, boolean isActive, List<PeEntry> peEntries, List<PowerVm> vmList) {

        double getRamUtilization = getRamUtilization(allocatedRam, getRamProvisioner().getRam());

        MyPowerHostEntry newState = new MyPowerHostEntry(
                time,
                allocatedMips,
                requestedMips,
                isActive,
                allocatedRam,
                requestedRam,
                allocatedBw,
                requestedBw,
                allocatedStorage,
                peEntries,
                vmList,
                getRamUtilization);
        if (!getStateHistory().isEmpty()) {
            HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
    }

    public long getStorageSize() {
        return storageSize;
    }
    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }

    /**
	 * Checks if the host is suitable for vm. If it has enough resources
         * to attend the VM.
	 * 
	 * @param vm the vm
	 * @return true, if is suitable for vm
	 */
    @Override
	public boolean isSuitableForVm(Vm vm) {
        //System.out.println("Is Suitable comming from my power host");
		return (getVmScheduler().getPeCapacity() >= vm.getCurrentRequestedMaxMips()
				&& getVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips()
				&& getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam()) && getBwProvisioner()
				.isSuitableForVm(vm, vm.getCurrentRequestedBw()));
	}


    public void setNetworkPowerModel(NetworkPowerModel model) {
        this.networkPowerModel = model;
    }

    public NetworkPowerModel getNetworkPowerModel() {
        return this.networkPowerModel;
    }

    public void setStoragePowerModel( PowerModelStorageProbabilistic modelStorageProbabilistic){
        this.storageProbabilistic = modelStorageProbabilistic;
    }
    public PowerModelStorageProbabilistic getStoragePowerModel(){
        return this.storageProbabilistic;
    }
    
    

   
}
