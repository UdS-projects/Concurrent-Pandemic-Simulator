package com.pseuco.np20.simulation.rocket;

public class PatchWait
{
    // The below booleans allow as to not miss out on a notify()
    // which might arrive shortly before we wait()
    // And so no notify() will "miss" us
    private boolean canWriteB;
    private boolean canReadB;

    public synchronized boolean getCanWriteB()
    {
        if(canWriteB)
        {
            canWriteB = false;
            return true;
        }
        else
        {
            return false;
        }
    }

    public synchronized boolean getCanReadB()
    {
        if(canReadB)
        {
            canReadB = false;
            return true;
        }
        else
        {
            return false;
        }
    }

    public synchronized void setCanWriteB()
    {
        canWriteB = false;
    }

    public synchronized void setCanReadB()
    {
        canReadB = false;
    }

    public synchronized void signalWrite()
    {
        canWriteB = true;
        notify();
    }

    public synchronized void signalRead()
    {
        canReadB = true;
        notify();
    }

    public synchronized void writeWait()
    {
        if(!getCanWriteB())
        {
            try
            {
                wait();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            setCanWriteB();
        }
    }

    public synchronized void readWait()
    {
        if(!getCanReadB())
        {
            try
            {
                wait();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            setCanReadB();
        }
    }
}
