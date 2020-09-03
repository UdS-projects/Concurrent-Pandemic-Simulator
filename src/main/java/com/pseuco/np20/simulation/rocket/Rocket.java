package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.*;
import com.pseuco.np20.simulation.common.Person;
import com.pseuco.np20.simulation.common.Simulation;
import com.pseuco.np20.validator.InsufficientPaddingException;
import com.pseuco.np20.validator.Validator;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Your implementation shall go into this class.
 * And so it did.
 *
 * <p>
 * This class has to implement the <em>Simulation</em> interface.
 * </p>
 */
public class Rocket implements Simulation
{
    private final Scenario scenario;
    private final int padding;
    private final int ticksAllowed;
    private int patchCount;
    private final Validator validator;

    private final List<Person> scenarioPopulation;
    private final Map<Integer, Rectangle> patchRectangles;
    private Patch[] patches;
    private final Set<Monitor> monitors;

    private final Map<String, List<Statistics>> statistics;
    private final Map<String, List<RWStatistics>> statistics2;
    private final List<TraceEntryId> traces;
    private final List<TraceEntry> tracesFinal;

    /**
     * Constructs a rocket with the given parameters.
     *
     * <p>
     * You must not change the signature of this constructor.
     * </p>
     *
     * <p>
     * Throw an insufficient padding exception if and only if the padding is insufficient.
     * Hint: Depending on the parameters, some amount of padding is required even if one
     * only computes one tick concurrently. The padding is insufficient if the provided
     * padding is below this minimal required padding.
     * </p>
     *
     * @param pScenario The scenario to simulate.
     * @param pPadding The padding to be used.
     * @param pValidator The validator to be called.
     */
    public Rocket(Scenario pScenario, int pPadding, Validator pValidator) throws InsufficientPaddingException
    {
        scenarioPopulation = new ArrayList<>();
        patchRectangles = new HashMap<>();
        monitors = new HashSet<>();

        statistics = new HashMap<>();
        statistics2 = new HashMap<>();
        traces = new LinkedList<>();
        tracesFinal = new LinkedList<>();

        scenario = pScenario;
        padding = pPadding;
        validator = pValidator;

        ticksAllowed = calcTicksAllowed();
        if(ticksAllowed <= 0)
        {
            throw new InsufficientPaddingException(padding);
        }
//        System.out.println("Padding: " + padding);
//        System.out.println("Allowed Ticks: " + ticksAllowed);
    }


    private int calcTicksAllowed()
    {
        int ticks = 0;
        int spread = scenario.getParameters().getInfectionRadius();
        for(int i=1; spread <= padding; i++)
        {
            if(i != 1 && i % scenario.getParameters().getIncubationTime() == 1)
            {
                spread += scenario.getParameters().getInfectionRadius();
            }
            spread += 2;
            ticks = i;
        }
        return ticks - 1;
    }

    // We create the initial allPopulation list here so that everyone has a unique id
    private void populate()
    {
        int id = 0;
        for(PersonInfo personInfo : scenario.getPopulation())
        {
            scenarioPopulation.add(new Person(id, null, scenario.getParameters(), personInfo));
            id++;
        }
    }

    // We initialize the map we use to collect the necessary statistics
    private void initStatistics()
    {
        for(String queryKey : scenario.getQueries().keySet())
        {
            statistics.put(queryKey, new LinkedList<>());

            List<RWStatistics> list = new LinkedList<>();
            for(int i=0; i <= scenario.getTicks(); i++)
            {
                list.add(new RWStatistics());
            }
            statistics2.put(queryKey, list);
        }
    }

    private void initTraces()
    {
        if(scenario.getTrace())
        {
            for(int i=0; i <= scenario.getTicks(); i++)
            {
                traces.add(new TraceEntryId());
            }
        }
    }

