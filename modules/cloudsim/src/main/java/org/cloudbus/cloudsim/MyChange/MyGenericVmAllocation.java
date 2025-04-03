package org.cloudbus.cloudsim.MyChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.t;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.power.PowerDatacenter;

public class MyGenericVmAllocation extends VmAllocationPolicy {
    private final Map<String, Host> vmTable = new HashMap<>();
    private String strategy;
    private int rrIndex = 0; // for round robin
    private PowerDatacenter datacenter;
    

    public MyGenericVmAllocation(List<? extends Host> list, String initialStrategy) {
        super(list);
        this.strategy = initialStrategy.toLowerCase();

    }


    public void setDatacenter(PowerDatacenter datacenter){
        this.datacenter = datacenter;
    }

    public PowerDatacenter getPowerDatacenter(){
        return this.datacenter;
    }

    // Allow runtime strategy update
    public void setStrategy(String strategy) {
        this.strategy = strategy.toLowerCase();
        Log.printLine("VM Allocation Strategy changed to: " + this.strategy);
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        List<Host> hostList = getHostList();

        switch (strategy) {
            case "firstfit":
                boolean res = PlacementStrategies.firstFit(vm, hostList);
                if(res){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                }
                return false;
            case "bestfit":
                boolean bfres = PlacementStrategies.bestFit(vm, hostList);
                if(bfres){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                }
                return false;
            case "worstfit":
                boolean wfres = PlacementStrategies.worstFit(vm, hostList);
                if(wfres){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                }
                return false;
            case "binpacking":
                boolean binres =PlacementStrategies.binPackingSingle(vm, hostList);
                if(binres){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                }
                return false;
            case "constraintbased":
                boolean consres = PlacementStrategies.constraintBased(vm, hostList);
                if(consres){
                        vmTable.put(vm.getUid(), vm.getHost());
                        return true;
                    }
                return false;
            case "resourceaware":
                boolean awres = PlacementStrategies.resourceAwareGreedy(vm, hostList);
                if(awres){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                    }
                return false;
            case "drf":
                boolean drf = PlacementStrategies.dominantResourceFairness(vm, hostList);
                if(drf){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                    }
                return false;
            case "threshold":
                boolean thr = PlacementStrategies.thresholdBased(vm, hostList);
                if(thr){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                    }
                return false;
            case "roundrobin":
                boolean rrb = PlacementStrategies.roundRobinWithFilteringSingle(vm, hostList, rrIndex++);
                if(rrb){
                    vmTable.put(vm.getUid(), vm.getHost());
                    return true;
                }
                return false;
            default:
                Log.printLine("Unknown strategy: " + strategy);
                return false;
        }
    }
  

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host != null && host.isSuitableForVm(vm) && host.vmCreate(vm)) {
            vmTable.put(vm.getUid(), host);
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {

        List<Map<String, Object>> result = new ArrayList<>();
        
        for(Host host : datacenter.getHostList()){
            for(Vm vm : host.getVmList()){
                while (vm.getCloudletScheduler().isFinishedCloudlets()) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("vm", vm);
                    entry.put("host", host);
                    result.add(entry);
                    Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
                    System.out.println("Cloudlet " + cl.getCloudletId() + " Finished" );
                }
            }
        }



        /*for(Vm vm : vmList){

            for (Host host : getHostList()) {

                if(host.isSuitableForVm(vm)){
                    Map<String, Object> map = new HashMap<>();
                    map.put(this.strategy, vm);
                    result.add(map);
                }
            }
        }*/
        return result;
        
    }



  



    
    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = vmTable.remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }

    @Override
    public Host getHost(Vm vm) {
        return vmTable.get(vm.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return vmTable.get(Vm.getUid(userId, vmId));
    }
    
}
