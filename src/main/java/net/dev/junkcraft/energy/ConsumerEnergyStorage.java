package net.dev.junkcraft.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Energy buffer that only allows external insertion (e.g. by cables/pipes) -
 * energy is removed internally via {@link #consumeEnergy(int)} while the machine works.
 */
public class ConsumerEnergyStorage extends EnergyStorage {
    public ConsumerEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0, 0);
    }

    public boolean consumeEnergy(int amount) {
        if (this.energy < amount) {
            return false;
        }
        this.energy -= amount;
        return true;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(this.capacity, energy));
    }
}
