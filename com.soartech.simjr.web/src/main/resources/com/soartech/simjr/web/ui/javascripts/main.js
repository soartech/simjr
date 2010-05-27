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
 */ 
function SimJr(ge) {
    var that = this;
    
    ge.getWindow().setVisibility(true);
    ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);

    this.ge = ge;
    this.entities = {};
    this.messages = [];
    this.status = { time: 0.0, paused: true };
    this.firstRefresh = true;
    this.selected = null;
    var timeout = null;
    
    function _updateEntity(json) {
        var e = that.entities[json.name];
        if(e) {
            e.update(json);
        }
        else {
            e = new SimJrEntity(json);
            e.kml = {};
            e.html = {};
            if(e.visible) {
                
                if (json.location && !json.polygon) {
                    
                    var pm = ge.createPlacemark("entity-" + json.name);
                    pm.setName(e.name);
                    
                    var pmStyle = ge.createStyle('');
                    var pmIcon = ge.createIcon('');
                    pmIcon.setHref(e.getIconHref());
                    pmStyle.getIconStyle().setIcon(pmIcon);
                    pm.setStyleSelector(pmStyle);
                    
                    e.kml.point = ge.createPoint('');
                    pm.setGeometry(e.kml.point);
                    ge.getFeatures().appendChild(pm);
                    
                    e.kml.placemark = pm;
                }
                else if(json.polygon && !json.polygon.closed) {
                    var pm = ge.createPlacemark("entity-" + json.name);
                    pm.setName(e.name);
                    var line = ge.createLineString("");
                    pm.setGeometry(line);
                    
                    $.each(json.polygon.points, function() {
                        line.getCoordinates().pushLatLngAlt(this.latitude, this.longitude, this.altitude);
                    });
                    ge.getFeatures().appendChild(pm);
                    
                    pm.setStyleSelector(ge.createStyle(''));
                    var lineStyle = pm.getStyleSelector().getLineStyle();
                    lineStyle.setWidth(5);
                    lineStyle.getColor().set('8800ffff');  // aabbggrr format  
                    
                    e.kml.placemark = pm;
                    e.kml.line = line;
                }
                else if(json.polygon && json.polygon.closed) {
                    var pm = ge.createPlacemark("entity-" + json.name);
                    pm.setName(e.name);
                    var poly = ge.createPolygon("");
                    var line = ge.createLinearRing("");
                    pm.setGeometry(poly);
                    
                    $.each(json.polygon.points, function() {
                        line.getCoordinates().pushLatLngAlt(this.latitude, this.longitude, this.altitude);
                    });
                    poly.setOuterBoundary(line);
                    ge.getFeatures().appendChild(pm);
                    
                    pm.setStyleSelector(ge.createStyle(''));
                    
                    var lineStyle = pm.getStyleSelector().getLineStyle();
                    lineStyle.setWidth(5);
                    lineStyle.getColor().set('8800ffff');  // aabbggrr format
                    
                    var polyStyle = pm.getStyleSelector().getPolyStyle();
                    polyStyle.getColor().set('8800ffff');  // aabbggrr format  

                    e.kml.placemark = pm;
                    e.kml.polygon = poly;
                }
                
                google.earth.addEventListener(e.kml.placemark, 'click', function(event) {
                    event.preventDefault();
                    that.showProperties(e);
                });
            }
            that.entities[e.name] = e;
            
            e.update(json);
        }
        
        return e;
    }
    
    this.showProperties = function(entity) {
        //$("#entity-properties-table").empty();
        if(entity) {
            entity.getProperties(function(json) {
                var table = $("<table>"); 
                $.each(json, function(name, value) {
                    if(!name.match(/^shape/) && name != "ms2525.template" && 
                        name != "position" && name != "velocity" && name != "orientation" &&
                        name != "class" && name != "category" && name != "visible") {
                        $("<tr>").append($("<td>").text(name)).
                                  append($("<td>").text(value)).
                                  appendTo(table);
                    }
                });
                var div = $("<div>").
                    append($("<img>").attr("src", entity.getIconHref())).
                    append($("<b>").text(entity.name)).
                    append(table);
                
                that.showBalloon({
                    div: div.get(0),
                    feature: entity.kml.placemark ? entity.kml.placemark : undefined
                });
            });
        }
    }
    
    this._executeCommand = function(json, success) {
        $.ajax({
            type: "POST",
            url: "/simjr/sim/commands",
            data: json,
            processData: false,
            dataType: "json",
            success: success
        });
    };
    
    this.getJSON = function(url, success) {
        $.getJSON(url, function(json) {    
            success(json);
        });
    };
    
    this.run = function() {
        this._executeCommand("{action:'run'}", function() {
            that.refresh(true);
        });
    };
    
    this.pause = function() {
        this._executeCommand("{action:'pause'}", function() {});
        if(timeout != null){
            clearTimeout(timeout);
            timeout = null;
            this.refresh();
        }
    };
    
    this.setTimeFactor = function(factor) {
        this._executeCommand("{action:'setTimeFactor', value:" + factor + "}", function() {});
    };
    
    this.lookAt = function(entity) {
        if(!entity.location) {
            return;
        }
        ge.setBalloon(null);
        var la = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);
        la.setLatitude(entity.location.latitude);
        la.setLongitude(entity.location.longitude);
        ge.getView().setAbstractView(la);
    }
    
    this.refresh = function(repeat) {
        this.getJSON("/simjr/sim", function(json) {
            this.status = json;
            $("#run-state").text(json.paused ? "Paused" : "Running");
            $("#time").text(json.time.toFixed(2) + " seconds");
        });
        
        this.getJSON("/simjr/sim/entities", function(json) {
            var first = null;
            $.each(json, function() {
                var e = _updateEntity(this);
                if(first == null && e.kml && e.kml.point) first = e;
            });
            
            if(first && that.firstRefresh) {
                var point = first.kml.point;
                var lookAt = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);
                lookAt.setLatitude(point.getLatitude());
                lookAt.setLongitude(point.getLongitude());
                lookAt.setRange(10000.0);
                ge.getView().setAbstractView(lookAt);
            }
            that.firstRefresh = false;
            
            if(repeat) {
                timeout = setTimeout("simjr.refresh(true)", 1000);
            }
        });
        
        this.refreshMessages();
    };
    
    function createEntityLink(name) {
        return $("<a>").attr("href", "#").text(name).click(function(){
            var entity = that.entities[name];
            if(entity.visible) {
                that.lookAt(entity);
            } else {
                that.showProperties(entity);
            }
        });
    }
    
    this.refreshMessages = function(handler) {
        var since = this.messages.length == 0 ? 0.0 : this.messages[this.messages.length - 1].time + 0.001;
        
        this.getJSON("/simjr/sim/messages?since=" + escape(since), function(json){
            that.messages = that.messages.concat(json);
            var tableBody = $("#message-history-table-body");
            $.each(json, function() {
                var fromTo = $("<td>").append(createEntityLink(this.source));
                if(this.target) {
                    fromTo.append(" --> ").append(createEntityLink(this.target));
                }
                var tr = $("<tr>").
                        append($("<td>").text(this.time.toFixed(2))).
                        append(fromTo).
                        append($("<td>").text(this.content)).
                        append($("<td>").text(this.frequency)).
                        prependTo(tableBody);
            });
            
            // Pass list of new messages to handler if provided
            if(handler) {
                handler(json);
            }
        });
    };
    
    this.showBalloon = function(options) {
        ge.setBalloon(null); // clear current balloon (stupid, but required)
        
        var balloon = ge.createHtmlDivBalloon('');
        if(options.feature) balloon.setFeature(options.feature);

        balloon.setContentDiv(options.div);

        if(options.maxWidth) balloon.setMaxWidth(options.maxWidth);
        if(options.minWidth) balloon.setMinWidth(options.minWidth);
        if(options.backgroundColor) balloon.setBackgroundColor(options.backgroundColor);
        ge.setBalloon(balloon);
    }
}

function mapReady(instance) {
    simjr = new SimJr(instance);
    simjr.refresh();
}

function mapFailed(errorCode) {
}

google.load("earth", "1");

$(function init() {
    google.earth.createInstance('map', mapReady, mapFailed);
    
    $("#show-entity-list").click(function() {
        var ul = $("<ul>")
        $.each(simjr.entities, function(name, entity) {
            
            var li = $("<li>").appendTo(ul);
            $("<img>").attr("src", entity.getIconHref()).
                       attr("width", 16).
                       attr("height", 16).
                       appendTo(li);
            $("<a>").attr("href", "#").text(entity.name).appendTo(li).click(function(event) {
                if(entity.visible) {
                    simjr.lookAt(entity);
                }
                else {
                    simjr.showProperties(entity);
                }
            });
            if(!entity.visible) {
                $("<span>").text(" (hidden)").appendTo(li);
            }
        });
        var div = $("<div>").addClass("entity-list-balloon").
                    append($("<h2>").text("Entities")).
                    append(ul);
        simjr.showBalloon({
            div:div.get(0),
            minWidth:150
        });
    });
    
    $("#time-factor").change(function() {
        simjr.setTimeFactor($(this).val());
    });
});
