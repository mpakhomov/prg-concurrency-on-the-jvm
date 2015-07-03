package com.agiledeveloper.pcj.typed;

/**
 * @author mpakhomov
 * @since: 7/3/2015
 */
public interface EnergySource {
    long getUnitsAvailable();
    long getUsageCount();
    void useEnergy(final long units);
}
