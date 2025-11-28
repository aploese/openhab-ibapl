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
package de.ibapl.openhab.automation.solarmanager.internal.handler;


import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseTriggerModuleHandler;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static de.ibapl.openhab.automation.solarmanager.internal.SolarManager.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arne Plöse - Initial Contribution
 */
@NonNullByDefault
public class SolarManagerTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber {
    public static final String MODULE_TYPE_ID = AUTOMATION_NAME + ".trigger";
    private static final Set<String> SUBSCRIBED_EVENT_TYPES = Set.of(ItemStateEvent.TYPE, ItemStateChangedEvent.TYPE);
    private final Logger logger = Logger.getLogger(SolarManagerTriggerHandler.class.getName());
    private final ServiceRegistration<?> eventSubscriberRegistration;
    private final SolarManager controller;
    private final int loopTimeMs;
    private long previousTimeMs = System.currentTimeMillis();
    final private Item mainPowerItem;
    final private Item device_1_OutputPower1tem;
    private Optional<String> commandTopic;
    private EventFilter eventFilter;
    private EventPublisher eventPublisher;
    private @Nullable String pInspector;
    private @Nullable String iInspector;
    private @Nullable String dInspector;
    private @Nullable String eInspector;
    private ItemRegistry itemRegistry;

    public SolarManagerTriggerHandler(Trigger module, ItemRegistry itemRegistry, EventPublisher eventPublisher,
            BundleContext bundleContext) {
        super(module);
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;

        Configuration config = module.getConfiguration();

        final String mainPowerItemName = (String) requireNonNull(config.get(CONFIG_MAIN_POWER), "Main power item is not set");
        final String device_1_OutputPower1temName = (String) requireNonNull(config.get(CONFIG_DEVICE_1_OUTPUT_POWER_SETPOINT_ITEM), "Device[1] output power Setpoint item is not set");

        try {
            mainPowerItem = itemRegistry.getItem(mainPowerItemName);
        } catch (ItemNotFoundException e) {
            throw new IllegalArgumentException("Configured main power item not found: " + mainPowerItemName, e);
        }

        try {
            device_1_OutputPower1tem = itemRegistry.getItem(device_1_OutputPower1temName);
        } catch (ItemNotFoundException e) {
            throw new IllegalArgumentException("Configured setpoint item not found: " + device_1_OutputPower1temName, e);
        }

        String commandItemName = (String) config.get(CONFIG_COMMAND_ITEM);
        if (commandItemName != null) {
            commandTopic = Optional.of("openhab/items/" + commandItemName + "/statechanged");
        } else {
            commandTopic = Optional.empty();
        }

        double kpAdjuster = getDoubleFromConfig(config, CONFIG_KP_GAIN);
        double kiAdjuster = getDoubleFromConfig(config, CONFIG_KI_GAIN);
        double kdAdjuster = getDoubleFromConfig(config, CONFIG_KD_GAIN);
        double kdTimeConstant = getDoubleFromConfig(config, CONFIG_KD_TIMECONSTANT);
        double iMinValue = getDoubleFromConfig(config, CONFIG_I_MIN);
        double iMaxValue = getDoubleFromConfig(config, CONFIG_I_MAX);
        pInspector = (String) config.get(P_INSPECTOR);
        iInspector = (String) config.get(I_INSPECTOR);
        dInspector = (String) config.get(D_INSPECTOR);
        eInspector = (String) config.get(E_INSPECTOR);

        loopTimeMs = ((BigDecimal) requireNonNull(config.get(CONFIG_LOOP_TIME), CONFIG_LOOP_TIME + " is not set"))
                .intValue();

        double previousIntegralPart = getItemNameValueAsNumberOrZero(itemRegistry, iInspector);
        double previousDerivativePart = getItemNameValueAsNumberOrZero(itemRegistry, dInspector);
        double previousError = getItemNameValueAsNumberOrZero(itemRegistry, eInspector);

        controller = new SolarManager(kpAdjuster, kiAdjuster, kdAdjuster, kdTimeConstant, iMinValue, iMaxValue,
                previousIntegralPart, previousDerivativePart, previousError);

        eventFilter = event -> {
            String topic = event.getTopic();

            return ("openhab/items/" + device_1_OutputPower1temName + "/state").equals(topic)
                    || ("openhab/items/" + device_1_OutputPower1temName + "/statechanged").equals(topic)
                    || ("openhab/items/" + device_1_OutputPower1temName + "/statechanged").equals(topic)
                    || commandTopic.map(t -> topic.equals(t)).orElse(false);
        };

        eventSubscriberRegistration = bundleContext.registerService(EventSubscriber.class.getName(), this, null);

        eventPublisher.post(ItemEventFactory.createCommandEvent(mainPowerItemName, RefreshType.REFRESH));
    }

