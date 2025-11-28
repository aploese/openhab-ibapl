/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2025, Arne Plöse and individual contributors as indicated
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
package de.ibapl.openhab.binding.zendure.internal;

import static de.ibapl.openhab.binding.zendure.internal.ZendureBindingConstants.ThingTypes.SOLAR_FLOW_800_PRO;
import de.ibapl.openhab.binding.zendure.internal.handler.GenericZendureDeviceHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhab.core.thing.Bridge;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link ZendureHandlerFactory} is responsible for creating things and
 * thing handlers. It is completely boiler-plate and nothing special at all.
 *
 * @author Arne Plöse - Initial contribution
 */
@Component(configurationPid = "binding.zendure", service = ThingHandlerFactory.class)
public class ZendureHandlerFactory extends BaseThingHandlerFactory {

    private final static Logger LOG = Logger.getLogger(ZendureHandlerFactory.class.getName());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ZendureBindingConstants.ThingTypes.supportsThingType(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ZendureBindingConstants.ThingTypes thingType = ZendureBindingConstants.ThingTypes.find(thing.getThingTypeUID());
        LOG.log(Level.FINER, "Create Thing Handler for {0}", thingType);
        switch (thingType) {
            case SOLAR_FLOW_800 -> {
                return new GenericZendureDeviceHandler((Bridge) thing, thingType);
            }
            case SOLAR_FLOW_800_PRO -> {
                return new GenericZendureDeviceHandler((Bridge) thing, thingType);
            }
            default -> {
                LOG.log(Level.SEVERE, "Implement Me! Zendure Handler");
                throw new RuntimeException("Implement Me!");
        }
    }
}
}
