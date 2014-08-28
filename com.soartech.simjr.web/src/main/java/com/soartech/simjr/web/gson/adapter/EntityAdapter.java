package com.soartech.simjr.web.gson.adapter;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.web.gson.adapter.Vector3Adapter.Vector3Proxy;

public class EntityAdapter implements JsonSerializer<Entity>
{
    public static class EntityProxy
    {
        public final String name;
        public final Vector3Proxy position;
        
        public EntityProxy(Entity entity)
        {
            name = entity.getName();
            position = new Vector3Proxy(entity.getPosition());
        }
    }
    
    @Override
    public JsonElement serialize(Entity entity, Type type, JsonSerializationContext ctx)
    {
        return ctx.serialize(new EntityProxy(entity), EntityProxy.class);
    }

}
