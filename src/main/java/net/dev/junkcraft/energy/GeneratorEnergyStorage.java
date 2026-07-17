package net.dev.junkcraft.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Energy buffer that only allows external extraction (e.g. by cables/pipes) -
 * energy is added internally via {@link #addEnergy(int)} while the generator burns fuel.
 */
public class GeneratorEnergyStorage extends EnergyStorage {
    public GeneratorEnergyStorage(int capacity, int maxExtract) {
        super(capacity, 0, maxExtract, 0);
    }

    public int addEnergy(int toAdd) {
        int energyReceived = Math.max(0, Math.min(this.capacity - this.energy, toAdd));
        this.energy += energyReceived;
        return energyReceived;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(this.capacity, energy));
    }
}
