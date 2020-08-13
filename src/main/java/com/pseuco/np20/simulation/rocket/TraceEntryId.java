package com.pseuco.np20.simulation.rocket;

import java.util.LinkedList;
import java.util.List;

public class TraceEntryId
{
    private final List<PersonInfoId> population;

    public TraceEntryId()
    {
        population = new LinkedList<>();
    }

    public TraceEntryId(List<PersonInfoId> pPopulation)
    {
        population = pPopulation;
    }

    public List<PersonInfoId> getPopulation()
    {
        return population;
    }
}
