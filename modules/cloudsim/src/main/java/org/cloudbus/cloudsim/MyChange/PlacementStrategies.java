package org.cloudbus.cloudsim.MyChange;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;

public class PlacementStrategies {
    
     public static boolean firstFit(Vm vm, List<Host> hostList) {
        for (Host host : hostList) {
            if (host.vmCreate(vm)) {
                Log.printLine("First Fit: VM " + vm.getId() + " placed on Host " + host.getId());
               return true;
            }
        }
        return false;
    }

    public static boolean bestFit(Vm vm, List<Host> hostList) {
            Host bestHost = null;
            double minRemaining = Double.MAX_VALUE;
            for (Host host : hostList) {
                if (host.isSuitableForVm(vm)) {
                    double remaining = host.getAvailableMips() - vm.getMips();
                    if (remaining >= 0 && remaining < minRemaining) {
                        minRemaining = remaining;
                        bestHost = host;
                    }
                }
            }
            if (bestHost != null && bestHost.vmCreate(vm)) {
                Log.printLine("Best Fit: VM " + vm.getId() + " placed on Host " + bestHost.getId());
                return true;
        }
        return false;
    }



    public static boolean worstFit(Vm vm, List<Host> hostList) {
        Host worstHost = null;
        double maxRemaining = -1;
    
        // Step 1: Find the worst-fit host
        for (Host host : hostList) {
            if (host.isSuitableForVm(vm)) {
                double remaining = host.getAvailableMips() - vm.getMips();
                if (remaining >= 0 && remaining > maxRemaining) {
                    maxRemaining = remaining;
                    worstHost = host;
                }
            }
        }
    
        // Step 2: Allocate VM to the selected host
        if (worstHost != null && worstHost.vmCreate(vm)) {
            Log.printLine("Worst Fit: VM " + vm.getId() + " placed on Host " + worstHost.getId());
            return true;
        }
    
        return false;
    }


    public static boolean binPackingSingle(Vm vm, List<Host> hostList) {
        // Sort hosts by available MIPS descending
        List<Host> sortedHosts = new ArrayList<>(hostList);
        sortedHosts.sort((h1, h2) -> Double.compare(h2.getAvailableMips(), h1.getAvailableMips()));

        for (Host host : sortedHosts) {
            if (host.vmCreate(vm)) {
                Log.printLine("Bin Packing (approx): VM " + vm.getId() + " placed on Host " + host.getId());
                return true;
            }
        }
        return false;
    }

    public static boolean constraintBased(Vm vm, List<Host> hostList) {
        for (Host host : hostList) {
            if (host.getRamProvisioner().isSuitableForVm(vm, vm.getRam()) &&
                host.getBwProvisioner().isSuitableForVm(vm, vm.getBw()) &&
                host.getStorage() >= vm.getSize()) {

                if (host.vmCreate(vm)) {
                    Log.printLine("Constraint-Based: VM " + vm.getId() + " placed on Host " + host.getId());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean resourceAwareGreedy(Vm vm, List<Host> hostList) {
        Host bestHost = null;
        double bestScore = -1;
        for (Host host : hostList) {
            if (host.isSuitableForVm(vm)) {
                double cpuScore = host.getAvailableMips();
                double ramScore = host.getRamProvisioner().getAvailableRam();
                double score = cpuScore + ramScore;
                if (score > bestScore) {
                    bestScore = score;
                    bestHost = host;
                }
            }
        }
        if (bestHost != null && bestHost.vmCreate(vm)) {
            Log.printLine("Greedy: VM " + vm.getId() + " placed on Host " + bestHost.getId());
            return true;
        }
        return false;
    }




    public static boolean dominantResourceFairness(Vm vm, List<Host> hostList) {
        double vmDominant = Math.max(vm.getRam(), vm.getMips());
        Host bestHost = null;
        double bestDominance = Double.MAX_VALUE;
        for (Host host : hostList) {
            if (host.isSuitableForVm(vm)) {
                double hostDominant = Math.max(
                    host.getRamProvisioner().getAvailableRam(),
                    host.getAvailableMips()
                );
                double dominance = Math.abs(hostDominant - vmDominant);
                if (dominance < bestDominance) {
                    bestDominance = dominance;
                    bestHost = host;
                }
            }
        }
        if (bestHost != null && bestHost.vmCreate(vm)) {
            Log.printLine("DRF: VM " + vm.getId() + " placed on Host " + bestHost.getId());
            return true;
        }
        return false;
        
    }

    public static boolean thresholdBased(Vm vm, List<Host> hostList) {
        double cpuThreshold = 0.9; // 80%
        double ramThreshold = 0.9;
            for (Host host : hostList) {
                double cpuUsage = (host.getTotalMips() - host.getAvailableMips()) / host.getTotalMips();
                double ramUsage = (host.getRam() - host.getRamProvisioner().getAvailableRam()) / host.getRam();
                if (cpuUsage < cpuThreshold && ramUsage < ramThreshold && host.isSuitableForVm(vm)) {
                    if (host.vmCreate(vm)) {
                        Log.printLine("Threshold-Based: VM " + vm.getId() + " placed on Host " + host.getId());
                        return true;
                    }
                }
            }
        return false;
    }

    public static boolean roundRobinWithFiltering(Vm vm, List<Host> hostList) {
        int index = 0;
        int hostCount = hostList.size();
            int attempts = 0;
            while (attempts < hostCount) {
                Host host = hostList.get(index);
                if (host.isSuitableForVm(vm) && host.vmCreate(vm)) {
                    Log.printLine("Round Robin: VM " + vm.getId() + " placed on Host " + host.getId());
                    return true;
                }
                index = (index + 1) % hostCount;
                attempts++;
            }
        return false;
    }

    public static boolean roundRobinWithFilteringSingle(Vm vm, List<Host> hostList, int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'roundRobinWithFilteringSingle'");
    }


    public static boolean policyPABFD(Vm vm, List<Host> hostList){
        Host allocatedHost = null;
        double minPower = Double.MAX_VALUE;

        for (Host  host: hostList) {
            PowerHost powerHost = (PowerHost) host;
            PowerVm powerVm = (PowerVm) vm;
            if (powerHost.isSuitableForVm(vm)) {
                try {

                    double power = getPowerAfterAllocation(powerHost, powerVm);
                    if (power < minPower) {
                        minPower = power;
                        allocatedHost = host;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (allocatedHost != null && allocatedHost.vmCreate(vm)) {
            //getVmTable().put(vm.getUid(), allocatedHost);
            return true;
        }

        return false;
    }

    protected static double getPowerAfterAllocation(PowerHost host, PowerVm vm) throws Exception {
        PowerHost powerHost = (PowerHost) host;

        // Save the current utilization and state
        double powerBefore = powerHost.getPower();

        MyPowerHost myPowerHost = (MyPowerHost) powerHost;
        double ramPowerBefore = myPowerHost.getPowerModelRam().getPower(powerHost.getRamProvisioner().getRam() - powerHost.getRamProvisioner().getAvailableRam()/powerHost.getRamProvisioner().getRam());
        

        double totalPowerBefore = powerBefore + ramPowerBefore;

        // Simulate VM allocation
        powerHost.vmCreate(vm);
        double powerAfter = powerHost.getPower();
        double ramPowerAfter = myPowerHost.getPowerModelRam().getPower(vm.getRam()/powerHost.getRamProvisioner().getRam());
        double totalPowerAfter = powerAfter + ramPowerAfter;
        powerHost.vmDestroy(vm); // Roll back simulation

        //return powerAfter - powerBefore;

        return totalPowerAfter - totalPowerBefore;
    }
    
}
