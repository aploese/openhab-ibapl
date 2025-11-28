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
package de.ibapl.openhab.onewire4j.internal;

import de.ibapl.openhab.onewire4j.handler.HumidityHandler;
import de.ibapl.openhab.onewire4j.handler.SpswBridgeHandler;
import de.ibapl.openhab.onewire4j.handler.TemperatureHandler;
import de.ibapl.openhab.onewire4j.handler.UnknownDeviceHandler;
import de.ibapl.openhab.onewire4j.internal.discovery.OneWire4JDiscoveryService;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import static de.ibapl.openhab.onewire4j.OneWire4JBindingConstants.ThingTypes;

/**
 * The {@link OneWire4JHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.onewire4j")
public class OneWire4JHandlerFactory extends BaseThingHandlerFactory {

    @Reference
    private List<SerialPortSocketFactory> serialPortSocketFactories;

    private final Map<ThingUID, org.osgi.framework.ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ThingTypes.supportsThingType(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypes thingType = ThingTypes.find(thing.getThingTypeUID());

        return switch (thingType) {
            case TEMPERATURE ->
                new TemperatureHandler(thing);
            case HUMIDITY ->
                new HumidityHandler(thing);
            case BRIDGE_RS232 ->
                registerDiscoveryService(new SpswBridgeHandler((Bridge) thing, serialPortSocketFactories));
            case UNKNOWN ->
                new UnknownDeviceHandler(thing);
            case SMART_BATTERY_MONITOR ->
//TODO impement
                null;
        };
    }

    private synchronized SpswBridgeHandler registerDiscoveryService(SpswBridgeHandler spswBridgeHandler) {
        OneWire4JDiscoveryService discoveryService = new OneWire4JDiscoveryService(spswBridgeHandler);
        this.discoveryServiceRegs.put(spswBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
        return spswBridgeHandler;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SpswBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                OneWire4JDiscoveryService service = (OneWire4JDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

}
