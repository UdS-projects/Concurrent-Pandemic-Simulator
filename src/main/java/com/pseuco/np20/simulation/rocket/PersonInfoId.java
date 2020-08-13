package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.PersonInfo;
import com.pseuco.np20.simulation.common.Person;

public class PersonInfoId
{
    private final PersonInfo info;
    private final int id;

    public PersonInfoId(Person p)
    {
        info = p.getInfo();
        id = p.getId();
    }

    public int getId()
    {
        return id;
    }

    public PersonInfo getInfo()
    {
        return info;
    }
}
