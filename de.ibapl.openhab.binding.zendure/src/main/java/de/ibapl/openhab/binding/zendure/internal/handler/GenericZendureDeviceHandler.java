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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.ibapl.openhab.binding.zendure.internal.ZendureBindingConstants;
import de.ibapl.openhab.binding.zendure.internal.ZendureConfig;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.RefreshType;

/**
 * The {@link GenericZendureDeviceHandler} is responsible for handling commands,
 * which are sent to one of the channels. It does the "heavy lifting" of
 * connecting to the Solar-Log, getting the data, parsing it and updating the
 * channels.
 *
 * @author Arne Plöse - Initial contribution
 */
public class GenericZendureDeviceHandler extends BaseBridgeHandler {

    private final static Logger LOGGER = Logger.getLogger(GenericZendureDeviceHandler.class.getName());
    private ZendureConfig config;
    private final ZendureBindingConstants.ThingTypes zendureDeviceType;

    public GenericZendureDeviceHandler(Bridge bridge, ZendureBindingConstants.ThingTypes zendureDeviceType) {
        super(bridge);
        this.zendureDeviceType = zendureDeviceType;
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

    public interface GridReverseFlowMode {

        //Order is important sync with src/main/resources/OH-INF.thing/channel-types.xml
        final public DecimalType DISABLED = new DecimalType(0);
        final public DecimalType REVERSE_FLOW_ALLOWED = new DecimalType(1);
        final public DecimalType REVERSE_FLOW_FORBIDDEN = new DecimalType(2);

    }

    public interface GridOffMode {

        //Order is important sync with src/main/resources/OH-INF.thing/channel-types.xml
        final public DecimalType NORMAL = new DecimalType(0);
        final public DecimalType ECO = new DecimalType(1);
        final public DecimalType OFF = new DecimalType(2);

    }

    public interface GridStandard {

        //Order is important sync with src/main/resources/OH-INF.thing/channel-types.xml
        final public DecimalType GERMANY = new DecimalType(0);
        final public DecimalType FRANCE = new DecimalType(1);
        final public DecimalType AUSTRIA = new DecimalType(2);

    }

    interface Channels {
        //PV related channels 

        public final static String PV_POWER = "PvPower";
        public final static String PV_POWER_PANEL_1 = "PvPowerPanel_1";
        public final static String PV_POWER_PANEL_2 = "PvPowerPanel_2";
        public final static String PV_POWER_PANEL_3 = "PvPowerPanel_3";
        public final static String PV_POWER_PANEL_4 = "PvPowerPanel_4";
        //Inverter related channels 
        public final static String INVERTER_POWER = "InverterPower";
        public final static String INVERTER_DESIRED_POWER = "InverterDesiredPower";
        public final static String INVERTER_OFF_GRID_POWER = "InverterOffGridPower";
        public final static String INVERTER_OFF_GRID_MODE = "InverterOffGridMode";
//Baterie pack related channels 
        public final static String BATTERY_PACK_HEATING = "BatteryPackHeating";
        public final static String BATTERY_PACK_POWER = "BatteryPackPower";
        public final static String BATTERY_PACK_CAPACITY = "BatteryPackCapacity";
        public final static String NUMBER_OF_BATTERIES_IN_PACK = "NumberOfBatteriesInPack";
        public final static String BATTERY_PACK_STATE_OF_CHARGE = "BatteryPackStateOfCharge";
        public final static String BATTERY_PACK_CHARGE_LEVGEL_MAX = "BatteryPackChargeLevelMax";
        public final static String BATTERY_PACK_DISCHARGE_LEVEL_MIN = "BatteryPackDischargeLevelMin";
        //TODO thing.xml is missing
        public final static String BATTERY_PACK_REMAINING_DISCHARGE_TIME = "BatteryPackRemainingDischargeTime";
        public final static String DEVICE_TEMPERATURE = "DeviceTemperature";
        public final static String GRID_STANDARD = "GridStandard";
        public final static String DEVICE_LAMP_SWITCH = "DeviceLampSwitch";
        public final static String DEVICE_FAN_MODE = "DeviceFanMode";
        public final static String DEVICE_FAN_SPEED = "DeviceFanSpeed";
        public final static String DEVICE_SMART_MODE = "SmartMode";
        public final static String DEVICE_WLAN_RSSI = "RSSI";
        public final static String INVERTER_MAX_POWER_LIMIT_IN = "InverterMaxPowerLimitIn";
        public final static String INVERTER_MAX_POWER_LIMIT_OUT = "InverterMaxPowerLimitOut";
        public final static String GRID_REVERSE_FLOW_MODE = "GridReverseFlowMode";
        public final static String DEVICE_STATE = "DeviceState";
    }

    private final int timeout = 1000;
    private String serialNumber;
    private int packNum;
    private double batteryPackCapacity;

    private ScheduledFuture<?> refreshJob;

    private GenericZendureDeviceHandler(Bridge bridge) {
        super(bridge);
        throw new RuntimeException("zendureDeviceType must be initilized!");
    }

    @Override
    public void initialize() {
        LOGGER.finer("Initializing Zendure");
        config = getConfigAs(ZendureConfig.class);
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            LOGGER.finer("Running refresh cycle");
            try {
                refresh();
                updateStatus(ThingStatus.ONLINE);
                // Very rudimentary Exception differentiation
            } catch (IOException e) {
                LOGGER.log(Level.FINER, "Error reading response from Solar-Log", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication error with the device. Please retry later.");
            } catch (JsonSyntaxException je) {
                LOGGER.log(Level.WARNING, "Invalid JSON when refreshing ", je);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error refreshing ", e);
            }
        }, 0, config.refreshInterval < 1 ? 1 : config.refreshInterval, TimeUnit.SECONDS); // Minimum interval is 1 s
    }

