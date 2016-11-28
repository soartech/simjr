package com.soartech.simjr.web.gson.adapter;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.soartech.math.Vector3;

public class Vector3Adapter implements JsonSerializer<Vector3>
{
    public static class Vector3Proxy
    {
        public final double x, y, z;
        
        public Vector3Proxy(Vector3 vec)
        {
            x = vec.x;
            y = vec.y;
            z = vec.z;
        }
    }

    @Override
    public JsonElement serialize(Vector3 vec, Type type,
            JsonSerializationContext ctx)
    {
        return ctx.serialize(new Vector3Proxy(vec), Vector3Proxy.class);
    }
}
