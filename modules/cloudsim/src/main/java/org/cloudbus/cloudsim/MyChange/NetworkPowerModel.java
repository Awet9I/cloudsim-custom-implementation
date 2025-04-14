package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.PowerVm;
import java.util.List;

// NetworkPowerModel: Computes power consumption from bandwidth usage
public class NetworkPowerModel {
    private final double beta1;
    private final double beta2;
    private final boolean efficient;
    private final int maxBandwidth;
    private double constant;
    private double staticPower;

    public NetworkPowerModel(double beta1, double beta2, boolean efficient, int maxBandwidth) {
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.efficient = efficient;
        this.maxBandwidth = maxBandwidth;
        setStaticPower(0.75 * getMaxPower());
        setConstant((getMaxPower() - getStaticPower()));
    }

    public double getEfficiency(double rateMbps) {
        return beta1 * rateMbps + beta2 * rateMbps * rateMbps;
        // how many bit per watt 
        //return 0.10 * maxBandwidth + 0.004 * maxBandwidth * maxBandwidth;
    }

    public double getPower(double rateMbps) {

        double utilization = rateMbps / maxBandwidth; 

        if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		if (utilization == 0) {
			return getStaticPower();
		}
        if (!efficient){
            return getStaticPower() + getConstant() * utilization; // inefficient fallback based on linear model 
        } 
        double efficiency =  Math.min(getEfficiency(rateMbps), 500.0); // Cap at 20 Mbps/W
        return (rateMbps > 0) ?  (rateMbps / efficiency) : efficiency;
    }

    public double getMaxPower(){
        return maxBandwidth / 20; // 20 Mbp/W the maximum number of whats that can be transmitted per W
    }

    protected double getConstant(){
        return constant;

    }

    protected void setConstant(double constant){
        this.constant = constant;
    }

    protected void setStaticPower(double staticPower){
        this.staticPower = staticPower;
    }

    protected double getStaticPower(){
        return this.staticPower;
    }
}