package org.cloudbus.cloudsim.MyChange;

import org.cloudbus.cloudsim.Cloudlet;

public class MemoryAccessEstimator {

    private static final double DEFAULT_ACCESS_PER_INSTRUCTION = 1.8;
    private static final double DEFAULT_READ_RATIO = 0.8;
    private static final int BITS_PER_ACCESS = 64;

    public static class MemoryActivity {
        public final double readBitsPerSecond;
        public final double writeBitsPerSecond;

        public MemoryActivity(double r, double w) {
            readBitsPerSecond = r;
            writeBitsPerSecond = w;
        }
    }

    public static MemoryActivity estimateBitRates(Cloudlet cloudlet, double durationSeconds) {
        long instructions = cloudlet.getCloudletLength() * 1_000_000L;
        double accesses = instructions * DEFAULT_ACCESS_PER_INSTRUCTION;
        double read = accesses * DEFAULT_READ_RATIO * BITS_PER_ACCESS;
        double write = accesses * (1 - DEFAULT_READ_RATIO) * BITS_PER_ACCESS;

        return new MemoryActivity(read / durationSeconds, write / durationSeconds);
    }
}
