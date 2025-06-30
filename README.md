# IsoLink

**IsoLink: A Context-Aware Task Offloading Framework for Delay-Sensitive Applications in Satellite Mist Computing**

IsoLink is a research-driven enhancement of the SatEdgeSim simulator. It introduces an intelligent, latency-conscious task orchestration algorithm optimized for mist-layer processing in satellite-based edge computing environments.

IsoLink can also be seen is as a task offloading algorithm designed for satellite-enabled edge computing environments. It relies on task classification and context-aware decision-making to enhance performance and reduce latency.

This project builds upon the foundations of [SatEdgeSim](https://github.com/wjy491156866/SatEdgeSim), integrating a new algorithm designed to reduce end-to-end delay for real-time applications through adaptive, context-sensitive task offloading at the mist level.

## 🔍 Objective

To reduce latency and improve task success rates in satellite edge computing scenarios, especially for time-critical applications like AR/VR, autonomous vehicles, and e-health.

## 🚀 Key Features

- **IsoLink Algorithm**: A novel, dynamic task offloading scheme based on task profiling and classification.
- **Mist-Level Optimization**: Offloading decisions are made at the mist layer to reduce propagation delay.
- **Context-Aware Task Classification**: Tasks are categorized into real-time, latency-tolerant, and heavy classes.
- **Metric Normalization**: Delay and execution parameters are normalized and weighted based on task urgency.
- **VM Selection Strategy**: Selection of the best Virtual Machine based on a multi-metric cost score.

## 🧠 IsoLink Logic Overview

1. **Application Profiling**: Match incoming tasks to predefined application profiles (via XML).
2. **Task Classification**: Based on deadline, required cores, container size, and task length.
3. **Weight Assignment**:
   - Real-time → High weight on delay
   - Latency-tolerant → Balanced
   - Heavy → High weight on execution
4. **Metric Normalization**: Standardize delay and execution cost to compare across VMs.
5. **Scoring & VM Selection**: Select VM with the lowest score for offloading.

## 📊 Performance Improvements

Simulation results show that IsoLink:
- Reduces average end-to-end delay significantly.
- Maintains or improves energy efficiency.
- Achieves over 80% task success rate, outperforming Round_Robin, Weight_Greedy, and Random_VM algorithms.

## 🔧 Integration

IsoLink is integrated within the SatEdgeSim environment as:
- A new task orchestration module in `TasksOrchestration`.
- XML-based task classification engine.
- Modified VM selection mechanism in the mist layer.
- Enhanced simulation reporting for delay, energy, and success metrics.

## 📁 Project Structure

- `src/`: Source code for all modules
- `applications.xml`: Application profile definitions
- `IsoLink.java`: Core orchestration logic
- `utils/`: Helper functions (e.g., task classification, normalization)

## P.S.
- Replace "SatEdgeSim-master\SatEdgeSim\settings\locationflie\" with the content inside "locationflie.zip" after downloading it from:
https://mega.nz/file/iNcXFSBI#jtW0ne8hfECSWeuKa9AMptrno1udResmvT6DVRICV6o
- Replace "SatEdgeSim-master\bin\settings\locationflie\" with the content inside "locationflie.zip" after downloading it from:
https://mega.nz/file/iNcXFSBI#jtW0ne8hfECSWeuKa9AMptrno1udResmvT6DVRICV6o
