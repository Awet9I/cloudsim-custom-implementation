package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.power.models.PowerModelLinear;

public class PowerModelRamDynamic extends PowerModelLinear  {
    private double idlePower;  // Power when RAM is idle
    private double selfRefreshPower;  // Power in self-refresh mode
    private double thresholdForIdle = 0.10;  // 10% utilization = Idle mode
    private double thresholdForSelfRefresh = 0.05;  // 5% utilization = Self-refresh mode

    public PowerModelRamDynamic(double maxPower, double staticPowerPercent, double idlePower, double selfRefreshPower) {
        super(maxPower, staticPowerPercent);  // Use PowerModelLinear logic
        this.idlePower = idlePower;
        this.selfRefreshPower = selfRefreshPower;
    }

@Override
public double getPower(double ramUtilization) throws IllegalArgumentException {
    if (ramUtilization < 0 || ramUtilization > 1) {
        throw new IllegalArgumentException("RAM Utilization must be between 0 and 1.");
    }

    // Debugging output
    //System.out.println("DEBUG: Calculating RAM Power | Utilization: " + ramUtilization);

    // If RAM is below 5% usage, enter self-refresh mode
    if (ramUtilization <= thresholdForSelfRefresh) {
        return selfRefreshPower;
    }
    // If RAM is below 10% usage, enter idle mode
    else if (ramUtilization <= thresholdForIdle) {
        return idlePower;
    }
    // Otherwise, use normal power scaling
    return super.getPower(ramUtilization);
}
}
