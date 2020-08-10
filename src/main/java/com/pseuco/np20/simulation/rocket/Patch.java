package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.*;
import com.pseuco.np20.simulation.common.Context;
import com.pseuco.np20.simulation.common.Person;

import java.util.*;
import java.util.stream.Collectors;

public class Patch extends Thread implements Context
{
    private boolean firstRun;

    private final Scenario scenario;

    private final Rectangle patchGrid;
    private final Rectangle allGrid;

    private final List<Person> allPopulation;
    private final List<Person> population;

    private final Map<String, List<Statistics>> statistics;
    private final List<TraceEntry> traces;

    public Patch(Scenario pScenario, List<Person> pAllPopulation, Rectangle pPatchGrid)
    {
        population = new ArrayList<>();
        statistics = new HashMap<>();
        traces = new LinkedList<>();

        firstRun = true;

        scenario = pScenario;
        allPopulation = pAllPopulation;
        patchGrid = pPatchGrid;
        allGrid = null;
    }

    // We take the people we need. No Data race cause shared allPopulation list is only read from.
    private void populate()
    {
        for(Person person : allPopulation)
        {
            if(patchGrid.contains(person.getPosition()));
            {
                population.add(person.clone(this));
            }
        }
    }

    // We initialize the map we use to collect the necessary statistics
    private void initStatistics()
    {
        for(String queryKey : scenario.getQueries().keySet())
        {
            statistics.put(queryKey, new LinkedList<>());
        }
    }

    // We collect the statistics for our patch for the current tick
    // At the end, Rocket will add those up
    private void extendStatistics()
    {
        for(Map.Entry<String, Query> entry : scenario.getQueries().entrySet()) {
            final Query query = entry.getValue();
            statistics.get(entry.getKey()).add(new Statistics(
                    population.stream().filter(
                            (Person person) -> person.isSusceptible() && query.getArea().contains(person.getPosition())
                    ).count(),
                    population.stream().filter(
                            (Person person) -> person.isInfected() && query.getArea().contains(person.getPosition())
                    ).count(),
                    population.stream().filter(
                            (Person person) -> person.isInfectious() && query.getArea().contains(person.getPosition())
                    ).count(),
                    population.stream().filter(
                            (Person person) -> person.isRecovered() && query.getArea().contains(person.getPosition())
                    ).count()
            ));
        }
    }

    // We extend the the trace for the current tick
    private void extendTraces()
    {
        if(scenario.getTrace())
        {
            traces.add(new TraceEntry(population.stream().map(Person::getInfo).collect(Collectors.toList())));
        }
    }

    @Override
    public Rectangle getGrid()
    {
        return allGrid;
    }

    @Override
    public List<Rectangle> getObstacles()
    {
        return scenario.getObstacles();
    }

    @Override
    public List<Person> getPopulation()
    {
        return population;
    }

    @Override
    public void run()
    {
        if(firstRun)
        {
            populate();
            initStatistics();
            firstRun = false;
        }
    }
}
