package org.cloudbus.cloudsim.power.models;


public class PowerModelLinearCelsiusV80AMDOpteron2218 extends PowerModelLinear{
    private double[] frequencies;
    private double[] voltages;
    public PowerModelLinearCelsiusV80AMDOpteron2218(double maxPower, double staticPowerPercent, double[] frequencies, double[] voltages) {
        super(maxPower, staticPowerPercent);
        this.frequencies = frequencies;
        this.voltages = voltages;
    }

    public double[] getFrequencies() {
        return frequencies;
    }
    public void setFrequencies(double[] frequencies) {
        this.frequencies = frequencies;
    }

    public double[] getVoltages() {
        return voltages;
    }
    public void setVoltages(double[] voltages) {
        this.voltages = voltages;
    }
}
