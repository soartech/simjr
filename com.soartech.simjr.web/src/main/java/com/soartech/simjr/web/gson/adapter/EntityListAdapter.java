package com.soartech.simjr.web.gson.adapter;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.web.gson.adapter.EntityAdapter.EntityProxy;

public class EntityListAdapter implements JsonSerializer<List<Entity>>
{   
    @Override
    public JsonElement serialize(List<Entity> list, Type type,
            JsonSerializationContext ctx)
    {
        List<EntityProxy> proxyList = new LinkedList<EntityProxy>();
        for (Entity e : list)
        {
            proxyList.add(new EntityProxy(e));
        }
        return ctx.serialize(proxyList);
    }
}
