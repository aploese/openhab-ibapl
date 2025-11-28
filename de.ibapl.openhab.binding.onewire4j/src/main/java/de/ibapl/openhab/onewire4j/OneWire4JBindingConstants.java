/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2024-2025, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.ibapl.openhab.onewire4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OneWire4JBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class OneWire4JBindingConstants {

    public static final String BINDING_ID = "onewire4j";

    public enum ThingTypes {
        TEMPERATURE("temperature", false),
        HUMIDITY("humidity", false),
        SMART_BATTERY_MONITOR("smart_battery_monitor", false),
        UNKNOWN("unknown", false),
        BRIDGE_RS232("rs232-bridge", true);

        private final static Map<ThingTypeUID, ThingTypes> supportedThingTypeUIDs = new HashMap<ThingTypeUID, ThingTypes>();

        public static ThingTypes find(ThingTypeUID thingTypeUID) {
            return supportedThingTypeUIDs.get(thingTypeUID);
        }

        public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
            return supportedThingTypeUIDs.containsKey(thingTypeUID);
        }

        public final ThingTypeUID thingTypeUID;
        public final boolean isBridge;

        private ThingTypes(String thingTypeId, boolean isBridge) {
            thingTypeUID = new ThingTypeUID(BINDING_ID, thingTypeId);
            this.isBridge = isBridge;
        }


        static {
            for (ThingTypes uid : ThingTypes.values()) {
                supportedThingTypeUIDs.put(uid.thingTypeUID, uid);
            }
        }
    }

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_MIN_TEMPERATURE = "minTemperature";
    public static final String CHANNEL_MAX_TEMPERATURE = "maxTemperature";

}