    //TODO reader.skipValue sollte nicht mehr vorkommen
    private void refresh() throws Exception {
        LOGGER.finer("Starting refresh handler");

        URI uri = new URI(config.url + "/properties/report");
        HttpURLConnection httpConnection = (HttpURLConnection) uri.toURL().openConnection();
        httpConnection.setRequestMethod("GET");
        if (httpConnection.getResponseCode() != 200) {
            throw new RuntimeException("Can't get data from device: " + httpConnection.getResponseCode() + " " + httpConnection);
        }

        JsonReader reader = new JsonReader(new InputStreamReader(httpConnection.getInputStream()));
        if (!reader.hasNext()) {
            throw new RuntimeException("Empty!");
        }
        reader.beginObject();

        String name = reader.nextName();
        if ("timestamp".equals(name)) {
            reader.skipValue();
        } else {
            throw new RuntimeException("expected \"timestamp\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("messageId".equals(name)) {
            reader.skipValue();
        } else {
            throw new RuntimeException("expected \"messageId\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("sn".equals(name)) {
            serialNumber = reader.nextString();
        } else {
            throw new RuntimeException("expected \"sn\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("version".equals(name)) {
            reader.skipValue();
        } else {
            throw new RuntimeException("expected \"version\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("product".equals(name)) {
            final String value = reader.nextString();
            switch (zendureDeviceType) {
                case SOLAR_FLOW_800 -> {
                    if ("solarFlow800".equals(value)) {
                    } else {
                        throw new RuntimeException("expected \"product\" == \"solarFlow800\" but found: \"" + value + '\"');
                    }
                }
                case SOLAR_FLOW_800_PRO -> {
                    if ("solarFlow800Pro".equals(value)) {
                    } else {
                        throw new RuntimeException("expected \"product\" == \"solarFlow800Pro\" but found: \"" + value + '\"');
                    }
                }
                default ->
                    throw new RuntimeException(" unknown \"product\" found: \"" + value + '\"');
            }
        } else {
            throw new RuntimeException("expected \"product\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("properties".equals(name)) {
            reader.beginObject();
            readSolarFlowProperties(reader);
            reader.endObject();
        } else {
            throw new RuntimeException("expected \"properties\" but found: \"" + name + '\"');
        }

        batteryPackCapacity = 0.0;

        name = reader.nextName();
        if ("packData".equals(name)) {
            int i = 0;
            reader.beginArray();
            while (reader.hasNext()) {
                i++;
                reader.beginObject();
                readBatteryPackProperties(reader);
                reader.endObject();
            }
            reader.endArray();
            if (i != packNum) {
                throw new RuntimeException("Number of batteriepacks mismatch! property \"packNum\" = " + packNum + " entries found = " + i);
            }
        } else {
            throw new RuntimeException("expected \"packData\" but found: \"" + name + '\"');
        }

        reader.endObject();

        updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_CAPACITY), new DecimalType(batteryPackCapacity));

        LOGGER.finer("Refresh DONE");

    }

    private void readBatteryPackProperties(JsonReader reader) throws IOException {
        final double capacity;
        final String model;
//            "": "CO4EHNCDN234434",
        String name = reader.nextName();
        if ("sn".equals(name)) {
            final String sn = reader.nextString();
//"lifted" from      https://github.com/Zendure/Zendure-HA/blob/master/custom_components/zendure_ha/device.py  
            switch (sn.charAt(0)) {
                case 'A' -> {
                    if (sn.charAt(3) == '3') {
                        model = "AIO2400";
                        capacity = 2.4;
                    } else {
                        model = "AB1000";
                        capacity = 0.96;
                    }
                }
                case 'B' -> {
                    model = "AB1000S";
                    capacity = 0.96;
                }
                case 'C' -> {
                    model = "AB2000" + (sn.charAt(3) == 'F' ? 'S' : sn.charAt(3) == 'E' ? 'X' : "");
                    capacity = 1.92;
                }
                case 'F' -> {
                    model = "AB3000";
                    capacity = 2.88;
                }
                default ->
                    throw new RuntimeException("Cant decode serialnumber pleas report error with SN and modelname and capacity: \"" + sn + '\"');
            }
        } else {
            throw new RuntimeException("expected \"sn\" but found: \"" + name + '\"');
        }

        batteryPackCapacity += capacity;

//            "": 300,
        final int packType;
        name = reader.nextName();
        if ("packType".equals(name)) {
            packType = reader.nextInt();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"packType\" but found: \"" + name + '\"');
        }

//            "": 20,
        name = reader.nextName();
        if ("socLevel".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"socLevel\" but found: \"" + name + '\"');
        }

//            "": 1,
        name = reader.nextName();
        if ("state".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"state\" but found: \"" + name + '\"');
        }

//            "": 19,
        name = reader.nextName();
        if ("power".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"power\" but found: \"" + name + '\"');
        }

//            "": 2801,
        name = reader.nextName();
        if ("maxTemp".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"maxTemp\" but found: \"" + name + '\"');
        }