    @Override
    public void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        getCallback().getScheduler().scheduleWithFixedDelay(this::calculate, 0, loopTimeMs, TimeUnit.MILLISECONDS);
    }

    private <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    private double getDoubleFromConfig(Configuration config, String key) {
        Object rawValue = config.get(key);

        if (rawValue == null) {
            return Double.NaN;
        }

        return ((BigDecimal) rawValue).doubleValue();
    }

    private void calculate() {
        final double mainPower = getItemValueAsNumber(mainPowerItem);

        long now = System.currentTimeMillis();

        SolarManmagetrOutputDTO output = controller.calculate(mainPower, now - previousTimeMs, loopTimeMs);
        previousTimeMs = now;

        updateItem(pInspector, output.getProportionalPart());
        updateItem(iInspector, output.getIntegralPart());
        updateItem(dInspector, output.getDerivativePart());
        updateItem(eInspector, output.getError());

        getCallback().triggered(module, Map.of(COMMAND, new DecimalType(output.getDevece_1_outputPowerSetpoint())));
    }

    private void updateItem(@Nullable String itemName, double value) {
        if (itemName != null) {
            try {
                itemRegistry.getItem(itemName);
                eventPublisher.post(ItemEventFactory.createStateEvent(itemName,
                        Double.isFinite(value) ? new DecimalType(value) : UnDefType.UNDEF));
            } catch (ItemNotFoundException e) {
                logger.log(Level.WARNING, "Item doesn''t exist: {0}", itemName);
            }
        }
    }

    private TriggerHandlerCallback getCallback() {
        ModuleHandlerCallback localCallback = callback;
        if (localCallback != null && localCallback instanceof TriggerHandlerCallback handlerCallback) {
            return handlerCallback;
        }

        throw new IllegalStateException("The module callback is not set");
    }

    private double getItemNameValueAsNumberOrZero(ItemRegistry itemRegistry, @Nullable String itemName)
            throws IllegalArgumentException {
        double value = 0.0;

        if (itemName == null) {
            return value;
        }

        try {
            value = getItemValueAsNumber(itemRegistry.getItem(itemName));
            logger.log(Level.FINER, "Item '{0}' value {0} recovered by Solar Manager controller", new Object[] {itemName, value});
        } catch (ItemNotFoundException e) {
            throw new IllegalArgumentException("Configured item not found: " + itemName, e);
        }

        return value;
    }

    private double getItemValueAsNumber(Item item)  {
        State setpointState = item.getState();

        if (setpointState instanceof Number number) {
            double doubleValue = number.doubleValue();

            if (Double.isFinite(doubleValue) && !Double.isNaN(doubleValue)) {
                return doubleValue;
            }
        } else if (setpointState instanceof StringType) {
            try {
                return Double.parseDouble(setpointState.toString());
            } catch (NumberFormatException e) {
                // nothing
            }
        }
        throw new RuntimeException("Not a number: " + setpointState.getClass().getSimpleName() + ": " + setpointState);
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ItemStateChangedEvent changedEvent) {
            if (commandTopic.isPresent() && event.getTopic().equals(commandTopic.get())) {
                if ("RESET".equals(changedEvent.getItemState().toString())) {
                    controller.setIntegralResult(0);
                    controller.setDerivativeResult(0);
                    eventPublisher.post(ItemEventFactory.createStateEvent(changedEvent.getItemName(), UnDefType.NULL));
                } else if (changedEvent.getItemState() != UnDefType.NULL) {
                    logger.log(Level.WARNING, "Unknown command: {0}", changedEvent.getItemState());
                }
            } else {
                calculate();
            }
        }
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return SUBSCRIBED_EVENT_TYPES;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return eventFilter;
    }

    @Override
    public void dispose() {
        eventSubscriberRegistration.unregister();

        super.dispose();
    }
}
