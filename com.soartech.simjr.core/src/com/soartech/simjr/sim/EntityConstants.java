/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on May 30, 2007
 */
package com.soartech.simjr.sim;

/**
 * Various constants for simulation entities.  Consists mostly of constants
 * for property names and values.
 * 
 * @author ray
 */
public interface EntityConstants
{
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_CLASS = "class";
    public static final String PROPERTY_PROTOTYPE = "prototype";
    public static final String PROPERTY_FORCE = "force";
    public static final String PROPERTY_CALLSIGN = "callsign";
    public static final String PROPERTY_DAMAGE = "damage";
    public static final String PROPERTY_SHAPE = "shape";
    public static final String PROPERTY_SHAPE_LAYER = "shape.layer";
    public static final String PROPERTY_SHAPE_FILL_COLOR = "shape.fill.color";
    public static final String PROPERTY_SHAPE_LINE_COLOR = "shape.line.color";
    public static final String PROPERTY_SHAPE_WIDTH_METERS = "shape.line.width.meters";
    public static final String PROPERTY_SHAPE_WIDTH_PIXELS = "shape.line.width.pixels";
    public static final String PROPERTY_SHAPE_OPACITY = "shape.opacity";
    public static final String PROPERTY_SHAPE_LABEL_VISIBLE = "shape.label.visible";
    public static final String PROPERTY_SHAPE_LABEL_DISPLAYIFPARENT = "shape.label.displayIfParent";
    public static final String PROPERTY_VISIBLE = "visible";
    public static final String PROPERTY_LOCKED = "locked";
    public static final String PROPERTY_CATEGORY = "category";
    public static final String PROPERTY_VELOCITY = "velocity";
    public static final String PROPERTY_POSITION = "position";
    public static final String PROPERTY_ORIENTATION = "orientation";
    public static final String PROPERTY_YAW = "yaw";
    public static final String PROPERTY_PITCH = "pitch";
    public static final String PROPERTY_ROLL = "roll";
    public static final String PROPERTY_WEB_DISABLE_ONCLICK = "web.selection.allow";
    public static final String PROPERTY_APPEARANCE = "appearance";
    public static final String PROPERTY_STATUS = "status";
    /**
     * @see LazyMgrsProperty
     */
    public static final String PROPERTY_MGRS = "mgrs";
    public static final String PROPERTY_LATITUDE = "latitude";
    public static final String PROPERTY_LONGITUDE = "longitude";
    public static final String PROPERTY_ALTITUDE = "altitude";
    public static final String PROPERTY_POINTS = "points";
    public static final String PROPERTY_POLYGONS = "polygons";
    public static final String PROPERTY_DIAMETER = "diameter";
    
    public static final String PROPERTY_MAXSPEED = "limits.maxSpeed";
    
    public static final String PROPERTY_BEHOLDER_RANGE = "beholder.range";
    public static final String PROPERTY_BEHOLDER_ARC = "beholder.arc";

    /**
     * Property holding visible range information for an entity. Must be of
     * type EntityVisibleRange.
     */
    public static final String PROPERTY_VISIBLE_RANGE = "visible-range";
    public static final String PROPERTY_VISIBLE_RANGE_VISIBLE = "visible-range.visible";
    public static final String PROPERTY_RADAR = "radar";
    public static final String PROPERTY_RADAR_VISIBLE = "radar.visible";
    
    public static final String PROPERTY_ENFORCE_AGL = "enforce-agl";
    
    public static final String PROPERTY_VOICE = "voice";
    public static final String PROPERTY_FREQUENCY = "frequency";
    public static final String PROPERTY_TRANSMITTER_ID = "transmitter-id";

    public static final String PROPERTY_HEADING = "heading";
    public static final String PROPERTY_BEARING = "bearing";
    public static final String PROPERTY_SPEED = "speed";
    public static final String PROPERTY_AGL = "agl";
    public static final String PROPERTY_FUEL = "fuel";

    public static final String PROPERTY_CONTAINS = "contains";
    public static final String PROPERTY_CONTAINER = "container";
    
    public static final String PROPERTY_CCIP = "ccip";
    
    public static final String LAYER_AREA = "area";
    public static final String LAYER_WAYPOINT = "waypoint";
    public static final String LAYER_ROUTE = "route";
    public static final String LAYER_GROUND = "ground";
    public static final String LAYER_AIR = "air";
    public static final String LAYER_SHADOWS = "shadows";
    public static final String LAYER_SELECTION = "selection";
    public static final String LAYER_LABELS = "labels";
    
    public static final String FORCE_OTHER = "other";       // Green
    public static final String FORCE_FRIENDLY = "friendly"; // Blue
    public static final String FORCE_OPPOSING = "opposing";      // Red
    public static final String FORCE_NEUTRAL = "neutral";   // Yellow
    
    public static String[] ALL_FORCES = new String[] {
        FORCE_FRIENDLY,
        FORCE_OPPOSING,
        FORCE_NEUTRAL,
        FORCE_OTHER
    };
    
    // These all are in title case in the yaml files so equalsIgnoreCase
    // needs to be used....
    public static final String CATEGORY_ROUTES = "Routes";
    public static final String CATEGORY_WAYPOINTS = "Waypoints";
    public static final String CATEGORY_VEHICLES = "Vehicles";
    public static final String CATEGORY_HUMANS = "Humans";
    public static final String CATEGORY_AREAS = "Areas";
    public static final String CATEGORY_COMPOUND_AREAS = "CompoundAreas";
    public static final String CATEGORY_FLYOUTS = "Flyouts";
    public static final String CATEGORY_STRUCTURES = "Structures";
    
    public static String[] ALL_CATEGORIES = new String[] {
        CATEGORY_HUMANS,
        CATEGORY_VEHICLES,
        CATEGORY_ROUTES,
        CATEGORY_WAYPOINTS,
        CATEGORY_FLYOUTS,
        CATEGORY_STRUCTURES
    };
}
