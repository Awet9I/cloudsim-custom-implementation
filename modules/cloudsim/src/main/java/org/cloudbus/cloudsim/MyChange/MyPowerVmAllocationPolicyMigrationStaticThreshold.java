package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

import java.util.List;

public class MyPowerVmAllocationPolicyMigrationStaticThreshold extends PowerVmAllocationPolicyMigrationStaticThreshold {
    private double lowerUtilizationThreshold;
    /**
     * Instantiates a new PowerVmAllocationPolicyMigrationStaticThreshold.
     *
     * @param hostList             the host list
     * @param vmSelectionPolicy    the vm selection policy
     * @param utilizationThreshold the utilization threshold
     */
    public MyPowerVmAllocationPolicyMigrationStaticThreshold(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy, double upperUtilizationThreshold, double lowerUtilizationThreshold) {
        super(hostList, vmSelectionPolicy, upperUtilizationThreshold);
        this.lowerUtilizationThreshold = lowerUtilizationThreshold;

    }

    public void setLowerUtilizationThreshold(double lowerUtilizationThreshold){
        this.lowerUtilizationThreshold = lowerUtilizationThreshold;
    }

    public double getLowerUtilizationThreshold(){
        return this.lowerUtilizationThreshold;
    }



   /**
	 * Checks if a host is under utilized, based on CPU usage.
	 * 
	 * @param host the host
	 * @return true, if the host is over utilized; false otherwise
	 */
	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		addHistoryEntry(host, getUtilizationThreshold());
		double totalRequestedMips = 0;
		double totalRequestedRam = 0;
		for (Vm vm : host.getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips();
			totalRequestedRam += vm.getCurrentRequestedRam();
		}
		double ramUtilization = totalRequestedRam / host.getRam();
		double utilization = totalRequestedMips / host.getTotalMips();
		return utilization < getLowerUtilizationThreshold();
	}
	

    /**
	 * Checks if a host is over utilized, based on CPU usage.
	 * 
	 * @param host the host
	 * @return true, if the host is over utilized; false otherwise
	 */
	protected boolean isHostUnderUtilized(PowerHost host) {
		addHistoryEntry(host, getUtilizationThreshold());
		double totalRequestedMips = 0;
		double totalRequestedRam = 0;
		for (Vm vm : host.getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips();
			totalRequestedRam += vm.getCurrentRequestedRam();
		}
		double ramUtilization = totalRequestedRam / host.getRam();
		double utilization = totalRequestedMips / host.getTotalMips();System.out.println( "Under utilized host" + host.getId() + utilization );
		return utilization > getUtilizationThreshold();
	}
	



}
