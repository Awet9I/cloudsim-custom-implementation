package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.power.models.PowerModel;

public class PowerModelRamDataSheetBased implements PowerModel {

    private double energyPerReadBit = 109;        // pJ
    private double energyPerWriteBit = 141;       // pJ
    private double standbyPowerW = 0.00000007;    // W

    private double lastReadBitsPerSec = 0.0;
    private double lastWriteBitsPerSec = 0.0;
    private double lastDutyCycle = 0.0;

    public PowerModelRamDataSheetBased() {}

    public PowerModelRamDataSheetBased(double energyPerReadBit, double energyPerWriteBit, double standbyPowerW) {
        this.energyPerReadBit = energyPerReadBit;
        this.energyPerWriteBit = energyPerWriteBit;
        this.standbyPowerW = standbyPowerW;
    }

    public double getPower(double readBitsPerSecond, double writeBitsPerSecond, double dutyCycle) {
        lastReadBitsPerSec = readBitsPerSecond;
        lastWriteBitsPerSec = writeBitsPerSecond;
        lastDutyCycle = dutyCycle;

        double readPower = readBitsPerSecond * energyPerReadBit * 1e-12;
        double writePower = writeBitsPerSecond * energyPerWriteBit * 1e-12;
        return (readPower + writePower) * dutyCycle + standbyPowerW * (1 - dutyCycle);
    }

    @Override
    public double getPower(double utilization) {
        return getPower(lastReadBitsPerSec, lastWriteBitsPerSec, utilization);
    }
}
