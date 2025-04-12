package org.cloudbus.cloudsim.MyChange;

public class PowerModelStorageProbabilistic {
    
    private final double idlePower;        // Watts
    private final double maxReadRate;      // MBps
    private final double maxWriteRate;     // MBps

    public PowerModelStorageProbabilistic(double idlePowerWatts, double maxReadMBps, double maxWriteMBps) {
        this.idlePower = idlePowerWatts;
        this.maxReadRate = maxReadMBps;
        this.maxWriteRate = maxWriteMBps;
    }

    public double getPower(double readRate, double writeRate) {
        double totalRate = readRate + writeRate;
        double maxTotalRate = maxReadRate + maxWriteRate;

        // Bound input values
        if (readRate < 0) readRate = 0;
        if (writeRate < 0) writeRate = 0;
        if (totalRate > maxTotalRate) totalRate = maxTotalRate;

        // Probabilities based on activity level
        double a = totalRate / maxTotalRate;           // Accessing state
        double b = 0.9 * (1 - a);                       // Idle state
        double c = 0.1 * (1 - a);                       // Startup (spin-up) state

        // Refined idle power with deeper idle/standby
        double alpha = 0.7; // probability of regular idle
        double beta = 0.3;  // probability of deeper idle (standby/sleep)
        double refinedIdle = idlePower * (alpha + 0.2 * beta);

        // Final HDD power consumption (W)
        double totalPower = a * (1.4 * idlePower) + b * refinedIdle + c * (3.7 * idlePower);
        return totalPower;
    }
}
