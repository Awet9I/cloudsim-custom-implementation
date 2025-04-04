package org.cloudbus.cloudsim.examples;



import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.MyChange.*;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.models.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.io.File;
import java.util.*;

public class Experiment1 {
    private static List<Cloudlet> cloudlets;
    private static List<MyPowerVm> VMs;
    private static List<MyPowerHost> hosts;
    private static List<PowerDatacenter> datacenters;

    private static List<Vm> createVM(int userId, int vms, int idShift) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 250;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for(int i=0;i<vms;i++){
            vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            list.add(vm[i]);
        }

        return list;
    }


    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<>();

        //cloudlet parameters
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelNull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        for(int i=0;i<cloudlets;i++){
            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }
    private static PowerDatacenter createDatacenter(String name, int Ml110G3Hosts, int Ml110G4Hosts, int Ml110G5Hosts, int custom1Hosts, List<MyPowerHost> list, int idShift, MyPowerDatacenterBroker broker){
        // https://www.spec.org/power_ssj2008/results/res2011q4/power_ssj2008-20111018-00401.html

        List<MyPowerHost> hostList;
        if(list != null){
            hostList = list;
        }else{

            hostList = new ArrayList<>();
        }

        // http://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110127-00342.html
        for (int i = 0; i < Ml110G3Hosts;i++){
            int mips = 3000;
            int coresPerHost = 2;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + idShift;
            int ram = 16_000;   // changed from 4000 mb 22.03.2025
            int storage = 160_000;
            int bandwidth = 10_000;
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList),
                            new PowerModelSpecPowerHpProLiantMl110G3PentiumD930(),
                            //new PowerModelRamDynamic(50, 0.2, 10, 5)
                            new PowerModelRamDataSheetBased(),
                            new MemoryBandwidthProvisioner(1e9, 1e9) // 1 Gbps each
                    )
            );
        }

        // https://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110124-00338.html
        for (int i = 0; i < Ml110G4Hosts;i++){
            int mips = 1860;
            int coresPerHost = 2;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + Ml110G3Hosts + idShift;
            int ram = 16_000;
            int storage = 160_000;
            int bandwidth = 10_000;
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList),
                            new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
                            //new PowerModelRamDynamic(50, 0.2, 10, 5)
                            new PowerModelRamDataSheetBased(),
                            new MemoryBandwidthProvisioner(1e9, 1e9) // 1 Gbps each
                    )
            );
        }

        // https://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110124-00339.html
        for (int i = 0; i < Ml110G5Hosts;i++){
            int mips = 2660;
            int coresPerHost = 2;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + Ml110G3Hosts + Ml110G4Hosts + idShift;
            int ram = 4000;
            int storage = 146_000;
            int bandwidth = 10_000;
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList),
                            new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(),
                            //new PowerModelRamDynamic(50, 0.2, 10, 5)
                            new PowerModelRamDataSheetBased(),
                            new MemoryBandwidthProvisioner(1e9, 1e9)
                    )
            );
        }

        // https://link.springer.com/article/10.1007/s10723-015-9334-y
        // https://onlinelibrary.wiley.com/doi/epdf/10.1002/cpe.1867
        // https://www.techpowerup.com/cpu-specs/opteron-2218-he.c3345
        // https://www.spec.org/cpu2006/results/res2007q1/cpu2006-20070205-00329.pdf
        // https://pdf.sciencedirectassets.com/272648/1-s2.0-S1569190X13X00085/1-s2.0-S1569190X13000786/main.pdf?X-Amz-Security-Token=IQoJb3JpZ2luX2VjEOX%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaCXVzLWVhc3QtMSJGMEQCIDdTWNjQAjxPpWPOn9apFLHg9d5l6WuwuDyUKy3JhDCEAiBxaioXUHoW2RMoYuFYpJtDwx%2BCZlbBvOvZoNx2I6g27SqyBQgtEAUaDDA1OTAwMzU0Njg2NSIMFSml8onkJuU4EtEAKo8FDAT8IhXvr7%2Bh7r3o8DEs58BM35mWteJNGQVkRrn4mSRv8j7Z7LvF3wO0AeyzXOsfFL7%2FbqCbwkdYYfgsZveKPZE87kaKFxNhWP9cPdwDa4nyCe4XnB0q1FCMabBY%2FVTwazu6eD788un5Fe520ggdpfxZ4m4UL6ZkqLasg2WHFQ5y9PccXoAS29WDyuxnCRIPgfRq30oTiSDcsY%2Fz8TLfYq6pTZ8F1YocrQyOePu9hMh9IvKncpMTJsV6mAthSonE7CETAkoGf6JgoVd7OTqW3%2BDf5yWRkfSCidN15IBXLdmLuULWUfqhxb6imA4c62Es6apVlCbkque6g0qk6L0rv1C3KsNqtdkGeCR9IC%2Fpw1uBuZvL0vopjztXUiripTSpwO9gxnrAtbOKX144gh9qi2s4BPaDYnozJzGrOdLbVEMQOgaiusA5Zlmu3VEM0MYY7FKgXPUch%2BkBN7I%2BfMN4ljhkMCMiL3HVkgUuZxn5soRsZJ2BHCqR%2F%2FFoGVLjPVIW80RJ9L%2BBePBU0ReI1u%2BGvPKHRjNP0TVOvCS%2Bvjrz3MRy%2FGQod5BRfWbap41n9Gmj7GnFOdeO8Yb5VYt5utAN4eSLsar1fgFoX%2FcnN0k9FrTIyrpL0hgVrncgOFmxTLy627awVDPPndlKA2Obcyk4dlT3xyTVlNSIg3glyiUIdVPoVpcOImCBylz%2FN9x4UcD%2Fs6l7AVNM6QupzpnmwW7eVEY%2F39z3eiS0evw7NGOgXbsSd8zHbWfB1WblWfROLUdaepZF0GuPwohHzwwwQsLor0IPGl1Tf4xk04HQD62sATS%2BwLEcvPH5wKpRhY39y4hXx%2BCUjERTOHR%2FbL9mHvL%2FvAaJHt6qA8WgvUtX6fTUwzCE5o6xBjqyATxiHCQ9ag1n8hNb6ffp6FMeQqtxIUrZKP%2FnYCO4hVMRqKwOhdK63TJG10GKfnj7uz6iZ4uSM9TQlmrROeCBle0l%2FkzcIfeEPnsFKBpw4kF9vEUrDQ9GQSSvTmswI6XXY709q%2FnKSI5QQwaLwRiqcOiRxOAry1oMSj%2FPGEQGwWtMJ4x%2BQrzZH63a%2B6FuEP8xtdFzSR7knc24OgjDuCbA9Np1TPvk74%2FVuf5KtgOKC7nALSE%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240420T130844Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=ASIAQ3PHCVTY4K6332XW%2F20240420%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=8fe51ed76aa5c2d02effb3e9ee8d63ad262874388897ee4526a58060e83a72e8&hash=7c6ece4dd5b455b4dfc846cbfcef7d7e817d6deb682d0d5f0ff97dcc5ce27b87&host=68042c943591013ac2b2430a89b270f6af2c76d8dfd086a07176afe7c76c2c61&pii=S1569190X13000786&tid=spdf-7d7b5e13-0e3a-4b34-956c-387a46a5d20f&sid=494d20046af35149f38854974f2318b05965gxrqb&type=client&tsoh=d3d3LnNjaWVuY2VkaXJlY3QuY29t&ua=0b085a59515352025409&rr=87755d5f1cb55691&cc=no
        // https://onlinelibrary.wiley.com/doi/epdf/10.1002/spe.3248 (why i use 30% of max power as  static power)
        // https://www.nts.nl/files/product/3198_%5Ben%5D_Celsius%20V840_Sales.pdf, https://www.yumpu.com/en/document/read/19365298/celsius-v840-top-end-workstation-power-fujitsu-uk (why the max power is 250W)
        // AMD Opteron 2218
        for (int i = 0; i < custom1Hosts;i++){
            int mips = 2600;
            int coresPerHost = 4;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + Ml110G3Hosts + Ml110G4Hosts + Ml110G5Hosts + idShift;
            int ram = 16_000;
            int storage = 80_000;//146000;
            int bandwidth = 10_000;

            double maxPower = 250; // https://www.nts.nl/files/product/3198_%5Ben%5D_Celsius%20V840_Sales.pdf, https://www.yumpu.com/en/document/read/19365298/celsius-v840-top-end-workstation-power-fujitsu-uk (why the max power is 250W)
            double staticPowerPercentage = 0.3; // https://onlinelibrary.wiley.com/doi/epdf/10.1002/spe.3248 (why i use 30% of max power as  static power)

            // https://link.springer.com/article/10.1007/s10723-015-9334-y
            double[] frequecies = {2600.0, 2400.0, 2200.0, 2000.0, 1800.0, 1000.0};
            double[] voltages = {1.30, 1.25, 1.20, 1.15, 1.10, 1.05};
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList),
                            new PowerModelLinearCelsiusV80AMDOpteron2218(maxPower, staticPowerPercentage, frequecies, voltages),
                            //new PowerModelRamDynamic(50, 0.2, 10, 5)
                            new PowerModelRamDataSheetBased(),
                            new MemoryBandwidthProvisioner(1e9, 1e9) // 1 Gbps each
                    )
            );
        }
        /*
        // https://link.springer.com/article/10.1007/s10723-015-9334-y
        // https://onlinelibrary.wiley.com/doi/epdf/10.1002/cpe.1867
        // https://www.techpowerup.com/cpu-specs/opteron-2218-he.c3345
        // https://www.spec.org/cpu2006/results/res2007q1/cpu2006-20070205-00329.pdf
        //https://pdf.sciencedirectassets.com/272648/1-s2.0-S1569190X13X00085/1-s2.0-S1569190X13000786/main.pdf?X-Amz-Security-Token=IQoJb3JpZ2luX2VjEOX%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaCXVzLWVhc3QtMSJGMEQCIDdTWNjQAjxPpWPOn9apFLHg9d5l6WuwuDyUKy3JhDCEAiBxaioXUHoW2RMoYuFYpJtDwx%2BCZlbBvOvZoNx2I6g27SqyBQgtEAUaDDA1OTAwMzU0Njg2NSIMFSml8onkJuU4EtEAKo8FDAT8IhXvr7%2Bh7r3o8DEs58BM35mWteJNGQVkRrn4mSRv8j7Z7LvF3wO0AeyzXOsfFL7%2FbqCbwkdYYfgsZveKPZE87kaKFxNhWP9cPdwDa4nyCe4XnB0q1FCMabBY%2FVTwazu6eD788un5Fe520ggdpfxZ4m4UL6ZkqLasg2WHFQ5y9PccXoAS29WDyuxnCRIPgfRq30oTiSDcsY%2Fz8TLfYq6pTZ8F1YocrQyOePu9hMh9IvKncpMTJsV6mAthSonE7CETAkoGf6JgoVd7OTqW3%2BDf5yWRkfSCidN15IBXLdmLuULWUfqhxb6imA4c62Es6apVlCbkque6g0qk6L0rv1C3KsNqtdkGeCR9IC%2Fpw1uBuZvL0vopjztXUiripTSpwO9gxnrAtbOKX144gh9qi2s4BPaDYnozJzGrOdLbVEMQOgaiusA5Zlmu3VEM0MYY7FKgXPUch%2BkBN7I%2BfMN4ljhkMCMiL3HVkgUuZxn5soRsZJ2BHCqR%2F%2FFoGVLjPVIW80RJ9L%2BBePBU0ReI1u%2BGvPKHRjNP0TVOvCS%2Bvjrz3MRy%2FGQod5BRfWbap41n9Gmj7GnFOdeO8Yb5VYt5utAN4eSLsar1fgFoX%2FcnN0k9FrTIyrpL0hgVrncgOFmxTLy627awVDPPndlKA2Obcyk4dlT3xyTVlNSIg3glyiUIdVPoVpcOImCBylz%2FN9x4UcD%2Fs6l7AVNM6QupzpnmwW7eVEY%2F39z3eiS0evw7NGOgXbsSd8zHbWfB1WblWfROLUdaepZF0GuPwohHzwwwQsLor0IPGl1Tf4xk04HQD62sATS%2BwLEcvPH5wKpRhY39y4hXx%2BCUjERTOHR%2FbL9mHvL%2FvAaJHt6qA8WgvUtX6fTUwzCE5o6xBjqyATxiHCQ9ag1n8hNb6ffp6FMeQqtxIUrZKP%2FnYCO4hVMRqKwOhdK63TJG10GKfnj7uz6iZ4uSM9TQlmrROeCBle0l%2FkzcIfeEPnsFKBpw4kF9vEUrDQ9GQSSvTmswI6XXY709q%2FnKSI5QQwaLwRiqcOiRxOAry1oMSj%2FPGEQGwWtMJ4x%2BQrzZH63a%2B6FuEP8xtdFzSR7knc24OgjDuCbA9Np1TPvk74%2FVuf5KtgOKC7nALSE%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240420T130844Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=ASIAQ3PHCVTY4K6332XW%2F20240420%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=8fe51ed76aa5c2d02effb3e9ee8d63ad262874388897ee4526a58060e83a72e8&hash=7c6ece4dd5b455b4dfc846cbfcef7d7e817d6deb682d0d5f0ff97dcc5ce27b87&host=68042c943591013ac2b2430a89b270f6af2c76d8dfd086a07176afe7c76c2c61&pii=S1569190X13000786&tid=spdf-7d7b5e13-0e3a-4b34-956c-387a46a5d20f&sid=494d20046af35149f38854974f2318b05965gxrqb&type=client&tsoh=d3d3LnNjaWVuY2VkaXJlY3QuY29t&ua=0b085a59515352025409&rr=87755d5f1cb55691&cc=no
        // https://onlinelibrary.wiley.com/doi/epdf/10.1002/spe.3248 (why i use 30% of max power as  static power)
        // AMD Opteron 2218

        //https://www.spec.org/cpu2006/results/res2006q3/cpu2006-20060513-00008.html
        // AMD Athlon 64 FX
        int custom1Hosts = 0;
        for (int i = 0; i < custom1Hosts;i++){
            int mips = 2600;
            int coresPerHost = 4;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + Ml110G3Hosts + Ml110G4Hosts + Ml110G5Hosts + idShift;
            int ram = 16000;
            int storage = 80000;//146000;
            int bandwidth = 1000;

            double maxPower = 250; // https://www.nts.nl/files/product/3198_%5Ben%5D_Celsius%20V840_Sales.pdf, https://www.yumpu.com/en/document/read/19365298/celsius-v840-top-end-workstation-power-fujitsu-uk (why the max power is 250W)
            double staticPowerPercentage = 0.3; // https://onlinelibrary.wiley.com/doi/epdf/10.1002/spe.3248 (why i use 30% of max power as  static power)
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeSharedOverSubscription(peList),
                            new PowerModelLinear(maxPower, staticPowerPercentage)
                    )
            );
        }
        */

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        PowerDatacenter datacenter = null;
        MyGenericVmAllocation genericVmAllocation = new MyGenericVmAllocation(hostList, "bestfit");
        try {
            //datacenter = new PowerDatacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 300);
            //datacenter = new PowerDatacenter(name, characteristics, new VmAllocationPolicyResilient(hostList), storageList, 300);
            datacenter = new PowerDatacenter(name, characteristics, genericVmAllocation, storageList, 300, broker);
            genericVmAllocation.setDatacenter(datacenter);
            // datacenter = new PowerDatacenter(name, characteristics, new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, new PowerVmSelectionPolicyRandomSelection(), 0.92), storageList, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static MyPowerDatacenterBroker createBroker(String name){

        MyPowerDatacenterBroker broker = null;
        try {
            broker = new MyPowerDatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    public static List<MyPowerVm> DatasetVMPerformance(int broker_id) {
        List<MyPowerVm> vmFromDataset = new LinkedList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/ubuntu/cloudsim_imp/3.csv"))) {
            String line;
            bufferedReader.readLine();
            int vm_id = 0;
            //while ((line = bufferedReader.readLine()) != null) {
            for(int i = 0; i < 2; i++){
                line = bufferedReader.readLine();
                String[] features = line.split(";\t");

                UtilizationModel utilizationModel = new UtilizationModelFull();

                /*System.out.println(features[0]);
                System.out.println(features[1]);
                System.out.println(features[2]);
                System.out.println(features[3]);
                System.out.println(features[4]);
                System.out.println(features[5]);
                System.out.println(features[6]);
                System.out.println(features[7]);
                System.out.println(features[8]);
                System.out.println(features[9]);*/

                String timestamp = features[0];
                int CPUCores = Integer.parseInt(features[1]);
                double CPUCapacityProvisioned = Double.parseDouble(features[2]);
                double CPUUsage = Double.parseDouble(features[3]);
                double CPUUsagePercent = Double.parseDouble(features[4]);
                double memoryCapacityProvisioned = Double.parseDouble(features[5]);
                double memoryUsage = Double.parseDouble(features[6]);
                double diskReadThroughput = Double.parseDouble(features[7]);
                double diskWriteThroughput = Double.parseDouble(features[8]);
                double networkReceivedThroughput = Double.parseDouble(features[9]);
                double networkTransmittedThroughput = Double.parseDouble(features[10]);


                //VM Parameters
                long size = 1000;//10000; //image size (MB)
                int ram = 512; //vm memory (MB)
                int mips = 250;
                long bw = 1000;
                int pesNumber = 1; //number of cpus
                String vmm = "Xen"; //VMM name
                

                /*if (networkReceivedThroughput >= networkTransmittedThroughput){
                    bw = (long) networkReceivedThroughput;
                }else{
                    bw = (long) networkTransmittedThroughput;
                }

                if (networkReceivedThroughput + networkTransmittedThroughput > 50){
                    System.out.println("------------------------------");
                    System.out.println(networkReceivedThroughput);
                    System.out.println(networkTransmittedThroughput);
                    System.out.println(networkReceivedThroughput + networkTransmittedThroughput);
                    System.out.println(((networkReceivedThroughput + networkTransmittedThroughput) / 1000));
                    System.out.println("------------------------------");
                }*/

                //Vm  vm = new Vm(vm_id, broker_id, mips*CPUCores, CPUCores, (int) (memoryCapacityProvisioned/1000), bw, size, vmm, new CloudletSchedulerTimeShared());
                //MyPowerVm vm = new MyPowerVm(vm_id, broker_id, CPUUsage, CPUCores,  (int) (memoryUsage / 1000), (long) ((networkReceivedThroughput + networkTransmittedThroughput) / 1000), size, 1, vmm, new CloudletSchedulerDynamicWorkload(CPUUsage, CPUCores), 300); Test comment
                MyPowerVm vm = new MyPowerVm(vm_id, broker_id, CPUUsage, CPUCores,  (int) ((memoryUsage / 1000)*2), (long) (networkReceivedThroughput + networkTransmittedThroughput), size, 1, vmm, new CloudletSchedulerSpaceShared(), 300);
                vmFromDataset.add(vm);
                vm_id++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return vmFromDataset;
    }

    public static List<Cloudlet> DatasetJobs(int broker_id, int simulation_limit) {
        List<Cloudlet> cloudletsFromDataset = new LinkedList<>();
        HashMap<String, Integer> userIds = new HashMap<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/ubuntu/cloudsim_imp/3.csv"))) {
            String line;
            bufferedReader.readLine();
            int cloudlet_id = 0;
            //while ((line = bufferedReader.readLine()) != null) {
            for(int i = 0; i < 4; i++){
                line = bufferedReader.readLine();
                String[] features = line.split(";\t");

                UtilizationModel utilizationModel = new UtilizationModelFull();

                String timestamp = features[0];
                int CPUCores = Integer.parseInt(features[1]);
                double CPUCapacityProvisioned = Double.parseDouble(features[2]);
                double CPUUsage = Double.parseDouble(features[3]);
                double CPUUsagePercent = Double.parseDouble(features[4]);
                double memoryCapacityProvisioned = Double.parseDouble(features[5]);
                double memoryUsage = Double.parseDouble(features[6]);
                double diskReadThroughput = Double.parseDouble(features[7]);
                double diskWriteThroughput = Double.parseDouble(features[8]);
                double networkReceivedThroughput = Double.parseDouble(features[9]);
                double networkTransmittedThroughput = Double.parseDouble(features[10]);


                long length = 24 * simulation_limit;
                long fileSize = 300;
                long outputSize = 300;
                int pesNumber = 1;

                Cloudlet ram_aware_Cloudlet = new WorkloadAwareCloudlet(cloudlet_id, length, CPUCores, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(), (memoryUsage/1000));

                /*Cloudlet cloudlet = new Cloudlet(cloudlet_id, length, CPUCores, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
                cloudlet.setVmId(cloudlet_id);
                cloudlet.setUserId(broker_id);*/


                ram_aware_Cloudlet.setVmId(cloudlet_id);
                ram_aware_Cloudlet.setUserId(broker_id);
                cloudletsFromDataset.add(ram_aware_Cloudlet);
                cloudlet_id++;

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cloudletsFromDataset;
    }


    public static void main(String[] args){
        try{

             Date now = new Date();
            String outputFile = now.toString();
            String [] FileNameWords = outputFile.split(" ");
            String logFileName = "log_" + FileNameWords[0] + "_" + FileNameWords[1] + "_" + FileNameWords[2];
            java.io.File logFile = new File("/home/ubuntu/cloudsim_imp/logs/" + logFileName + ".log");
            logFile.getParentFile().mkdirs(); // Create directories if they don't exist
            
            // Redirect System.out and System.err to a log file
            PrintStream fileOut = new PrintStream(new FileOutputStream(logFile, false));
            System.setOut(fileOut);
            System.setErr(fileOut);


           




            int users = 1;
            Calendar calender = Calendar.getInstance();
            boolean trace_flags = true;

            CloudSim.init(users, calender, trace_flags);


            MyPowerDatacenterBroker broker = createBroker("Central");


            int[] hosts_datacenter = {200*2, 185*1, 190*1, 275*1};
            int[] hosts_datacenter_id_shifts = {0, hosts_datacenter[0], hosts_datacenter[0] + hosts_datacenter[1], hosts_datacenter[0] + hosts_datacenter[1] + hosts_datacenter[2]};
            datacenters = new ArrayList<PowerDatacenter>();
            PowerDatacenter datacenter1 = createDatacenter("Datacenter_1", 0, 0, 1, 1, null, hosts_datacenter_id_shifts[0], broker);
            //PowerDatacenter datacenter1 = createDatacenter("Datacenter_1", 100*2, 40*2, 40*2, 20*2, null, hosts_datacenter_id_shifts[0]);
            //PowerDatacenter datacenter2 = createDatacenter("Datacenter_2", 20*1, 100*1, 40*1, 25*1, datacenter1.getHostList(), hosts_datacenter_id_shifts[1]);
            //PowerDatacenter datacenter3 = createDatacenter("Datacenter_3", 0*1, 40*1, 120*1, 30*1, datacenter2.getHostList(), hosts_datacenter_id_shifts[2]);
            //PowerDatacenter datacenter4 = createDatacenter("Datacenter_4", 80*1, 90*1, 70*1, 35*1, datacenter3.getHostList(), hosts_datacenter_id_shifts[3]);

            System.out.println(datacenter1.getHostList().size());
            //System.out.println(datacenter2.getHostList().size());
            //System.out.println(datacenter3.getHostList().size());
            //System.out.println(datacenter4.getHostList().size());

            datacenter1.setDisableMigrations(false);
            //datacenter2.setDisableMigrations(false);
            //datacenter3.setDisableMigrations(false);
            //datacenter4.setDisableMigrations(false);
            datacenters.add(datacenter1);
            //datacenters.add(datacenter2);
            //datacenters.add(datacenter3);
            //datacenters.add(datacenter4);

            int SIMULATION_LIMIT = 24*60*60;

           

            VMs = DatasetVMPerformance(broker.getId());//createVM(broker.getId(), 5, 0); //creating 5 vms
            cloudlets = DatasetJobs(broker.getId(), SIMULATION_LIMIT);// createCloudlet(broker.getId(), 10, 0); // creating 10 cloudlets

            broker.submitVmList(VMs);
            broker.submitCloudletList(cloudlets);





            // Fifth step: Starts the simulation

            CloudSim.terminateSimulation(SIMULATION_LIMIT);
            CloudSim.startSimulation();
            //new Thread(monitor).start();
            //Thread.sleep(10000);
            // Final step: Print results when simulation is over

            System.out.println("Logging results...");
            String fileName = "data_" + FileNameWords[0] + "_" + FileNameWords[1] + "_" + FileNameWords[2];
            java.io.File logfile = new java.io.File("/home/ubuntu/cloudsim_imp/logs/" + fileName + ".csv");

            try {
                logfile.createNewFile();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                System.err.println("Can't create file");
            }
            //String msg = "time;datacenter_name;host_id;type;active;number_of_pes;available_pes;mips;available_mips;utilization_per_pe;ram;available_ram;bw;available_bw;power_model;vms\n"; // frequencies;mips_per_frequency;cpu_idle_power_per_frequency;cpu_full_power_per_frequency;
            String msg = "time;datacenter_id;datacenter_name;host_id;type;active;number_of_pes;available_pes;mips;available_mips;utilization_per_pe;dvfs_available;frequency_range;voltage_range;cpu_utilization;cpu_power;ram;allocated_ram;ram_power;bw;available_bw;storage;available_storage;power_model;vms\n"; // frequencies;mips_per_frequency;cpu_idle_power_per_frequency;cpu_full_power_per_frequency;


            HashMap<Integer, Integer> vm_ram_map = new HashMap<>();



            int datacenter_number = 0;
            for(PowerDatacenter datacenter : datacenters){
                System.out.println("Extracting data from datacenter " + datacenter.getName() + "...");
                //System.out.println("Datacenter...");
                /*int start_index = 0;
                int end_index = 0;
                if (datacenter_number == 0){
                    start_index = 0;
                    end_index = hosts_datacenter[datacenter_number] - 1;
                    System.out.println("---------");
                    System.out.println(start_index);
                    System.out.println(end_index);

                }else {
                    start_index = 0;
                    end_index = hosts_datacenter[datacenter_number] - 1;

                    for (int j = datacenter_number-1; j >= 0; j--){
                        start_index += hosts_datacenter[datacenter_number - 1];
                        end_index += hosts_datacenter[datacenter_number - 1];
                    }
                    System.out.println("---------");
                    System.out.println(start_index);
                    System.out.println(end_index);
                }*/
                /*else if(datacenter_number == 1) {
                    start_index = hosts_datacenter[datacenter_number - 1];
                    end_index = start_index + hosts_datacenter[datacenter_number] - 1;
                } else if (datacenter_number == 2) {
                    start_index = hosts_datacenter[datacenter_number - 1];
                    end_index = start_index + hosts_datacenter[datacenter_number] - 1;
                }*/
                //System.out.println(datacenter.getHostList().subList(start_index, end_index).size());
                //System.out.println("-------------------------------------------");
                //System.out.println(hosts_datacenter_id_shifts[datacenter_number]);
                //System.out.println(hosts_datacenter_id_shifts[datacenter_number] + hosts_datacenter[datacenter_number] - 1);
                //System.out.println(datacenter.getHostList().subList(hosts_datacenter_id_shifts[datacenter_number], hosts_datacenter_id_shifts[datacenter_number] + hosts_datacenter[datacenter_number]).size());



                //for(int i = hosts_datacenter_id_shifts[datacenter_number]; i < hosts_datacenter_id_shifts[datacenter_number] + hosts_datacenter[datacenter_number]; i++){

                //for(Host host : datacenter.getHostList().subList(hosts_datacenter_id_shifts[datacenter_number], hosts_datacenter_id_shifts[datacenter_number] + hosts_datacenter[datacenter_number])){ // .subList(start_index, end_index)
                for(int i = 0; i < datacenter.getHostList().size(); i++){
                    Host host = datacenter.getHostList().get(i);
                    if(host instanceof MyPowerHost){
                        //System.out.println(host.getId());
                        //if(host.getId() == 75){
                        //    System.out.println("Herez");
                        //}

                        /*String frequencies = "";
                        String mipsPerFrequency = "";
                        String cpuIdlePerFrequency = "";
                        String cpuFullPerFrequency = "";
                        if(((MyPowerHost) host).getPowerModel() instanceof PowerModelSpecHpProLiantDl165G7AMDOpteron6276) {
                            for (int i = 0; i < 4; i++) {
                                frequencies += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.AVAILABLE_FREQUENCIES[i] + ",";
                                mipsPerFrequency += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.AVAILABLE_FREQUENCIES_AS_MIPS[i] + ",";
                                cpuIdlePerFrequency += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.CPU_IDLE_POWER_PER_FREQUENCY[i] + ",";
                                cpuFullPerFrequency += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.CPU_FULL_POWER_PER_FREQUENCY[i] + ",";
                            }
                        }*/
                        String frequencyRange, voltageRange;
                        boolean dvfs;
                        if (((MyPowerHost) host).getPowerModel() instanceof PowerModelSpecPowerHpProLiantMl110G3PentiumD930){
                            // https://www.intel.com/content/www/us/en/products/sku/27518/intel-pentium-d-processor-930-4m-cache-3-00-ghz-800-mhz-fsb/specifications.html
                            frequencyRange = "";
                            voltageRange = "1.200-1.3375";
                            dvfs = false;
                        } else if (((MyPowerHost) host).getPowerModel() instanceof PowerModelSpecPowerHpProLiantMl110G4Xeon3040) {
                            // https://www.intel.com/content/www/us/en/products/sku/27203/intel-xeon-processor-3040-2m-cache-1-86-ghz-1066-mhz-fsb/specifications.html
                            frequencyRange = "";
                            voltageRange = "0.8500-1.500";
                            dvfs = false;
                        } else if (((MyPowerHost) host).getPowerModel() instanceof  PowerModelSpecPowerHpProLiantMl110G5Xeon3075) {
                            // https://ark.intel.com/content/www/us/en/ark/products/27206/intel-xeon-processor-3070-4m-cache-2-66-ghz-1066-mhz-fsb.html
                            frequencyRange = "";
                            voltageRange = "0.8500-1.500";
                            dvfs = false;
                        } else if (((MyPowerHost) host).getPowerModel() instanceof PowerModelLinearCelsiusV80AMDOpteron2218) {
                            PowerModelLinearCelsiusV80AMDOpteron2218 powerModel = (PowerModelLinearCelsiusV80AMDOpteron2218) ((MyPowerHost) host).getPowerModel();
                            frequencyRange = "";
                            voltageRange = "";
                            for(int j = 0; j < powerModel.getFrequencies().length; j++){
                                frequencyRange += powerModel.getFrequencies()[j] + ",";
                                voltageRange += powerModel.getVoltages()[j] + ",";
                            }


                            dvfs = true;
                        } else {
                            frequencyRange = "";
                            voltageRange = "";
                            dvfs = false;
                        }

                        String powermodel = "";
                        double[] utilizations = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
                        for(double utilization : utilizations){
                            powermodel += ((MyPowerHost) host).getPowerModel().getPower(utilization) + ",";
                        }

                        int index = 0;
                        for(HostStateHistoryEntry entry : ((MyPowerHost) host).getStateHistory()){
                            if(entry instanceof  MyPowerHostEntry){



                                String vmInfo = "";
                                double vmRam = 0.0;
                                for (Vm vm : ((MyPowerHostEntry) entry).getVms()){
                                    //vmInfo += vm.getNumberOfPes() + "," + vm.getMips() + "," + vm.getRam() + "," + vm.getBw() + ":";
                                    if (vm instanceof MyPowerVm){
                                        vmInfo += vm.getNumberOfPes() + "," + ((MyPowerVmEntry) vm.getStateHistory().get(index)).getAllocatedMips() + "," + ((MyPowerVmEntry) vm.getStateHistory().get(index)).getRequestedRam() + "," + ((MyPowerVmEntry) vm.getStateHistory().get(index)).getRequestedBw() + ":";
                                        
                                        vm_ram_map.put((Integer) vm.getId(), (Integer)vm.getRam());

                                        /*List<VmStateHistoryEntry> vmStateHistoryEntry = ((MyPowerVm) vm).getStateHistory();

                                        for(VmStateHistoryEntry vmentry : vmStateHistoryEntry){
                                            vmRam += ((MyPowerVmEntry) vmentry).getRamPower();

                                            ((MyPowerVmEntry) vmentry).
                                        }*/
                                        List<VmStateHistoryEntry> vmStateHistoryEntry = ((MyPowerVm) vm).getStateHistory();

                                        for(VmStateHistoryEntry vmentry : vmStateHistoryEntry){
                                            vmRam += ((MyPowerVmEntry) vmentry).getRamPower();

                                        

                                        }
                                    }
                                }

                                String peUtilizationInfo = "";
                                double cpuUtilization = 0.0; 
                                double allocated_mips = 0.0;
                                double total_mips = 0.0;
                                int freePes = 0;
                                for (PeEntry peEntry : ((MyPowerHostEntry) entry).getPeEntries()){
                                    if(peEntry.getAvailableMIPS() == peEntry.getMaxMIPS()){
                                        freePes++;
                                    }
                                    peUtilizationInfo += peEntry.getMaxMIPS() + "," + peEntry.getAvailableMIPS
                                    () + ":";

                                    total_mips += peEntry.getMaxMIPS();

                                    allocated_mips += (peEntry.getMaxMIPS() - peEntry.getAvailableMIPS());

                                    
                                }

                                /*f(entry.isActive()){*/

                                    cpuUtilization = allocated_mips / total_mips;


                                     Log.print(
                                        "Time: " + entry.getTime() + 
                                        ", Data Center ID: " + datacenter.getId() + 
                                        ", Data Center Name: " + datacenter.getName() +
                                        ", Host ID: " + host.getId() +
                                        ", Host Active: " + entry.isActive() +
                                        ", Total PEs: " + host.getNumberOfPes() +
                                        ", Free PEs: " + freePes +
                                        ", Total MIPS: " + host.getTotalMips() +
                                        ", Available MIPS: " + (host.getTotalMips() - entry.getAllocatedMips()) +
                                        ", PE Utilization: " + peUtilizationInfo +
                                        ", CPU Utilization: " + cpuUtilization+
                                        ", Total Energy CPU: " + ((MyPowerHost) host ).getPowerModel().getPower(cpuUtilization) +
                                        ", DVFS: " + dvfs +
                                        ", Frequency Range: " + frequencyRange +
                                        ", Voltage Range: " + voltageRange +
                                        //", RAM Model: " + ((MyPowerHost) host).getPowerModelRam() + 
                                        ", Total RAM: " + host.getRamProvisioner().getRam() +
                                        ", Allocated RAM: " + (((MyPowerHostEntry) entry).getAllocatedRam()) +
                                        //", RAM Utilization: " + ((MyPowerHostEntry) entry).getRamUtilization() +
                                        ", RAM Power: " + vmRam +
                                        //", RAM Energy: " + ramEnergy +
                                        ", Total Bandwidth: " + host.getBwProvisioner().getBw() +
                                        ", Available Bandwidth: " + (host.getBwProvisioner().getBw() - ((MyPowerHostEntry) entry).getAllocatedBw()) +
                                        ", Storage Size: " + ((MyPowerHost) host).getStorageSize() +
                                        ", Available Storage: " + (((MyPowerHost) host).getStorageSize() -((MyPowerHostEntry) entry).getAllocatedStorage()) +
                                        ", Power Model: " + powermodel +
                                        ", VM Info: " + vmInfo + "\n"
                                        //", Total Datacenter Energy: " + datacenterTotalPower
                                    );

                                //msg += entry.getTime() + ";" + datacenter.getName() + ";" +  host.getId() + ";host;" + entry.isActive() + ";" + host.getNumberOfPes() + ";" +  freePes + ";" + host.getTotalMips() + ";" + (host.getTotalMips() - entry.getAllocatedMips()) + ";" + peUtilizationInfo + ";" + host.getRamProvisioner().getRam() + ";" + (host.getRamProvisioner().getRam() - ((MyPowerHostEntry) entry).getAllocatedRam()) + ";" + host.getBwProvisioner().getBw() + ";" + (host.getBwProvisioner().getBw() - ((MyPowerHostEntry) entry).getAllocatedBw()) + ";" + powermodel + ";" + vmInfo + "\n"; // + ";" + frequencies + ";" + mipsPerFrequency + ";" + cpuIdlePerFrequency + ";" + cpuFullPerFrequency
                                msg += entry.getTime() + ";" + datacenter.getId() + ";" + datacenter.getName() + ";" +  host.getId() + ";host;" + entry.isActive() + ";" + host.getNumberOfPes() + ";" +  freePes + ";" + host.getTotalMips() + ";" + (host.getTotalMips() - entry.getAllocatedMips()) + ";" + peUtilizationInfo + ";" + dvfs + ";" + frequencyRange + ";" + voltageRange + ";" +  cpuUtilization + ";" + ((MyPowerHost) host ).getPowerModel().getPower(cpuUtilization) + ";" + host.getRamProvisioner().getRam() + ";"  + (((MyPowerHostEntry) entry).getAllocatedRam()) + ";" + vmRam + ";"+ host.getBwProvisioner().getBw() + ";" + (host.getBwProvisioner().getBw() - ((MyPowerHostEntry) entry).getAllocatedBw()) + ";" + ((MyPowerHost) host).getStorageSize() + ";" + (((MyPowerHost) host).getStorageSize() - ((MyPowerHostEntry) entry).getAllocatedStorage()) + ";" + powermodel + ";" + vmInfo + "\n"; // + ";" + frequencies + ";" + mipsPerFrequency + ";" + cpuIdlePerFrequency + ";" + cpuFullPerFrequency 

                                /*}*/

                            }else{
                                System.err.println("Err");
                            }
                            index++;
                        }

                    }
                }
                datacenter_number++;
            }

            File logDirectory = new File("/home/ubuntu/cloudsim_imp/logs/");
            if (!logDirectory.exists()) {
                if (logDirectory.mkdirs()) {
                    System.out.println("Created directory: " + logDirectory.getAbsolutePath());
                } else {
                    System.err.println("ERROR: Failed to create log directory!");
                }
            }





            System.out.println("Writing extracted data to log file...");
            try {
                
                FileWriter writer = new FileWriter("/home/ubuntu/cloudsim_imp/logs/" + fileName + ".csv");
                writer.write(msg);
                writer.close();
                System.out.println("Successfully wrote log data.");
            } catch (IOException e) {
                //throw new RuntimeException(e);
                System.err.println("Can't write to file");
                e.printStackTrace();
            }


            Log.print("--------------------------------------------");
            //List<Cloudlet> newList = broker.getCloudletReceivedList();

            //Log.printLine("---------------");

            CloudSim.stopSimulation();
            /*runMonitorThread.run = false;*/

            List<Cloudlet> newList = broker.getCloudletReceivedList();


            
            String fileName1 = "cloudlet_data_" + FileNameWords[0] + "_" + FileNameWords[1] + "_" + FileNameWords[2];
            java.io.File outputfile = new java.io.File("/home/ubuntu/cloudsim_imp/logs/" + fileName1 + ".csv");

            try {
                outputfile.createNewFile();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                System.err.println("Can't create file");
            }


            //String msg = "time;datacenter_name;host_id;type;active;number_of_pes;available_pes;mips;available_mips;utilization_per_pe;ram;available_ram;bw;available_bw;power_model;vms\n"; // frequencies;mips_per_frequency;cpu_idle_power_per_frequency;cpu_full_power_per_frequency;
            String output = "cl_id;length;cpu_time;cpu_util;ram_util;time_on_resource;submission_time;finish_time;vm;vm_ram;cloudlet_used_ram;\n"; 
            // frequencies;mips_per_frequency;cpu_idle_power_per_frequency;cpu_full_power_per_frequency;


            for(Cloudlet cloudlet : newList){
                output += cloudlet.getCloudletId() + ";" + cloudlet.getCloudletLength()+ ";" + cloudlet.getActualCPUTime() + ";" + cloudlet.getUtilizationOfCpu(cloudlet.getActualCPUTime()) + ";" + cloudlet.getUtilizationOfRam(cloudlet.getExecStartTime() - cloudlet.getFinishTime())+ ";" +cloudlet.getWallClockTime() + ";" + cloudlet.getSubmissionTime() + ";" + cloudlet.getFinishTime() + ";" + cloudlet.getVmId() +  ";" + vm_ram_map.get(cloudlet.getVmId())+ ";" + ((WorkloadAwareCloudlet) cloudlet).getActualRamUsed() + "\n";


                
            }
 


            System.out.println("Writing cloudlet data to file...");
            try {
                
                FileWriter writer = new FileWriter("/home/ubuntu/cloudsim_imp/logs/" + fileName1 + ".csv");
                writer.write(output);
                writer.close();
                System.out.println("Successfully wrote log data.");
            } catch (IOException e) {
                //throw new RuntimeException(e);
                System.err.println("Can't write to file");
                e.printStackTrace();
            }




            //printCloudletList(newList);

            //Log.printLine("Test finished!");
            Log.print("Simulation finished");
            Log.print("Ending Simulation");

            /*Log.printLine("--------------------------");
            for(Cloudlet cloudlet : cloudlets){
                Log.printLine(cloudlet.get);
            }*/
        }catch (Exception e){
            Log.printLine("Error:");
            Log.printLine(e.getMessage());
        }


    }
}