//            "": 4800,
        name = reader.nextName();
        if ("totalVol".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"totalVol\" but found: \"" + name + '\"');
        }

//            "": 4,
        name = reader.nextName();
        if ("batcur".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"batcur\" but found: \"" + name + '\"');
        }

//            "": 322,
        name = reader.nextName();
        if ("maxVol".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"maxVol\" but found: \"" + name + '\"');
        }

//            "": 318,
        name = reader.nextName();
        if ("minVol".equals(name)) {
            reader.skipValue();
//            updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"minVol\" but found: \"" + name + '\"');
        }

//        TODO Config
//            "": 4117
        name = reader.nextName();
        if ("softVersion".equals(name)) {
            reader.skipValue();
        } else {
            throw new RuntimeException("expected \"softVersion\" but found: \"" + name + '\"');
        }
        switch (packType) {
            case 70 -> {
                name = reader.nextName();
                if ("heatState".equals(name)) {
                    reader.skipValue();
                } else {
                    throw new RuntimeException("expected \"heatState\" but found: \"" + name + '\"');
                }
            }
            case 300 -> {
            }
            default ->
                throw new RuntimeException("Unknown packType " + packType);
        }
        if (reader.hasNext()) {
            throw new RuntimeException("Reader has more data! propertyname: " + reader.nextName());
        }
    }

    private void readSolarFlowProperties(JsonReader reader) throws IOException {

        String name = reader.nextName();
        if ("heatState".equals(name)) {
            final int value = reader.nextInt();
            switch (value) {
                case 0 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_HEATING), OnOffType.OFF);
                case 1 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_HEATING), OnOffType.ON);
                default ->
                    throw new RuntimeException("Can''t handle \"heatState\" from json unknown value : " + value);
            }
        } else {
            throw new RuntimeException("expected \"heatState\" but found: \"" + name + '\"');
        }

        // Power out of the battery pack (into inverter?)
        final int packInputPower;
        name = reader.nextName();
        if ("packInputPower".equals(name)) {
            packInputPower = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"packInputPower\" but found: \"" + name + '\"');
        }
        // Power into the battery pack (out of hte pv panels?)
        final int outputPackPower;
        name = reader.nextName();
        if ("outputPackPower".equals(name)) {
            outputPackPower = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"outputPackPower\" but found: \"" + name + '\"');
        }
        if (packInputPower != 0) {
            if (outputPackPower == 0) {
                //Discharging power is positive
                updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_POWER), new DecimalType(packInputPower));
            } else {
                throw new RuntimeException("Error: \"packInputPower\" = " + packInputPower + " and \"outputPackPower\" = " + outputPackPower);
            }
        } else {
            //Charging power is negative
            updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_POWER), new DecimalType(-outputPackPower));
        }

        final int outputHomePower;
        name = reader.nextName();
        if ("outputHomePower".equals(name)) {
            outputHomePower = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"outputHomePower\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("remainOutTime".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_REMAINING_DISCHARGE_TIME), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"remainOutTime\" but found: \"" + name + '\"');
        }

        long deviceState = 0;

        //0: Standby, 1: Charging, 2: Discharging
        name = reader.nextName();
        if ("packState".equals(name)) {
            final int packState = reader.nextInt();
            switch (packState) {
                case 0 -> {
                    //deviceState |= DeviceStateEntries.BATTERY_PACK_STANDBY;
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.BATTERY_PACK_CHARGING;
                case 2 ->
                    deviceState |= DeviceStateEntries.BATTERY_PACK_DISCHARGING;
                default ->
                    throw new RuntimeException("Unknown value for packState: " + packState);
            }
        } else {
            throw new RuntimeException("expected \"packState\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("electricLevel".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_STATE_OF_CHARGE), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"electricLevel\" but found: \"" + name + '\"');
        }

        final int gridInputPower;
        name = reader.nextName();
        if ("gridInputPower".equals(name)) {
            gridInputPower = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"gridInputPower\" but found: \"" + name + '\"');
        }
        if (outputHomePower != 0) {
            if (gridInputPower == 0) {
                //Discharging power or solar power is positive
                updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_POWER), new DecimalType(outputHomePower));
            } else {
                throw new RuntimeException("Error: \"outputHomePower\" = " + outputHomePower + " and \"gridInputPower\" = " + gridInputPower);
            }
        } else {
            //Charging power is negative
            updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_POWER), new DecimalType(-gridInputPower));
        }

        name = reader.nextName();
        if ("solarInputPower".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.PV_POWER), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"solarInputPower\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("solarPower1".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.PV_POWER_PANEL_1), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"solarPower1\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("solarPower2".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.PV_POWER_PANEL_2), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"solarPower2\" but found: \"" + name + '\"');
        }

        switch (zendureDeviceType) {
            case SOLAR_FLOW_800 -> {
                // only 2 PV tracker
            }
            case SOLAR_FLOW_800_PRO -> {
                name = reader.nextName();
                if ("solarPower3".equals(name)) {
                    updateState(new ChannelUID(getThing().getUID(), Channels.PV_POWER_PANEL_3), new DecimalType(reader.nextInt()));
                } else {
                    throw new RuntimeException("expected \"solarPower3\" but found: \"" + name + '\"');
                }

                name = reader.nextName();
                if ("solarPower4".equals(name)) {
                    updateState(new ChannelUID(getThing().getUID(), Channels.PV_POWER_PANEL_4), new DecimalType(reader.nextInt()));
                } else {
                    throw new RuntimeException("expected \"solarPower4\" but found: \"" + name + '\"');
                }
            }
            default ->
                throw new RuntimeException("Cant handle devicetype! please report this as Error!" + zendureDeviceType);
        }

        //Bypass 0: No, 1: Yes ?TODO APL we will see 2 sporadically ?
        name = reader.nextName();
        if ("pass".equals(name)) {
            final int pass = reader.nextInt();
            switch (pass) {
                case 0 -> {
                    // deviceState |= DeviceStateEntries.NOT_BYPASSING;
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.BYPASSING;
                default ->
                    throw new RuntimeException("Unknown value for pass: " + pass + " for ting UID" + thing.getUID()); //TODO to all others...
            }
        } else {
            throw new RuntimeException("expected \"pass\" but found: \"" + name + '\"');
        }

        //  0: No, 1: Reverse flow
        name = reader.nextName();
        if ("reverseState".equals(name)) {
            final int reverseState = reader.nextInt();
            switch (reverseState) {
                case 0 -> {
                    // deviceState.add(DeviceStateEntry.NOT_REVERSE_FLOWING);
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.REVERSE_FLOWING;
                default ->
                    throw new RuntimeException("Unknown value for reverseState: " + reverseState);
            }
        } else {
            throw new RuntimeException("expected \"reverseState\" but found: \"" + name + '\"');
        }

        //  0: No, 1: Calibrating
        name = reader.nextName();
        if ("socStatus".equals(name)) {
            final int socStatus = reader.nextInt();
            switch (socStatus) {
                case 0 -> {
                    //   deviceState |= DeviceStateEntries.BATTERY_NOT_CALIBRATING;
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.BATTERY_CALIBRATING;
                default ->
                    throw new RuntimeException("Unknown value for socStatus: " + socStatus);
            }
        } else {
            throw new RuntimeException("expected \"socStatus\" but found: \"" + name + '\"');
        }

        //        "": 2851,
        name = reader.nextName();
        if ("hyperTmp".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_TEMPERATURE), new DecimalType(reader.nextInt() / 100.0));
        } else {
            throw new RuntimeException("expected \"hyperTmp\" but found: \"" + name + '\"');
        }

        switch (zendureDeviceType) {
            case SOLAR_FLOW_800 -> {
                //no off grid
            }
            case SOLAR_FLOW_800_PRO -> {
                //        "": 0,
                name = reader.nextName();
                if ("gridOffPower".equals(name)) {
                    updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_OFF_GRID_POWER), new DecimalType(reader.nextInt()));
                } else {
                    throw new RuntimeException("expected \"gridOffPower\" but found: \"" + name + '\"');
                }
            }
            default ->
                throw new RuntimeException("Cant handle gridOffPower! please report this as Error! " + zendureDeviceType);
        }

        // 0: Stopped, 1: Battery input, 2: Battery output,
        name = reader.nextName();
        if ("dcStatus".equals(name)) {
            final int dcStatus = reader.nextInt();
            switch (dcStatus) {
                case 0 -> {
                    // deviceState.add(DeviceStateEntry.DC_STOPPED);
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.DC_BATTERY_INPUT;
                case 2 ->
                    deviceState |= DeviceStateEntries.DC_BATTERY_OUTPUT;
                default ->
                    throw new RuntimeException("Unknown value for dcStatus: " + dcStatus);
            }
        } else {
            throw new RuntimeException("expected \"dcStatus\" but found: \"" + name + '\"');
        }

        name = reader.nextName();
        if ("pvStatus".equals(name)) {
            final int pvStatus = reader.nextInt();
            switch (pvStatus) {
                case 0 -> {
                    // deviceState.add(DeviceStateEntry.PV_STOPPED);
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.PV_RUNNING;
                default ->
                    throw new RuntimeException("Unknown value for pvStatus: " + pvStatus);
            }
        } else {
            throw new RuntimeException("expected \"pvStatus\" but found: \"" + name + '\"');
        }

        //0: Stopped, 1: Grid-connected operation, 2: Charging operation
        name = reader.nextName();
        if ("acStatus".equals(name)) {
            final int acStatus = reader.nextInt();
            switch (acStatus) {
                case 0 -> {
                    //   deviceState.add(DeviceStateEntry.AC_STOPPED);
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.AC_GRID_CONNECTED_OPERATION;
                case 2 ->
                    deviceState |= DeviceStateEntries.AC_CHARGING_OPERATION;
                default ->
                    throw new RuntimeException("Unknown value for acStatus: " + acStatus);
            }
        } else {
            throw new RuntimeException("expected \"acStatus\" but found: \"" + name + '\"');
        }

        //        "": 1,
        name = reader.nextName();
        if ("dataReady".equals(name)) {
            final int dataReady = reader.nextInt();
            if (dataReady != 1) {
                LOGGER.log(Level.SEVERE, "dataReady - expected 0, but was: {0}", dataReady);
            }
        } else {
            throw new RuntimeException("expected \"dataReady\" but found: \"" + name + '\"');
        }

        // 0: Not connected, 1: Connected
        name = reader.nextName();
        if ("gridState".equals(name)) {
            final int gridState = reader.nextInt();
            switch (gridState) {
                case 0 -> {
                    // deviceState.add(DeviceStateEntry.GRID_STATE_NOT_CONNECTED);
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.GRID_STATE_CONNECTED;
                default ->
                    throw new RuntimeException("Unknown value for gridState: " + gridState);
            }
        } else {
            throw new RuntimeException("expected \"gridState\" but found: \"" + name + '\"');
        }

        //        "": 4813,
        name = reader.nextName();
        if ("BatVolt".equals(name)) {
            reader.skipValue();
// from HA "BatVolt": ("template", "{{ value / 100 if (value | int) < 32768 else (value | bitwise_xor(0x8000 | int) - 0x8000 | int) / 100 }}", "V", "voltage"),
//            updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_VOLTAGE), new DecimalType(reader.nextInt() / 100.0));
        } else {
            throw new RuntimeException("expected \"BatVolt\" but found: \"" + name + '\"');
        }

        //0: Normal state, 1: Charge limit reached, 2: Discharge limit reached
        name = reader.nextName();
        if ("socLimit".equals(name)) {
            final int socLimit = reader.nextInt();
            switch (socLimit) {
                case 0 -> {
                    // deviceState |= DeviceStateEntries.SOC_NORMAL_STATE;
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.SOC_CHARGE_LIMIT_REACHED;
                case 2 ->
                    deviceState |= DeviceStateEntries.SOC_DISCHARGE_LIMIT_REACHED;
                default ->
                    throw new RuntimeException("Unknown value for socLimit: " + socLimit);
            }
        } else {
            throw new RuntimeException("expected \"socLimit\" but found: \"" + name + '\"');
        }

        // Read/write response acknowledgment        "": 0,
        final int writeRsp;
        name = reader.nextName();
        if ("writeRsp".equals(name)) {
            writeRsp = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"writeRsp\" but found: \"" + name + '\"');
        }

        //0: 0 1: Input, 2: Output
//./Zendure-HA/custom_components/zendure_ha/device.py:        self.acMode = ZendureSelect(self, "acMode", {1: "input", 2: "output"}, self.entityWrite, 1)
//./Zendure-HA/custom_components/zendure_ha/device.py:        await self.doCommand({"properties": {"smartMode": 0 if power == 0 else 1, "acMode": 1, "inputLimit": -power}})
//./Zendure-HA/custom_components/zendure_ha/device.py:        await self.doCommand({"properties": {"smartMode": 0 if power == 0 else 1, "acMode": 2, "outputLimit": power}})
//./Zendure-HA/custom_components/zendure_ha/device.py:        await self.doCommand({"properties": {"smartMode": 0, "acMode": 2, "outputLimit": 0, "inputLimit": 0}})
        final int acMode;
        name = reader.nextName();
        if ("acMode".equals(name)) {
            acMode = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"acMode\" but found: \"" + name + '\"');
        }

        //        "": 160,
        final int inputLimit;
        name = reader.nextName();
        if ("inputLimit".equals(name)) {
            inputLimit = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"inputLimit\" but found: \"" + name + '\"');
        }

        //        "outputLimit": 0,
        final int outputLimit;
        name = reader.nextName();
        if ("outputLimit".equals(name)) {
            outputLimit = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"outputLimit\" but found: \"" + name + '\"');
        }

        switch (acMode) {
            case 0 ->
                updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_DESIRED_POWER), DecimalType.ZERO);
            case 1 ->
                updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_DESIRED_POWER), new DecimalType(-inputLimit));
            case 2 ->
                updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_DESIRED_POWER), new DecimalType(outputLimit));
            default ->
                throw new RuntimeException("Unknown value of acMode: " + acMode);
        }

        //        "": 960,
        name = reader.nextName();
        if ("socSet".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_CHARGE_LEVGEL_MAX), new DecimalType(reader.nextInt() / 10.0));
        } else {
            throw new RuntimeException("expected \"socSet\" but found: \"" + name + '\"');
        }

        //        "": 200,
        name = reader.nextName();
        if ("minSoc".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_DISCHARGE_LEVEL_MIN), new DecimalType(reader.nextInt() / 10.0));
        } else {
            throw new RuntimeException("expected \"minSoc\" but found: \"" + name + '\"');
        }

        //        "": 0,
        name = reader.nextName();
        if ("gridStandard".equals(name)) {
            final int gridStandard = reader.nextInt();
            switch (gridStandard) {
                case 0 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.GRID_STANDARD), GridStandard.GERMANY);
                case 1 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.GRID_STANDARD), GridStandard.FRANCE);
                case 2 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.GRID_STANDARD), GridStandard.AUSTRIA);
                default ->
                    throw new RuntimeException("Unkown value for GridStandard " + gridStandard);
            }
        } else {
            throw new RuntimeException("expected \"gridStandard\" but found: \"" + name + '\"');
        }

        //0: Disabled, 1: Allowed reverse flow, 2: Forbidden reverse flow 
        name = reader.nextName();
        if ("gridReverse".equals(name)) {
            final int gridReverse = reader.nextInt();
            switch (gridReverse) {
                case 0 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.GRID_REVERSE_FLOW_MODE), GridReverseFlowMode.DISABLED);
                case 1 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.GRID_REVERSE_FLOW_MODE), GridReverseFlowMode.REVERSE_FLOW_ALLOWED);
                case 2 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.GRID_REVERSE_FLOW_MODE), GridReverseFlowMode.REVERSE_FLOW_FORBIDDEN);
                default ->
                    throw new RuntimeException("Unknown value for gridReverse: " + gridReverse);
            }
        } else {
            throw new RuntimeException("expected \"gridReverse\" but found: \"" + name + '\"');
        }

        //Maximum output power limit        "": 800,
        name = reader.nextName();
        if ("inverseMaxPower".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_MAX_POWER_LIMIT_OUT), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"inverseMaxPower\" but found: \"" + name + '\"');
        }

        //        "": 0,
        name = reader.nextName();
        if ("lampSwitch".equals(name)) {
            final int value = reader.nextInt();
            switch (value) {
                case 0 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_LAMP_SWITCH), OnOffType.OFF);
                case 1 ->
                    updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_LAMP_SWITCH), OnOffType.ON);
                default ->
                    throw new RuntimeException("Can''t handle \"heatState\" from json unknown value : " + value);
            }
        } else {
            throw new RuntimeException("expected \"lampSwitch\" but found: \"" + name + '\"');
        }

        switch (zendureDeviceType) {
            case SOLAR_FLOW_800 -> {
                //no off grid
            }
            case SOLAR_FLOW_800_PRO -> {
                //        "gridOffMode": 2,
                name = reader.nextName();
                if ("gridOffMode".equals(name)) {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_OFF_GRID_MODE), GridOffMode.NORMAL);
                        case 1 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_OFF_GRID_MODE), GridOffMode.ECO);
                        case 2 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_OFF_GRID_MODE), GridOffMode.OFF);
                        default ->
                            throw new RuntimeException("Can''t handle \"gridOffMode\" from json unknown value : " + value);
                    }
                } else {
                    throw new RuntimeException("expected \"gridOffMode\" but found: \"" + name + '\"');
                }
            }
            default ->
                throw new RuntimeException("Cant handle gridOffPower! please report this as Error! " + zendureDeviceType);
        }

        //        "": 2, APL: 2 -> MQTT?
        name = reader.nextName();
        if ("IOTState".equals(name)) {
            final int IOTState = reader.nextInt();
            if (IOTState != 2) {
                LOGGER.log(Level.SEVERE, "IOTState - expected 0, but was: {0}", IOTState);
            }
        } else {
            throw new RuntimeException("expected \"IOTState\" but found: \"" + name + '\"');
        }

        switch (zendureDeviceType) {
            case SOLAR_FLOW_800 -> {
                //no off grid
            }
            case SOLAR_FLOW_800_PRO -> {
                //        "": 1,
                name = reader.nextName();
                if ("Fanmode".equals(name)) {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_FAN_MODE), OnOffType.OFF);
                        case 1 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_FAN_MODE), OnOffType.ON);
                        default ->
                            throw new RuntimeException("Can''t handle \"heatState\" from json unknown value : " + value);
                    }
                } else {
                    throw new RuntimeException("expected \"Fanmode\" but found: \"" + name + '\"');
                }

                //        "": 0,
                name = reader.nextName();
                if ("Fanspeed".equals(name)) {
                    updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_FAN_SPEED), new DecimalType(reader.nextInt()));
                } else {
                    throw new RuntimeException("expected \"Fanspeed\" but found: \"" + name + '\"');
                }

                //        "": 0,
                name = reader.nextName();
                if ("bindstate".equals(name)) {
                    final int bindstate = reader.nextInt();
                    if (bindstate != 0) {
                        LOGGER.log(Level.SEVERE, "bindState - expected 0, but was: {0}", bindstate);
                    }
                } else {
                    throw new RuntimeException("expected \"bindstate\" but found: \"" + name + '\"');
                }
            }
            default ->
                throw new RuntimeException("Cant handle Fanmode,Fanspeed,bindstate! please report this as Error! " + zendureDeviceType);
        }

        //        "": 0,
        name = reader.nextName();
        if ("factoryModeState".equals(name)) {
            final int factoryModeState = reader.nextInt();
            if (factoryModeState != 0) {
                LOGGER.log(Level.SEVERE, "factoryModeState - expected 0, but was: {0}", factoryModeState);
            }
        } else {
            throw new RuntimeException("expected \"factoryModeState\" but found: \"" + name + '\"');
        }

        //        "": 0,
        name = reader.nextName();
        if ("OTAState".equals(name)) {
            final int OTAState = reader.nextInt();
            if (OTAState != 0) {
                LOGGER.log(Level.SEVERE, "OTAState - expected 0, but was: {0}", OTAState);
            }
        } else {
            throw new RuntimeException("expected \"OTAState\" but found: \"" + name + '\"');
        }

        //        "": 0,
        name = reader.nextName();
        if ("LCNState".equals(name)) {
            final int LCNState = reader.nextInt();
            if (LCNState != 0) {
                LOGGER.log(Level.SEVERE, "LCNState - expected 0, but was: {0}", LCNState);
            }
        } else {
            throw new RuntimeException("expected \"LCNState\" but found: \"" + name + '\"');
        }

        //        "": 0,
        name = reader.nextName();
        if ("oldMode".equals(name)) {
            final int oldMode = reader.nextInt();
            if (oldMode != 0) {
                LOGGER.log(Level.SEVERE, "oldMode - expected 0, but was: {0}", oldMode);
            }
        } else {
            throw new RuntimeException("expected \"oldMode\" but found: \"" + name + '\"');
        }

        //        "": 0,
        name = reader.nextName();
        if ("VoltWakeup".equals(name)) {
            final int VoltWakeup = reader.nextInt();
            if (VoltWakeup != 0) {
                LOGGER.log(Level.SEVERE, "VoltWakeup - expected 0, but was: {0}", VoltWakeup);
            }
        } else {
            throw new RuntimeException("expected \"VoltWakeup\" but found: \"" + name + '\"');
        }

        //        "": 1763977019,
        final int ts;
        name = reader.nextName();
        if ("ts".equals(name)) {
            ts = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"ts\" but found: \"" + name + '\"');
        }

        switch (zendureDeviceType) {
            case SOLAR_FLOW_800 -> {
                //        "": 0,
                name = reader.nextName();
                if ("bindstate".equals(name)) {
                    final int bindstate = reader.nextInt();
                    if (bindstate != 0) {
                        LOGGER.log(Level.SEVERE, "bindState - expected 0, but was: {0}", bindstate);
                    }
                } else {
                    throw new RuntimeException("expected \"bindstate\" but found: \"" + name + '\"');
                }
            }
            case SOLAR_FLOW_800_PRO -> {

            }
            default ->
                throw new RuntimeException("Cant handle bindstate betrwen ts and tsZone! please report this as Error! " + zendureDeviceType);
        }

        //        "": 13,
        final int tsZone;
        name = reader.nextName();
        if ("tsZone".equals(name)) {
            tsZone = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"tsZone\" but found: \"" + name + '\"');
        }

        switch (zendureDeviceType) {
            case SOLAR_FLOW_800 -> {
                //        "": 800, 
                name = reader.nextName();
                if ("chargeMaxLimit".equals(name)) {
                    updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_MAX_POWER_LIMIT_IN), new DecimalType(reader.nextInt()));
                } else {
                    throw new RuntimeException("expected \"chargeMaxLimit\" but found: \"" + name + '\"');
                }

                //        "": 0,
                name = reader.nextName();
                if ("smartMode".equals(name)) {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_SMART_MODE), OnOffType.OFF);
                        case 1 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_SMART_MODE), OnOffType.ON);
                        default ->
                            throw new RuntimeException("Can''t handle \"heatState\" from json unknown value : " + value);
                    }
                } else {
                    throw new RuntimeException("expected \"smartMode\" but found: \"" + name + '\"');
                }
            }
            case SOLAR_FLOW_800_PRO -> {
                //        "": 0,
                name = reader.nextName();
                if ("smartMode".equals(name)) {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_SMART_MODE), OnOffType.OFF);
                        case 1 ->
                            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_SMART_MODE), OnOffType.ON);
                        default ->
                            throw new RuntimeException("Can''t handle \"heatState\" from json unknown value : " + value);
                    }
                } else {
                    throw new RuntimeException("expected \"smartMode\" but found: \"" + name + '\"');
                }

                //        "": 1000, 
                name = reader.nextName();
                if ("chargeMaxLimit".equals(name)) {
                    updateState(new ChannelUID(getThing().getUID(), Channels.INVERTER_MAX_POWER_LIMIT_IN), new DecimalType(reader.nextInt()));
                } else {
                    throw new RuntimeException("expected \"chargeMaxLimit\" but found: \"" + name + '\"');
                }
            }
            default ->
                throw new RuntimeException("Cant handle Fanmode,Fanspeed,bindstate! please report this as Error! " + zendureDeviceType);
        }

        //        "": 2,
        name = reader.nextName();
        if ("packNum".equals(name)) {
            packNum = reader.nextInt();
            updateState(new ChannelUID(getThing().getUID(), Channels.NUMBER_OF_BATTERIES_IN_PACK), new DecimalType(packNum));
        } else {
            throw new RuntimeException("expected \"packNum\" but found: \"" + name + '\"');
        }

        //        "": -58,
        name = reader.nextName();
        if ("rssi".equals(name)) {
            updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_WLAN_RSSI), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"rssi\" but found: \"" + name + '\"');
        }

        //        "": 0
        name = reader.nextName();
        if ("is_error".equals(name)) {
            final int is_error = reader.nextInt();
            switch (is_error) {
                case 0 -> {
                    //  deviceState.add(DeviceStateEntry.NO_ERROR_PENDING);
                }
                case 1 ->
                    deviceState |= DeviceStateEntries.ERROR_PENDING;
                default ->
                    throw new RuntimeException("Can''t handle \"is_error\" from json unknown value : " + is_error);
            }
        } else {
            throw new RuntimeException("expected \"is_error\" but found: \"" + name + '\"');
        }

        updateState(new ChannelUID(getThing().getUID(), Channels.DEVICE_STATE), new DecimalType(deviceState));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //Ignore Refresh Request? or schedule one? 
        if (command == RefreshType.REFRESH) {
            return;
        }
        try {
            final URI uri = new URI(config.url + "/properties/write");
            HttpURLConnection httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setDoOutput(true);
            final JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(httpConnection.getOutputStream()));
            jsonWriter.beginObject();
            jsonWriter.name("sn");
            jsonWriter.value(serialNumber);
            jsonWriter.name("properties");
            jsonWriter.beginObject();
            switch (channelUID.getId()) {
                case Channels.DEVICE_LAMP_SWITCH -> {
                    if (command instanceof DecimalType decimalType) {
                        jsonWriter.name("lampSwitch");
                        final int lampState = decimalType.intValue();
                        switch (lampState) {
                            case 0 ->
                                jsonWriter.value(0);
                            case 1 ->
                                jsonWriter.value(1);
                            default ->
                                throw new RuntimeException("Unknow state for channel \"" + channelUID + "\" : " + lampState);
                        }
                    } else {
                        throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    }
                }
                case Channels.GRID_REVERSE_FLOW_MODE -> {
                    if (command instanceof DecimalType decimalType) {
                        jsonWriter.name("gridReverse");
                        final int gridReverse = decimalType.intValue();
                        switch (gridReverse) {
                            case 0 ->
                                jsonWriter.value(0);
                            case 1 ->
                                jsonWriter.value(1);
                            case 2 ->
                                jsonWriter.value(2);
                            default ->
                                throw new RuntimeException("Unknow state for channel \"" + channelUID + "\" : " + gridReverse);
                        }
                    } else {
                        throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    }
                }
                case Channels.GRID_STANDARD -> {
                    if (command instanceof DecimalType decimalType) {
                        jsonWriter.name("gridStandard");
                        final int gridStandard = decimalType.intValue();
                        switch (gridStandard) {
                            case 0 ->
                                jsonWriter.value(0);
                            case 1 ->
                                jsonWriter.value(1);
                            case 2 ->
                                jsonWriter.value(2);
                            default ->
                                throw new RuntimeException("Unknow state for channel \"" + channelUID + "\" : " + gridStandard);
                        }
                    } else {
                        throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    }
                }
                case Channels.INVERTER_DESIRED_POWER -> {
                    final int value = switch (command) {
                        case QuantityType quantityType ->
                            quantityType.intValue();
                        case DecimalType decimalType ->
                            decimalType.intValue();
                        default ->
                            throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    };

                    jsonWriter.name("smart");
                    jsonWriter.value(1);
                    jsonWriter.name("acMode");
                    if (value == 0) {
                        jsonWriter.value(0);
                        jsonWriter.name("inputLimit");
                        jsonWriter.value(0);
                        jsonWriter.name("outputLimit");
                        jsonWriter.value(0);
                    } else if (value < 0) {
                        //Input to Inverter (Energyflow from inverter to battery pack)
                        //Zendure Zen Cloud sets this to max 
                        //SolarFlow 800     max 1200W
                        //SolarFlow 800 Pro max 2000W
                        //but inverseMaxPower can only be set to max 800W in Germany???
                        jsonWriter.value(0);
                        jsonWriter.name("inputLimit");
                        jsonWriter.value(-value);
                        jsonWriter.name("outputLimit");
                        jsonWriter.value(0);
                    } else if (value > 0) {
                        //Output to Battery
                        jsonWriter.value(0);
                        jsonWriter.name("inputLimit");
                        jsonWriter.value(0);
                        jsonWriter.name("outputLimit");
                        jsonWriter.value(value);
                    }
                }
                case Channels.INVERTER_MAX_POWER_LIMIT_IN -> {
                    final int value = switch (command) {
                        case QuantityType quantityType ->
                            quantityType.intValue();
                        case DecimalType decimalType ->
                            decimalType.intValue();
                        default ->
                            throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    };
                        jsonWriter.name("chargeMaxLimit");
                        jsonWriter.value(value);
                }
                case Channels.INVERTER_MAX_POWER_LIMIT_OUT -> {
                    final int value = switch (command) {
                        case QuantityType quantityType ->
                            quantityType.intValue();
                        case DecimalType decimalType ->
                            decimalType.intValue();
                        default ->
                            throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    };
                        jsonWriter.name("inverseMaxPower");
                        jsonWriter.value(value);
                }
                case Channels.INVERTER_OFF_GRID_MODE -> {
                    if (command instanceof DecimalType decimalType) {
                        jsonWriter.name("gridOffMode");
                        int gridOffMode = decimalType.intValue();
                        switch (gridOffMode) {
                            case 0 ->
                                jsonWriter.value(0);
                            case 1 ->
                                jsonWriter.value(1);
                            case 2 ->
                                jsonWriter.value(2);
                            default ->
                                throw new RuntimeException("Unknow state for channel \"" + channelUID + "\" : " + gridOffMode);
                        }
                    } else {
                        throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    }
                }
                case Channels.BATTERY_PACK_DISCHARGE_LEVEL_MIN -> {
                    final float value = switch (command) {
                        case QuantityType quantityType ->
                            quantityType.intValue();
                        case DecimalType decimalType ->
                            decimalType.intValue();
                        default ->
                            throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    };
                        jsonWriter.name("minSoc");
                        jsonWriter.value(Math.round(value * 10));
                }
                case Channels.BATTERY_PACK_CHARGE_LEVGEL_MAX -> {
                    final float value = switch (command) {
                        case QuantityType quantityType ->
                            quantityType.intValue();
                        case DecimalType decimalType ->
                            decimalType.intValue();
                        default ->
                            throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
                    };
                        jsonWriter.name("socSet");
                        jsonWriter.value(Math.round(value * 10));
                }
                default ->
                    throw new RuntimeException("Unknow channel \"" + channelUID + "\" : " + command + "  " + command.getClass().getCanonicalName());
            }
            jsonWriter.endObject();
            jsonWriter.endObject();
            jsonWriter.flush();
            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("Can't post data to device: " + httpConnection.getResponseCode() + " " + httpConnection);
            }
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, (String) null, ex);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, (String) null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, (String) null, ex);
        }
    }

    @Override
    public void dispose() {
        LOGGER.log(Level.INFO, "Close Zendure handler for: {0}", config.url);

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        LOGGER.log(Level.INFO, "Zendure handler closed for: {0}", config.url);
    }

}
