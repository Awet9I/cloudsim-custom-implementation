package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

public class WorkloadAwareCloudlet extends Cloudlet{

    private Double actualRamUsed;
    private double requiredBandwidthMbps = 0.0;
    private double diskReadRate = 0.0;
    private double diskWriteRate = 0.0;


    public WorkloadAwareCloudlet(
            int cloudletId,
            long cloudletLength,
            int pesNumber,
            long fileSize,
            long outputSize,
            UtilizationModel utilizationModelCpu,
            UtilizationModel utilizationModelRam,
            UtilizationModel utilizationModelBw,
            double actualRamUsed) {

        super(cloudletId, cloudletLength, pesNumber,
              fileSize, outputSize,
              utilizationModelCpu, utilizationModelRam, utilizationModelBw);

        this.actualRamUsed = actualRamUsed;
    }

    public double getActualRamUsed() {
        return actualRamUsed;
    }

    public void setRequiredBandwidth(double bw) {
        this.requiredBandwidthMbps = bw;
    }
    
    public double getRequiredBandwidth() {
        return this.requiredBandwidthMbps;
    }

    public void setDiskWriteRate(double writeRate) {
        this.diskWriteRate = writeRate;
    }
    
    public double getDiskWriteRate() {
        return this.diskWriteRate;
    }

    public void setDiskReadRate(double readRate) {
        this.diskReadRate = readRate;
    }
    
    public double getDiskReadRate() {
        return this.diskReadRate;
    }

    

    
}
