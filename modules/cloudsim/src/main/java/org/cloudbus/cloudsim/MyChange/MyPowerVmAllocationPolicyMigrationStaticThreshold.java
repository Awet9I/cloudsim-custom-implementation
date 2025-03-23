package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

import java.util.List;

public class MyPowerVmAllocationPolicyMigrationStaticThreshold extends PowerVmAllocationPolicyMigrationStaticThreshold {
    /**
     * Instantiates a new PowerVmAllocationPolicyMigrationStaticThreshold.
     *
     * @param hostList             the host list
     * @param vmSelectionPolicy    the vm selection policy
     * @param utilizationThreshold the utilization threshold
     */
    public MyPowerVmAllocationPolicyMigrationStaticThreshold(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy, double utilizationThreshold) {
        super(hostList, vmSelectionPolicy, utilizationThreshold);
    }


}