    private void initThreads()
    {
        Iterator<Rectangle> patchesIterator = Utils.getPatches(scenario);
        for(int i=0; patchesIterator.hasNext(); i++)
        {
            patchRectangles.put(i, patchesIterator.next());
        }
        patches = new Patch[patchRectangles.size()];

        Thread[] helpers = new Thread[patchRectangles.size()];
        for(int i=0; i < patchRectangles.size(); i++)
        {
            int finalI = i;
            helpers[i] = new Thread()
            {
                public void run()
                {
                    Rectangle patch = patchRectangles.get(finalI);
                    Rectangle[] paddings = new Rectangle[8];

                    // We compute the up to 8 padding rectangles for every patch
                    // Hereby we make sure not to make paddings which exceed the scenario map in any direction
                    // If a padding is possible and required in one of the 8 possible directions,
                    // we save it in a clockwise order in the paddings array
                    if(patch.getTopLeft().getY() > 0)
                    {
                        Rectangle pTop = new Rectangle(
                                new XY(patch.getTopLeft().getX(), Math.max(0, patch.getTopLeft().getY() - padding)),
                                new XY(patch.getSize().getX(), Math.min(padding, patch.getTopLeft().getY())));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pTop, patch))
                        {
                            paddings[0] = pTop;
                        }
                    }
                    if(patch.getTopLeft().getX() > 0)
                    {
                        Rectangle pLeft = new Rectangle(
                                new XY(Math.max(0, patch.getTopLeft().getX() - padding), patch.getTopLeft().getY()),
                                new XY(Math.min(padding, patch.getTopLeft().getX()), patch.getSize().getY()));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pLeft, patch))
                        {
                            paddings[6] = pLeft;
                        }
                    }
                    if(patch.getTopLeft().getY() > 0 && patch.getTopLeft().getX() > 0)
                    {
                        Rectangle pTopLeft = new Rectangle(
                                new XY(Math.max(0, patch.getTopLeft().getX() - padding), Math.max(0, patch.getTopLeft().getY() - padding)),
                                new XY(Math.min(padding, patch.getTopLeft().getX()) , Math.min(padding, patch.getTopLeft().getY())));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pTopLeft, patch))
                        {
                            paddings[7] = pTopLeft;
                        }
                    }
                    if(patch.getBottomRight().getX() < scenario.getGrid().getBottomRight().getX())
                    {
                        Rectangle pRight = new Rectangle(
                                new XY(patch.getBottomRight().getX(), patch.getTopLeft().getY()),
                                new XY(Math.min(padding, scenario.getGrid().getBottomRight().getX() - patch.getBottomRight().getX()), patch.getSize().getY()));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pRight, patch))
                        {
                            paddings[2] = pRight;
                        }
                    }
                    if(patch.getTopLeft().getY() > 0 && patch.getBottomRight().getX() < scenario.getGrid().getBottomRight().getX())
                    {
                        Rectangle pTopRight = new Rectangle(
                                new XY(patch.getBottomRight().getX(), Math.max(0, patch.getTopLeft().getY() - padding)),
                                new XY(Math.min(padding, scenario.getGrid().getBottomRight().getX() - patch.getBottomRight().getX()), Math.min(padding, patch.getTopLeft().getY())));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pTopRight, patch))
                        {
                            paddings[1] = pTopRight;
                        }
                    }
                    if(patch.getBottomRight().getY() < scenario.getGrid().getBottomRight().getY())
                    {
                        Rectangle pBottom = new Rectangle(
                                new XY(patch.getTopLeft().getX(), patch.getBottomRight().getY()),
                                new XY(patch.getSize().getX(), Math.min(padding, scenario.getGrid().getBottomRight().getY() - patch.getBottomRight().getY())));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pBottom, patch))
                        {
                            paddings[4] = pBottom;
                        }
                    }
                    if(patch.getTopLeft().getX() > 0 && patch.getBottomRight().getY() < scenario.getGrid().getBottomRight().getY())
                    {
                        Rectangle pBottomLeft = new Rectangle(
                                new XY(Math.max(0, patch.getTopLeft().getX() - padding), patch.getBottomRight().getY()),
                                new XY(Math.min(padding, patch.getTopLeft().getX()), Math.min(padding, scenario.getGrid().getBottomRight().getY() - patch.getBottomRight().getY())));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pBottomLeft, patch))
                        {
                            paddings[5] = pBottomLeft;
                        }
                    }
                    if(patch.getBottomRight().getX() < scenario.getGrid().getBottomRight().getX() && patch.getBottomRight().getY() < scenario.getGrid().getBottomRight().getY())
                    {
                        Rectangle pBottomRight = new Rectangle(
                                new XY(patch.getBottomRight().getX(), patch.getBottomRight().getY()),
                                new XY(Math.min(padding, scenario.getGrid().getBottomRight().getX() - patch.getBottomRight().getX()), Math.min(padding, scenario.getGrid().getBottomRight().getY() - patch.getBottomRight().getY())));
                        if(com.pseuco.np20.simulation.common.Utils.mayPropagateFrom(scenario, pBottomRight, patch))
                        {
                            paddings[3] = pBottomRight;
                        }
                    }

                    Patch p = new Patch(finalI, ticksAllowed, scenario, validator, patch, paddings, scenarioPopulation);
                    patches[finalI] = p;
                }
            };
            helpers[i].start();
        }

        for(int i=0; i < patchRectangles.size(); i++)
        {
            try
            {
                helpers[i].join();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        patchCount = patchRectangles.size();
        for(int i=0; i < patchCount; i++)
        {
            Patch pi = patches[i];
            for(int j=i+1; j < patchCount; j++)
            {
                Patch pj = patches[j];
                for(int k=0; k < 8; k++)
                {
                    //System.out.println(k + " " + pi.getPaddings()[k]);
                    // If one of the paddings of patch i overlaps with the patchGrid of patch j
                    // Then they require a monitor for synchronisation
                    if(pi.getPaddings()[k] != null)
                    {
                        if(pj.getPatchGrid().overlaps(pi.getPaddings()[k]))
                        {
                            Monitor m = new Monitor(pi, i, pj, j);
                            m.setIntersection(j, pj.getPatchGrid().intersect(pi.getPaddings()[k]));
                            int w = (k + 4) % 8;
                            m.setIntersection(i, pi.getPatchGrid().intersect(pj.getPaddings()[w]));

                            if(monitors.add(m))
                            {
                                pi.addMonitor(m);
                                pj.addMonitor(m);
                            }
                        }
                    }
                }
            }
        }
//        System.out.println("Patches: " + patchCount);
//        System.out.println("Monitors: " + monitors.size());
    }

    @Override
    public Output getOutput()
    {
        return new Output(scenario, tracesFinal, statistics);
    }

    @Override
    public void run()
    {
        populate();
        Thread initStatsThread = new Thread()
        {
            public void run()
            {
                initStatistics();
            }
        };
        initStatsThread.start();
        initTraces();
        initThreads();

        for(int i=0; i < patchCount; i++)
        {
            patches[i].start();
        }

        try
        {
            initStatsThread.join();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        for(int i=0; i < patchCount; i++)
        {
            try
            {
                patches[i].join();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }

            for(String queryKey : patches[i].getStatistics().keySet())
            {
                List<Statistics> list = patches[i].getStatistics().get(queryKey);
                for(int j=0; j < list.size(); j++)
                {
                    statistics2.get(queryKey).get(j).addStats(list.get(j));
                }
            }

            if(scenario.getTrace())
            {
                for(int j=0; j < traces.size(); j++)
                {
                    traces.get(j).getPopulation().addAll(patches[i].getTraces().get(j).getPopulation());
                }
            }
        }

        // Below is a duplicate person finder
//        int tSize = traces.get(scenario.getTicks()).getPopulation().size();
//        System.out.println("Rocket last trace size " + tSize);
//        for(int i=0; i < tSize; i++)
//        {
//            for(int j=0; j < tSize; j++)
//            {
//                if(i!=j && traces.get(scenario.getTicks()).getPopulation().get(i).getInfo().getName().equals(traces.get(scenario.getTicks()).getPopulation().get(j).getInfo().getName()))
//                {
//                    System.out.println("dup found: at " + i + " " + traces.get(scenario.getTicks()).getPopulation().get(i).getInfo().getName() + " and at " + j + " " + traces.get(scenario.getTicks()).getPopulation().get(j).getInfo().getName());
//                }
//            }
//        }
        
        statistics.replaceAll( (k, v) -> statistics2.get(k).stream().map(RWStatistics::getStatistics).collect(Collectors.toList()) );

        if(scenario.getTrace())
        {
            for(int i=0; i < traces.size(); i++)
            {
                traces.get(i).getPopulation().sort( (PersonInfoId p1, PersonInfoId p2) -> p1.getId() - p2.getId() );
                tracesFinal.add(new TraceEntry(traces.get(i).getPopulation().stream().map(PersonInfoId::getInfo).collect(Collectors.toList())));
            }
        }
    }
}