package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

public class WorkloadAwareCloudlet extends Cloudlet{

    private Double actualRamUsed;

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


    
}
