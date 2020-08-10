package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.Rectangle;
import com.pseuco.np20.simulation.common.Context;
import com.pseuco.np20.simulation.common.Person;

import java.util.List;

public class Patch extends Thread implements Context
{
    Rectangle contextGrid;
    List<Rectangle> obstacles;



    @Override
    public Rectangle getGrid() {
        return contextGrid;
    }

    @Override
    public List<Rectangle> getObstacles() {
        return obstacles;
    }

    @Override
    public List<Person> getPopulation()
    {
        //combine both lists
        return null;
    }
}
