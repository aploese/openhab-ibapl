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
package de.ibapl.openhab.automation.solarmanager.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Constants for Solar Manager.
 *
 * @author Arne Plöse - Initial contribution
 *
 */
@NonNullByDefault
public class SolarManager {
    public static final String AUTOMATION_NAME = "solarmanager";
    public static final String CONFIG_MAIN_POWER = "mainPower";
    public static final String CONFIG_DEVICE_1_OUTPUT_POWER_SETPOINT_ITEM = "device1OutputPowerSetpoint";
    public static final String CONFIG_COMMAND_ITEM = "commandItem";
    public static final String CONFIG_LOOP_TIME = "loopTime";
    public static final String CONFIG_KP_GAIN = "kp";
    public static final String CONFIG_KI_GAIN = "ki";
    public static final String CONFIG_KD_GAIN = "kd";
    public static final String CONFIG_KD_TIMECONSTANT = "kdTimeConstant";
    public static final String CONFIG_I_MAX = "integralMaxValue";
    public static final String CONFIG_I_MIN = "integralMinValue";
    public static final String P_INSPECTOR = "pInspector";
    public static final String I_INSPECTOR = "iInspector";
    public static final String D_INSPECTOR = "dInspector";
    public static final String E_INSPECTOR = "eInspector";
    public static final String COMMAND = "command";
}
