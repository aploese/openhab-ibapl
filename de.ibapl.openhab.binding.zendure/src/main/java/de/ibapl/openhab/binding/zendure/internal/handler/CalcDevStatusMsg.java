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
package de.ibapl.openhab.binding.zendure.internal.handler;

/**
 *
 * @author aploese
 */
public class CalcDevStatusMsg {

    public static void main(String[] args) throws Exception {
//select distinct value from item0126 order by value union select distinct value from item0120 order by value;
        long[] values = new long[]{1034, 1035, 1281, 1288, 1290, 1291, 1292, 1301, 1546, 1547, 5121, 5128, 5130, 5131, 5132, 5141, 5377, 5386, 5388, 5397};
        for (long value : values) {
            String result = String.format("<option value=\"%d\">% 6d:", value, value);
            if ((value & DeviceStateEntries.PV_RUNNING) == DeviceStateEntries.PV_RUNNING) {
                result += "PV-->";
            } else {
                result += "PV___";
            }

            if ((value & DeviceStateEntries.BATTERY_PACK_CHARGING) == DeviceStateEntries.BATTERY_PACK_CHARGING) {
                result += ";BP&lt;--";
            } else if ((value & DeviceStateEntries.BATTERY_PACK_DISCHARGING) == DeviceStateEntries.BATTERY_PACK_DISCHARGING) {
                result += ";BP--&gt;";
            } else {
                result += ";BP___";
            }

            if ((value & DeviceStateEntries.BATTERY_CALIBRATING) == DeviceStateEntries.BATTERY_CALIBRATING) {
                result += ",___";
            } else {
                result += ",cal";
            }

            if ((value & DeviceStateEntries.BYPASSING) == DeviceStateEntries.BYPASSING) {
                result += ",      ";
            } else {
                result += ",bypass";
            }

            if ((value & DeviceStateEntries.REVERSE_FLOWING) == DeviceStateEntries.REVERSE_FLOWING) {
                result += ",flow--&gt;";
            } else {
                result += ",flow&lt;--";
            }

            if ((value & DeviceStateEntries.DC_BATTERY_INPUT) == DeviceStateEntries.DC_BATTERY_INPUT) {
                result += ";DC BP&lt;--";
            } else if ((value & DeviceStateEntries.DC_BATTERY_OUTPUT) == DeviceStateEntries.DC_BATTERY_OUTPUT) {
                result += ";DC BP--&gt;";
            } else {
                result += ";DC BP____";
            }

            if ((value & DeviceStateEntries.AC_GRID_CONNECTED_OPERATION) == DeviceStateEntries.AC_GRID_CONNECTED_OPERATION) {
                result += ";AC UG conn";
            } else if ((value & DeviceStateEntries.AC_CHARGING_OPERATION) == DeviceStateEntries.AC_CHARGING_OPERATION) {
                result += ";AC  charge";
            } else {
                result += ";AC _______";
            }

            if ((value & DeviceStateEntries.GRID_STATE_CONNECTED) == DeviceStateEntries.GRID_STATE_CONNECTED) {
                result += ";UG conn";
            } else {
                result += ";UG ____";
            }

            if ((value & DeviceStateEntries.SOC_CHARGE_LIMIT_REACHED) == DeviceStateEntries.SOC_CHARGE_LIMIT_REACHED) {
                result += ";SOC  &gt;&gt;|";
            } else if ((value & DeviceStateEntries.SOC_DISCHARGE_LIMIT_REACHED) == DeviceStateEntries.SOC_DISCHARGE_LIMIT_REACHED) {
                result += ";SOC  |&lt;&lt;";
            } else {
                result += ";SOC  |&gt;&lt;|";
            }

            if ((value & DeviceStateEntries.ERROR_PENDING) == DeviceStateEntries.ERROR_PENDING) {
                result += ";Err y";
            } else {
                result += ";Err _";
            }

            System.out.println(result + "</option>");
        }
    }

    public static interface DeviceStateEntries {
        //Order is important sync with src/main/resources/OH-INF.thing/channel-types.xml
//        PV_STOPPED,

        final public long PV_RUNNING = 0x0000000000000001L;
//        BATTERY_PACK_STANDBY, //Needed?
        final public long BATTERY_PACK_CHARGING = 0x0000000000000002L;
        final public long BATTERY_PACK_DISCHARGING = 0x0000000000000004L;

//        DC_STOPPED,
        final public long DC_BATTERY_INPUT = 0x0000000000000008L;
        final public long DC_BATTERY_OUTPUT = 0x0000000000000010L;
//        BATTERY_NOT_CALIBRATING,
        final public long BATTERY_CALIBRATING = 0x0000000000000020L;
//        NOT_BYPASSING,
        final public long BYPASSING = 0x0000000000000040L;
//        NOT_REVERSE_FLOWING,
        final public long REVERSE_FLOWING = 0x0000000000000080L;
//        AC_STOPPED,
        final public long AC_GRID_CONNECTED_OPERATION = 0x0000000000000100L;
        final public long AC_CHARGING_OPERATION = 0x0000000000000200L;

//        GRID_STATE_NOT_CONNECTED,
        final public long GRID_STATE_CONNECTED = 0x0000000000000400L;

//        SOC_NORMAL_STATE,
        final public long SOC_CHARGE_LIMIT_REACHED = 0x0000000000000800L;
        final public long SOC_DISCHARGE_LIMIT_REACHED = 0x0000000000001000L;

//        NO_ERROR_PENDING,
        final public long ERROR_PENDING = 0x0000000000002000L;

    }

}
