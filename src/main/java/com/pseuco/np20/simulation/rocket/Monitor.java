package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.Rectangle;
import com.pseuco.np20.simulation.common.Person;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Monitor
{
    // Here data between two patch-thread regarding one padding will be exchanged
    // This will happened via two arrays

    private final Patch patch1;
    private final Patch patch2;
    private final int patchId1;
    private final int patchId2;

    private Rectangle intersection1;
    private Rectangle intersection2;

    private List<Person> population1;
    private List<Person> population2;

    public Monitor(Patch pPatch1, int pPatchId1, Patch pPatch2, int pPatchId2)
    {
        patch1 = pPatch1;
        patchId1 = pPatchId1;
        patch2 = pPatch2;
        patchId2 = pPatchId2;
        population1 = new ArrayList<>();
        population2 = new ArrayList<>();
    }

    public int getPatchId1()
    {
        return patchId1;
    }
    public int getPatchId2()
    {
        return patchId2;
    }

    public Rectangle getIntersection(int id)
    {
        if(id == patchId1)
        {
            return intersection1;
        }
        else if(id == patchId2)
        {
            return intersection2;
        }

        return null;
    }

    public void setIntersection(int id, Rectangle i)
    {
        if(id == patchId1)
        {
            intersection1 = i;
        }
        else if(id == patchId2)
        {
            intersection2 = i;
        }
    }

    public synchronized Person[] getPopulation(int id)
    {
        if(id == patchId1)
        {
            while(population2.isEmpty())
            {

            }
        }
        else if(id == patchId2)
        {
            while(population1.isEmpty())
            {

            }
        }

        return null;
    }

    public synchronized void setPopulation(int id, List<Person> people) throws InterruptedException
    {
        if(id == patchId1)
        {
            while(!population1.isEmpty())
            {
                wait();
            }
            Iterator<Person> iter = people.iterator();
            while(iter.hasNext())
            {
                population1.add(iter.next().clone(patch2));
            }
        }
        else if(id == patchId2)
        {
            while(!population2.isEmpty())
            {
                wait();
            }
            Iterator<Person> iter = people.iterator();
            while(iter.hasNext())
            {
                population2.add(iter.next().clone(patch1));
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Monitor)
        {
            return (patchId1 == ((Monitor)o).getPatchId1() && patchId2 == ((Monitor)o).getPatchId2())
                    || (patchId1 == ((Monitor)o).getPatchId2() && patchId2 == ((Monitor)o).getPatchId1());
        }
        else
        {
            return false;
        }
    }
}