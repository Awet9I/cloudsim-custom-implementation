package org.cloudbus.cloudsim.MyChanges;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;

public class MyPowerVmAllocationPolicyMigrationStaticThreshold extends PowerVmAllocationPolicyMigrationStaticThreshold {
    /**
     * Instantiates a new PowerVmAllocationPolicyMigrationStaticThreshold.
     *
     * @param hostList             the host list
     * @param vmSelectionPolicy    the vm selection policy
     * @param utilizationThreshold the utilization threshold
     */
    public MyPowerVmAllocationPolicyMigrationStaticThreshold(List<? extends Host> hostList, SelectionPolicy selectionPolicy, double utilizationThreshold) {
        super(hostList, selectionPolicy, utilizationThreshold);
    }

    
}
