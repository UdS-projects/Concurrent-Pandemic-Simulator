package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.Statistics;

public class RWStatistics
{
    private long susceptible;
    private long infected;
    private long infectious;
    private long recovered;

    public RWStatistics()
    {
        susceptible = 0;
        infected = 0;
        infectious = 0;
        recovered = 0;
    }

    public RWStatistics(long pSusceptible, long pInfected, long pInfectious, long pRecovered)
    {
        susceptible = pSusceptible;
        infected = pInfected;
        infectious = pInfectious;
        recovered = pRecovered;
    }

    public long getSusceptible()
    {
        return susceptible;
    }

    public long getInfected()
    {
        return infected;
    }

    public long getInfectious()
    {
        return infectious;
    }

    public long getRecovered()
    {
        return recovered;
    }

    public void setSusceptible(long pSusceptible)
    {
        susceptible = susceptible;
    }

    public void setInfected(long pInfected)
    {
        infected = infected;
    }

    public void setInfectious(long pInfectious)
    {
        infectious = infectious;
    }

    public void setRecovered(long pRecovered)
    {
        recovered = recovered;
    }

    public void addStats(Statistics stats)
    {
        susceptible += stats.getSusceptible();
        infected += stats.getInfected();
        infectious += stats.getInfectious();
        recovered += stats.getRecovered();
    }

    public Statistics getStatistics()
    {
        return new Statistics(susceptible, infected, infectious, recovered);
    }
}