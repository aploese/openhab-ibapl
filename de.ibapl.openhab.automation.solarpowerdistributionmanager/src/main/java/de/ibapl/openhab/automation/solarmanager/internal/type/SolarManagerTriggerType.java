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
package de.ibapl.openhab.automation.solarmanager.internal.type;


import static de.ibapl.openhab.automation.solarmanager.internal.SolarManager.*;
import static de.ibapl.openhab.automation.solarmanager.internal.handler.SolarManagerTriggerHandler.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.Output;
import org.openhab.core.automation.type.TriggerType;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;

/**
 *
 * @author Arne Plöse - Initial Contribution
 */
@NonNullByDefault
public class SolarManagerTriggerType extends TriggerType {
    private static final String DEFAULT_LOOPTIME_MS = "1000";
    private static final String ITEM = "item";

    public static SolarManagerTriggerType initialize() {
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_MAIN_POWER, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("Input Item") //
                .withDescription("Item to monitor") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_DEVICE_1_OUTPUT_POWER_SETPOINT_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("Setpoint") //
                .withDescription("Targeted setpoint") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KP_GAIN, Type.DECIMAL).withRequired(true) //
                .withMultiple(false) //
                .withDefault("1.0") //
                .withMinimum(BigDecimal.ZERO) //
                .withLabel("Proportional Gain (Kp)") //
                .withDescription("Change to output propertional to current error value.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KI_GAIN, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault("1.0") //
                .withMinimum(BigDecimal.ZERO) //
                .withLabel("Integral Gain (Ki)") //
                .withDescription("Accelerate movement towards the setpoint.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KD_GAIN, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault("1.0") //
                .withMinimum(BigDecimal.ZERO) //
                .withLabel("Derivative Gain (Kd)") //
                .withDescription("Slows the rate of change of the output.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KD_TIMECONSTANT, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withMinimum(BigDecimal.ZERO) //
                .withDefault("1.0") //
                .withLabel("Derivative Time Constant") //
                .withDescription("Slows the rate of change of the D-part (T1) in seconds.") //
                .withUnit("s") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_LOOP_TIME, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault(DEFAULT_LOOPTIME_MS) //
                .withLabel("Loop Time") //
                .withDescription("The interval the output value is updated in ms") //
                .withUnit("ms") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_I_MIN, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withLabel("I-part Lower Limit") //
                .withDescription("The I-part will be min this value. Can be left empty for no limit.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_I_MAX, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withLabel("I-part Upper Limit") //
                .withDescription("The I-part will be max this value. Can be left empty for no limit.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(P_INSPECTOR, Type.TEXT) //
                .withRequired(false) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("P Inspector Item") //
                .withDescription("Item for debugging the P part") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(I_INSPECTOR, Type.TEXT) //
                .withRequired(false) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("I Inspector Item") //
                .withDescription("Item for debugging the I part") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(D_INSPECTOR, Type.TEXT) //
                .withRequired(false).withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("D Inspector Item") //
                .withDescription("Item for debugging the D part") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(E_INSPECTOR, Type.TEXT) //
                .withRequired(false).withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("Error Inspector Item") //
                .withDescription("Item for debugging the error value") //
                .build());

        Output output = new Output(COMMAND, BigDecimal.class.getName(), "Output", "Output value of the Solar Manager Controller",
                Set.of("command"), null, null);

        return new SolarManagerTriggerType(configDescriptions, List.of(output));
    }

    public SolarManagerTriggerType(List<ConfigDescriptionParameter> configDescriptions, List<Output> outputs) {
        super(MODULE_TYPE_ID, configDescriptions, "Solar Manager controller triggers", null, null,
                Visibility.VISIBLE, outputs);
    }
}
