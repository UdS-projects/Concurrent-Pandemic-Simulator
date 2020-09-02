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
    private final List<TraceEntryId> traces;

    public Patch(int pId, int pTicksAllowed, Scenario pScenario, Validator pValidator, Rectangle pPatchGrid, Rectangle[] pPaddings, List<Person> pScenarioPopulation)
    {
        monitors = new ArrayList<>();
        population = new ArrayList<>();
        statistics = new HashMap<>();
        traces = new LinkedList<>();
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

    public Map<String, List<Statistics>> getStatistics()
    {
        return statistics;
    }

    public List<TraceEntryId> getTraces()
    {
        return traces;
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
        List<Person> list = population.stream().filter( (Person person) -> patchGrid.contains(person.getPosition()) ).collect(Collectors.toList());
        for(Map.Entry<String, Query> entry : scenario.getQueries().entrySet()) {
            final Query query = entry.getValue();
            statistics.get(entry.getKey()).add(new Statistics(
                    list.stream().filter(
                            (Person person) -> person.isSusceptible() && query.getArea().contains(person.getPosition()) ).count(),
                    list.stream().filter(
                            (Person person) -> person.isInfected() && query.getArea().contains(person.getPosition()) ).count(),
                    list.stream().filter(
                            (Person person) -> person.isInfectious() && query.getArea().contains(person.getPosition()) ).count(),
                    list.stream().filter(
                            (Person person) -> person.isRecovered() && query.getArea().contains(person.getPosition()) ).count()
            ));
        }
    }

    // We extend the the trace for the current tick
    private void extendTraces()
    {
        if(scenario.getTrace())
        {
            traces.add(new TraceEntryId(population.stream()
                    .filter( (Person person) -> patchGrid.contains(person.getPosition()) )
                    .map( (Person p) -> new PersonInfoId(p) ).collect(Collectors.toList())));

//            if(currentTick == scenario.getTicks() - 1)
//            {
//                System.out.println("Thread " + id + " tick " + currentTick + " trace popNum " + traces.get(currentTick).getPopulation().size());
//                System.out.println("Thread " + id + " pop0 in last trace " + traces.get(600).getPopulation().get(0).toString());
//            }
        }
    }

    private void synchronize() throws InterruptedException
    {
        // Delete people in paddings
        population.removeIf(person -> !(patchGrid.contains(person.getPosition())));

        // Write into the monitors in parallel
        // This is ok because those writer threads ONLY read from non-locked data
        Thread[] writers = new Thread[monitors.size()];
        for(int i=0; i < monitors.size(); i++)
        {
            int finalI = i;
            writers[i] = new Thread() {
                public void run()
                {
                    Rectangle intersection = monitors.get(finalI).getIntersection(id);
                    List<Person> people = population.stream().filter(
                            (Person person) -> intersection.contains(person.getPosition())
                    ).collect(Collectors.toList());
                    try
                    {
                        monitors.get(finalI).setPopulation(id, people);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            writers[i].start();
        }
        for(int i=0; i < monitors.size(); i++)
        {
            writers[i].join();
        }

        // Read from the monitors in parallel
        // This is ok because writing the people into local memory will happen sequentially afterwards
        Thread[] readers = new Thread[monitors.size()];
        List[] results = new List[monitors.size()];
        for(int i=0; i < monitors.size(); i++)
        {
            //System.out.println("hi, my name is " + Thread.currentThread() + " mon size: " + monitors.size());
            int finalI = i;
            readers[i] = new Thread()
            {
                public void run()
                {
                    try
                    {
                        List<Person> l = monitors.get(finalI).getPopulation(id);
                        l.sort( (Person p1, Person p2) -> p1.getId() - p2.getId() );
                        results[finalI] = l;
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            readers[i].start();
        }
        for(int i=0; i < monitors.size(); i++)
        {
            readers[i].join();
        }

        //System.out.println("thread " + id + " res " + results[0]);

        // Add the read persons to local storage
        for(List l : results)
        {
            for(Object person : l)
            {
                //System.out.println("person: " + ((Person)person).toString());
                population.add((Person)person);
            }
        }

        population.sort( (Person p1, Person p2) -> p1.getId() - p2.getId() );
    }

    private void tick()
    {
        if(currentTick % ticksAllowed == 0)
        {
            try
            {
                synchronize();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        for(Person person : population)
        {
            validator.onPersonTick(currentTick, id, person.getId());
            person.tick();
        }

        // Bust the ghosts of all persons
        population.stream().forEach(Person::bustGhost);

        // Now compute how the infection spreads between the population
        for(int i = 0; i < population.size(); i++)
        {
            for(int j = i + 1; j < population.size(); j++)
            {
                final Person iPerson = population.get(i);
                final Person jPerson = population.get(j);
                final XY iPosition = iPerson.getPosition();
                final XY jPosition = jPerson.getPosition();
                final int deltaX = Math.abs(iPosition.getX() - jPosition.getX());
                final int deltaY = Math.abs(iPosition.getY() - jPosition.getY());
                final int distance = deltaX + deltaY;
                if(distance <= scenario.getParameters().getInfectionRadius())
                {
                    if(iPerson.isInfectious() && iPerson.isCoughing() && jPerson.isBreathing())
                    {
                        jPerson.infect();
                    }
                    if(jPerson.isInfectious() && jPerson.isCoughing() && iPerson.isBreathing())
                    {
                        iPerson.infect();
                    }
                }
            }
        }

        // We need to collect statistics and extend the recorded trace
        extendStatistics();
        extendTraces();
    }

    @Override
    public void run()
    {
        populate();
        initStatistics();
        extendStatistics();
        extendTraces();

        for(; currentTick < this.scenario.getTicks(); currentTick++)
        {
            //System.out.println("t" + id + " tick " + currentTick);
            validator.onPatchTick(currentTick, id);
            tick();
        }
    }
}