package org.cloudbus.cloudsim.MyChanges;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

public class PeEntry extends Pe {
    private double availableMIPS;
    private double maxMIPS;

    public PeEntry(int id, double maxMIPS, double availableMIPS) {
        super(id, new PeProvisionerSimple(maxMIPS)); 
        this.maxMIPS = maxMIPS;
        this.availableMIPS = availableMIPS;
    }
  
  

    public double getMaxMIPS() {
        return maxMIPS;
    }
    public void setMaxMIPS(double maxMIPS) {
        this.maxMIPS = maxMIPS;
    }

    public double getAvailableMIPS() {
        return availableMIPS;
    }
    public void setAvailableMIPS(double availableMIPS) {
        this.availableMIPS = availableMIPS;
    }
    
}

