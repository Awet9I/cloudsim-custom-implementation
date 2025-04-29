package org.cloudbus.cloudsim.MyChange;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerVm;

public class CustomVmList extends VmList {
    


    /**
     * Sorts the VMs by decreasing total resource demand (CPU, RAM, Disk, Network).
     * Applies dynamic weights based on VM type.
     * 
     * @param vmList list of VMs to sort
     */
    public static void sortByDecreasingResourceDemand(List<? extends Vm> vmList) {
        Collections.sort(vmList, new Comparator<Vm>() {
            @Override
            public int compare(Vm vm1, Vm vm2) {
                double demand1 = calculateResourceDemand(vm1);
                double demand2 = calculateResourceDemand(vm2);
                return Double.compare(demand2, demand1); // Decreasing order
            }
        });
    }


    /**
     * Calculates the resource demand of a VM based on CPU, RAM, Disk, Network utilizations,
     * using static weights.
     * 
     * @param vm the VM
     * @return resource demand score
     */
    private static double calculateResourceDemand(Vm vm) {
        if (!(vm instanceof PowerVm)) {
            return 0;
        }

        final double MAX_CPU = 10400;    // Total MIPS
        final double MAX_RAM = 16000;     // RAM MB
        final double MAX_DISK = 160000;   // Disk MB
        final double MAX_BW = 1000;       // BW Mbps
    
        PowerVm powerVm = (PowerVm) vm;

        // normilizing the utils based on max available resource 
        double cpuUtil = powerVm.getMips() / MAX_CPU;
        double ramUtil = powerVm.getRam() / MAX_RAM;
        double diskUtil = powerVm.getSize() / MAX_DISK;
        double netUtil = powerVm.getBw() / MAX_BW;
    

        // Default weights
        double cpuWeight = 0.7;
        double ramWeight = 0.2;
        double diskWeight = 0.05;
        double netWeight = 0.05;


        return (cpuWeight * cpuUtil) +
               (ramWeight * ramUtil) +
               (diskWeight * diskUtil) +
               (netWeight * netUtil);
    }
}
