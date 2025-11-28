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
package de.ibapl.openhab.automation.solarmanager.internal.factory;

import de.ibapl.openhab.automation.solarmanager.internal.handler.SolarManagerTriggerHandler;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.ItemRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author Hilbrand Bouwkamp - Initial Contribution
 */
@Component(service = ModuleHandlerFactory.class, configurationPid = "action.solarmanager")
@NonNullByDefault
public class SolarManagerModuleHandlerFactory extends BaseModuleHandlerFactory {
    private static final Collection<String> TYPES = Set.of(SolarManagerTriggerHandler.MODULE_TYPE_ID);
    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private BundleContext bundleContext;

    @Activate
    public SolarManagerModuleHandlerFactory(@Reference ItemRegistry itemRegistry,
            @Reference EventPublisher eventPublisher, BundleContext bundleContext) {
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
        this.bundleContext = bundleContext;
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        switch (module.getTypeUID()) {
            case SolarManagerTriggerHandler.MODULE_TYPE_ID:
                return new SolarManagerTriggerHandler((Trigger) module, itemRegistry, eventPublisher, bundleContext);
        }

        return null;
    }
}
