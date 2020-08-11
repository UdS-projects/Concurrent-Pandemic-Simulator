package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.Rectangle;

import java.util.Set;

public class Monitor
{
    // Here data between two patch-thread regarding one padding will be exchanged
    // This will happened via two arrays

    private final int patch1;
    private final int patch2;

    private Rectangle intersection1;
    private Rectangle intersection2;

    public Monitor(int pPatch1, int pPatch2)
    {
        patch1 = pPatch1;
        patch2 = pPatch2;
    }

    public int getPatch1()
    {
        return patch1;
    }
    public int getPatch2()
    {
        return patch2;
    }

    public Rectangle getIntersection(int id)
    {
        if(id == patch1)
        {
            return intersection1;
        }
        else if(id == patch2)
        {
            return intersection2;
        }

        return null;
    }

    public void setIntersection(int id, Rectangle i)
    {
        if(id == patch1)
        {
            intersection1 = i;
        }
        else if(id == patch2)
        {
            intersection2 = i;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Monitor)
        {
            return (patch1 == ((Monitor)o).getPatch1() && patch2 == ((Monitor)o).getPatch2())
                    || (patch1 == ((Monitor)o).getPatch2() && patch2 == ((Monitor)o).getPatch1());
        }
        else
        {
            return false;
        }
    }
}