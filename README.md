# CloudSim Custom Implementation for Multi-Resource Power Modeling

This repository contains a Framework consistin of a customized CloudSim implementation and Python-based analysis developed as part of the master's thesis:

> **Towards Energy Efficient Cloud Data Centers: A Multi-Resource Power Model Framework**  
> Awet Teklemariam Ghebrekidan — Oslo Metropolitan University, Spring 2025

---

## Project Overview

This research extends the CloudSim simulation toolkit with support for **multi-resource power modeling**, enabling energy-efficient strategies across:

- **CPU**
- **RAM**
- **Network Bandwidth**
- **Disk Storage**

The framework introduces power models and VM placement logic that collectively reduce total datacenter energy usage, validated through simulations using the **GWA-T-12 Bitbrains** dataset.

---

## Repository Structure

```text
.
├── analysis/                             # Python notebooks for log analysis and plotting
├── logs/                                 # Output CSV logs from simulations
├── modules/
│   ├── cloudsim/                         # CloudSim core
│   │   └── src/main/java/org/cloudbus/cloudsim/
│   │       └── ...                       # Custom framework code (e.g., power models, brokers, hosts)
│   ├── cloudsim-examples/               # Example simulations
│       └── src/main/java/org/cloudbus/cloudsim/examples/
│           └── ...                       # Main experiment entry points
├── pom.xml                               # Maven parent build file
├── .gitignore
├── LICENSE
└── README.md 

```




## Requirements

### Simulation Framework

- **Java 8+**
- **Apache Maven** (use compatible version with you Java SDK)


## Getting Started

### Clone the Repository

```git clone https://github.com/Awet9I/cloudsim-custom-implementation.git ``` <br />

```cd cloudsim-custom-implementation```


### Build the Project with Maven

From the root ```modules/``` folder:

```cd modules/``` <br />

```mvn clean install```

### Run the Simulation
```mvn exec:java -pl cloudsim-examples \ -Dexec.mainClass="org.cloudbus.cloudsim.examples.Experiment"```
