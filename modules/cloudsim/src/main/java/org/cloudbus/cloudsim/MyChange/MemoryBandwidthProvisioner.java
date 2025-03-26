package org.cloudbus.cloudsim.MyChange;

import java.util.HashMap;
import java.util.Map;
import org.cloudbus.cloudsim.Vm;

public class MemoryBandwidthProvisioner {


    private double totalReadBandwidth, totalWriteBandwidth;
    private double availableReadBandwidth, availableWriteBandwidth;
    private Map<String, double[]> vmBandwidthMap = new HashMap<>();

    public MemoryBandwidthProvisioner(double totalRead, double totalWrite) {
        this.totalReadBandwidth = totalRead;
        this.totalWriteBandwidth = totalWrite;
        this.availableReadBandwidth = totalRead;
        this.availableWriteBandwidth = totalWrite;
    }

    public boolean allocateBandwidthForVm(Vm vm, double readBps, double writeBps) {
        deallocateBandwidthForVm(vm);
        if (readBps <= availableReadBandwidth && writeBps <= availableWriteBandwidth) {
            vmBandwidthMap.put(vm.getUid(), new double[]{readBps, writeBps});
            availableReadBandwidth -= readBps;
            availableWriteBandwidth -= writeBps;
            return true;
        }
        return false;
    }

    public void deallocateBandwidthForVm(Vm vm) {
        double[] bws = vmBandwidthMap.remove(vm.getUid());
        if (bws != null) {
            availableReadBandwidth += bws[0];
            availableWriteBandwidth += bws[1];
        }
    }

    public double[] getAllocatedBandwidthForVm(Vm vm) {
        return vmBandwidthMap.getOrDefault(vm.getUid(), new double[]{0.0, 0.0});
    }

    public void deallocateBandwidthForAllVms() {
        vmBandwidthMap.clear();
        availableReadBandwidth = totalReadBandwidth;
        availableWriteBandwidth = totalWriteBandwidth;
    }
    
}
