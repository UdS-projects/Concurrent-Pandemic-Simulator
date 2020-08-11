package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.*;
import com.pseuco.np20.simulation.common.Person;
import com.pseuco.np20.simulation.common.Simulation;
import com.pseuco.np20.validator.InsufficientPaddingException;
import com.pseuco.np20.validator.Validator;

import java.util.*;


/**
 * Your implementation shall go into this class.
 *
 * <p>
 * This class has to implement the <em>Simulation</em> interface.
 * </p>
 */
public class Rocket implements Simulation
{
    private final Scenario scenario;
    private final List<Person> allPopulation;
    private final List<Rectangle> patches;

    private final int padding;
    private final int ticksAllowed;

    private final Map<String, List<Statistics>> statistics;
    private final List<TraceEntry> traces;

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
        allPopulation = new ArrayList<>();
        patches = new LinkedList<>();
        statistics = new HashMap<>();
        traces = new LinkedList<>();

        scenario = pScenario;
        padding = pPadding;

        ticksAllowed = 5; //TODO
        if(ticksAllowed <= 0)
        {
            throw new InsufficientPaddingException(padding);
        }

        populate();
        initStatistics();
        initThreads();
    }

    // We create the initial allPopulation list here so that everyone has a unique id.
    private void populate()
    {
        int id = 0;
        for(PersonInfo personInfo : scenario.getPopulation())
        {
            allPopulation.add(new Person(id, null, scenario.getParameters(), personInfo));
            id++;
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

    private void initThreads()
    {
        Iterator<Rectangle> patchesIterator = Utils.getPatches(scenario);
        int patchesNum = 0;
        while(patchesIterator.hasNext())
        {
            Rectangle patch = patchesIterator.next();
            Rectangle[] paddings = new Rectangle[8];

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
            }
            if(patch.getTopLeft().getY() > 0 && patch.getTopLeft().getX() > 0)
            {
                Rectangle pTopLeft = new Rectangle(
                        new XY(Math.max(0, patch.getTopLeft().getX() - padding), Math.max(0, patch.getTopLeft().getY() - padding)),
                        new XY(Math.min(padding, patch.getTopLeft().getX()) , Math.min(padding, patch.getTopLeft().getY())));
            }
            if(patch.getBottomRight().getX() < scenario.getGrid().getBottomRight().getX())
            {
                Rectangle pRight = new Rectangle(
                        new XY(patch.getBottomRight().getX(), patch.getTopLeft().getY()),
                        new XY(Math.min(padding, scenario.getGrid().getBottomRight().getX() - patch.getBottomRight().getX()), patch.getSize().getY()));
            }
            if(patch.getTopLeft().getY() > 0 && patch.getBottomRight().getX() < scenario.getGrid().getBottomRight().getX())
            {
                Rectangle pTopRight = new Rectangle(
                        new XY(patch.getBottomRight().getX(), Math.max(0, patch.getTopLeft().getY() - padding)),
                        new XY(Math.min(padding, scenario.getGrid().getBottomRight().getX() - patch.getBottomRight().getX()), Math.min(padding, patch.getTopLeft().getY())));
            }
            if(patch.getBottomRight().getY() < scenario.getGrid().getBottomRight().getY())
            {
                Rectangle pBottom = new Rectangle(
                        new XY(patch.getTopLeft().getX(), patch.getBottomRight().getY()),
                        new XY(patch.getSize().getX(), Math.min(padding, scenario.getGrid().getBottomRight().getY() - patch.getBottomRight().getY())));
            }
            if(patch.getTopLeft().getX() > 0 && patch.getBottomRight().getY() < scenario.getGrid().getBottomRight().getY())
            {
                Rectangle pBottomLeft = new Rectangle(
                        new XY(Math.max(0, patch.getTopLeft().getX() - padding), patch.getBottomRight().getY()),
                        new XY(Math.min(padding, patch.getTopLeft().getX()), Math.min(padding, scenario.getGrid().getBottomRight().getY() - patch.getBottomRight().getY())));
            }
            if(patch.getBottomRight().getX() < scenario.getGrid().getBottomRight().getX() && patch.getBottomRight().getY() < scenario.getGrid().getBottomRight().getY())
            {
                Rectangle pBottomRight = new Rectangle(
                        new XY(patch.getBottomRight().getX(), patch.getBottomRight().getY()),
                        new XY(Math.min(padding, scenario.getGrid().getBottomRight().getX() - patch.getBottomRight().getX()), Math.min(padding, scenario.getGrid().getBottomRight().getY() - patch.getBottomRight().getY())));
            }

            patches.add(patchesIterator.next());
            patchesNum++;
        }


    }

    @Override
    public Output getOutput()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void run()
    {
        throw new RuntimeException("not implemented");
    }
}