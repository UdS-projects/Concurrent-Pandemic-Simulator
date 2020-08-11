package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.*;
import com.pseuco.np20.simulation.common.Context;
import com.pseuco.np20.simulation.common.Person;
import com.pseuco.np20.validator.Validator;

import java.util.*;
import java.util.stream.Collectors;

public class Patch extends Thread implements Context
{
    private final int id;
    private boolean firstRun;
    private final int ticksAllowed;
    private int currentTick;

    private final Scenario scenario;
    private final Validator validator;

    private final Rectangle patchGrid;
    private final Rectangle[] paddings;
    private final List<Monitor> monitors;

    private final List<Person> scenarioPopulation;
    private final List<Person> population;

    private final Map<String, List<Statistics>> statistics;
    private final List<TraceEntry> traces;

    public Patch(int pId, int pTicksAllowed, Scenario pScenario, Validator pValidator, Rectangle pPatchGrid, Rectangle[] pPaddings, List<Person> pScenarioPopulation)
    {
        monitors = new ArrayList<>();
        population = new ArrayList<>();
        statistics = new HashMap<>();
        traces = new LinkedList<>();
        firstRun = true;
        currentTick = 0;

        id = pId;
        ticksAllowed = pTicksAllowed;
        scenario = pScenario;
        validator = pValidator;
        patchGrid = pPatchGrid;
        paddings = pPaddings;
        scenarioPopulation = pScenarioPopulation;
    }

    public int getPatchId()
    {
        return id;
    }

    public Rectangle getPatchGrid()
    {
        return patchGrid;
    }

    public Rectangle[] getPaddings()
    {
        return paddings;
    }

    @Override
    public Rectangle getGrid()
    {
        return scenario.getGrid();
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

    public void addMonitor(Monitor m)
    {
        monitors.add(m);
    }

    // We take the people we need. No Data race cause shared allPopulation list is only read from.
    private void populate()
    {
        for(Person person : scenarioPopulation)
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

    private void synchronize()
    {

    }

    private void tick()
    {

    }

    @Override
    public void run() {
        if (firstRun) {
            populate();
            initStatistics();
            extendStatistics();
            extendTraces();
            synchronize();
            firstRun = false;
        }

        for (; currentTick < this.scenario.getTicks(); currentTick++) {
            validator.onPatchTick(currentTick, id);
            this.tick();
        }
    }
}