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
package de.ibapl.openhab.binding.zendure;

import com.google.gson.stream.JsonReader;
import java.io.*;
import java.net.*;
import java.time.Instant;
import org.openhab.core.types.State;

public class SimpleHttp {

    private static void readPackProperties(JsonReader reader, int index) throws IOException {
        while (reader.hasNext()) {
            final String name = reader.nextName();
        
        switch (name) {
                case "sn" -> System.out.println(name + " = " + reader.nextInt());
            }
/*
                System.out.println(packdata.getAsJsonPrimitive("sn").getAsString());
                System.out.println(PackType.valueOf(packdata.getAsJsonPrimitive("packType").getAsInt()));
                System.out.println(packdata.getAsJsonPrimitive("socLevel").getAsInt());
                System.out.println(BatteriePackState.valueOf(packdata.getAsJsonPrimitive("state").getAsInt()));
                System.out.println(packdata.getAsJsonPrimitive("power").getAsInt());
                System.out.println(0.1f * packdata.getAsJsonPrimitive("maxTemp").getAsInt());
                System.out.println(packdata.getAsJsonPrimitive("totalVol").getAsInt());
                System.out.println(0.1f * packdata.getAsJsonPrimitive("batcur").getAsInt());
                System.out.println(0.1f * packdata.getAsJsonPrimitive("maxVol").getAsInt());
                System.out.println(0.1f * packdata.getAsJsonPrimitive("minVol").getAsInt());
                System.out.println(packdata.getAsJsonPrimitive("softVersion").getAsInt());

        */
                //NUr PackType == 70 ? System.out.println(HeatState.valueOf(packdata.getAsJsonPrimitive("heatState").getAsInt()));
        }
    }

        private static void readDevProperties(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            final String name = reader.nextName();
        
        switch (name) {
                case "heatState", "packInputPower","outputPackPower", "outputHomePower", "remainOutTime" -> System.out.println(name + " = " + reader.nextInt());
                case "sn", "product" -> System.out.println(name + " = " + reader.nextString());
            }
/*
        System.out.println(Heating.valueOf(deviceProperties.getAsJsonPrimitive("").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive().getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive().getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive().getAsInt());
        System.out.println(BatteriePackState.valueOf(deviceProperties.getAsJsonPrimitive("packState").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("electricLevel").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("gridInputPower").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("solarInputPower").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("solarPower1").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("solarPower2").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("solarPower3").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("solarPower4").getAsInt());
        System.out.println(Pass.valueOf(deviceProperties.getAsJsonPrimitive("pass").getAsInt()));
        System.out.println(ReverseState.valueOf(deviceProperties.getAsJsonPrimitive("reverseState").getAsInt()));
        System.out.println(SocState.valueOf(deviceProperties.getAsJsonPrimitive("socStatus").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("hyperTmp").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("gridOffPower").getAsInt());
        System.out.println(DcState.valueOf(deviceProperties.getAsJsonPrimitive("dcStatus").getAsInt()));
        System.out.println(PvState.valueOf(deviceProperties.getAsJsonPrimitive("pvStatus").getAsInt()));
        System.out.println(AcState.valueOf(deviceProperties.getAsJsonPrimitive("acStatus").getAsInt()));
        System.out.println(DataReady.valueOf(deviceProperties.getAsJsonPrimitive("dataReady").getAsInt()));
        System.out.println(GridState.valueOf(deviceProperties.getAsJsonPrimitive("gridState").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("BatVolt").getAsInt());
        System.out.println(SocLimit.valueOf(deviceProperties.getAsJsonPrimitive("socLimit").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("writeRsp").getAsInt());
        System.out.println(AcMode.valueOf(deviceProperties.getAsJsonPrimitive("acMode").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("inputLimit").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("outputLimit").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("socSet").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("minSoc").getAsInt());
        System.out.println(GridStandard.valueOf(deviceProperties.getAsJsonPrimitive("gridStandard").getAsInt()));
        System.out.println(GridReverseFlow.valueOf(deviceProperties.getAsJsonPrimitive("gridReverse").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("inverseMaxPower").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("lampSwitch").getAsInt());
        System.out.println(GridOffMode.valueOf(deviceProperties.getAsJsonPrimitive("gridOffMode").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("IOTState").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("Fanmode").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("Fanspeed").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("bindstate").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("factoryModeState").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("OTAState").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("LCNState").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("oldMode").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("VoltWakeup").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("ts").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("tsZone").getAsInt());
        System.out.println(SmartMode.valueOf(deviceProperties.getAsJsonPrimitive("smartMode").getAsInt()));
        System.out.println(deviceProperties.getAsJsonPrimitive("chargeMaxLimit").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("packNum").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("rssi").getAsInt());
        System.out.println(deviceProperties.getAsJsonPrimitive("is_error").getAsInt());
        */
    }
    }

    public enum Heating {
        OFF,
        ON;

        private static Heating valueOf(int value) {
            return switch (value) {
                case 0 ->
                    OFF;
                case 1 ->
                    ON;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum BatteriePackState implements State {
        STANDBY("standby"),
        CHARGING("charging"),
        DISCHARGING("discharging");

        public final String label;

        private BatteriePackState(String label) {
            this.label = label;
        }

        private static BatteriePackState valueOf(int value) {
            return switch (value) {
                case 0 ->
                    STANDBY;
                case 1 ->
                    CHARGING;
                case 2 ->
                    DISCHARGING;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }

        @Override
        public String format(String pattern) {
            return String.format(pattern, label);
        }

        @Override
        public String toFullString() {
            return label;
        }
    }

    public enum Pass {
        NO,
        YES;

        private static Pass valueOf(int value) {
            return switch (value) {
                case 0 ->
                    NO;
                case 1 ->
                    YES;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum ReverseState {
        NO,
        REVERSE_FLOW;

        private static ReverseState valueOf(int value) {
            return switch (value) {
                case 0 ->
                    NO;
                case 1 ->
                    REVERSE_FLOW;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum SocState {
        NO,
        CALIBRATING;

        private static SocState valueOf(int value) {
            return switch (value) {
                case 0 ->
                    NO;
                case 1 ->
                    CALIBRATING;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum AcState {
        STOPPED,
        GRID_CONNECTED_OPERATION,
        CHARGIN_OPERATION;

        private static AcState valueOf(int value) {
            return switch (value) {
                case 0 ->
                    STOPPED;
                case 1 ->
                    GRID_CONNECTED_OPERATION;
                case 2 ->
                    CHARGIN_OPERATION;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum DcState {
        STOPPED,
        BATTERY_INPUT,
        BATTERY_OUTPUT;

        private static DcState valueOf(int value) {
            return switch (value) {
                case 0 ->
                    STOPPED;
                case 1 ->
                    BATTERY_INPUT;
                case 2 ->
                    BATTERY_OUTPUT;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum PvState {
        STOPPED,
        RUNNING;

        private static PvState valueOf(int value) {
            return switch (value) {
                case 0 ->
                    STOPPED;
                case 1 ->
                    RUNNING;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum DataReady {
        NO,
        YES;

        private static DataReady valueOf(int value) {
            return switch (value) {
                case 0 ->
                    NO;
                case 1 ->
                    YES;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum GridState {
        NOT_CONNECTED,
        CONNECTED;

        private static GridState valueOf(int value) {
            return switch (value) {
                case 0 ->
                    NOT_CONNECTED;
                case 1 ->
                    CONNECTED;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum AcMode {
        INPUT,
        OUTPUT;

        private static AcMode valueOf(int value) {
            return switch (value) {
                case 1 ->
                    INPUT;
                case 2 ->
                    OUTPUT;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum GridStandard {
        GERMANY,
        FRANCE,
        AUSTRIA;

        private static GridStandard valueOf(int value) {
            return switch (value) {
                case 0 ->
                    GERMANY;
                case 1 ->
                    FRANCE;
                case 2 ->
                    AUSTRIA;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum GridReverseFlow {
        DISABLED,
        ALLOWED,
        FORBIDDEN;

        private static GridReverseFlow valueOf(int value) {
            return switch (value) {
                case 0 ->
                    DISABLED;
                case 1 ->
                    ALLOWED;
                case 2 ->
                    FORBIDDEN;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum GridOffMode {
        NORMAL,
        ECO,
        OFF;

        private static GridOffMode valueOf(int value) {
            return switch (value) {
                case 0 ->
                    NORMAL;
                case 1 ->
                    ECO;
                case 2 ->
                    OFF;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum SmartMode {
        WRITE_TO_MEM_AND_FLASH,
        WRITE_ONLY_TO_MEM;

        private static SmartMode valueOf(int value) {
            return switch (value) {
                case 0 ->
                    WRITE_TO_MEM_AND_FLASH;
                case 1 ->
                    WRITE_ONLY_TO_MEM;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum PackType {
        _70,
        _300;

        private static PackType valueOf(int value) {
            return switch (value) {
                case 70 ->
                    _70;
                case 300 ->
                    _300;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public enum SocLimit {
        NORMAL,
        CHARGE_LIMIT_REACHED,
        DISCHARGE_LIMIT_REACHED;

        private static SocLimit valueOf(int value) {
            return switch (value) {
                case 0 ->
                    NORMAL;
                case 1 ->
                    CHARGE_LIMIT_REACHED;
                case 2 ->
                    DISCHARGE_LIMIT_REACHED;
                default ->
                    throw new IllegalArgumentException("Can't handle value : " + value);
            };
        }
    }

    public static void main(String[] args) throws Exception {

        // GET 请求
        URL getUrl = new URL("http://solarflow-800-pro/properties/report");
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");
        System.out.println("GET Response Code: " + getConn.getResponseCode());
        System.out.println("Msg: ");
        //  System.out.writeBytes(getConn.getInputStream().readAllBytes());

        JsonReader reader = new JsonReader(new InputStreamReader(getConn.getInputStream()));
        if (!reader.hasNext()) {
            throw new RuntimeException("Empty!");
        }
        reader.beginObject();
        while (reader.hasNext()) {
            final String name = reader.nextName();
            switch (name) {
                case "timestamp" -> System.out.println(name + " = " + Instant.ofEpochMilli(reader.nextLong()));
                case "messageId", "version" -> System.out.println(name + " = " + reader.nextInt());
                case "sn", "product" -> System.out.println(name + " = " + reader.nextString());
                case "properties" -> {
                    reader.beginObject();
                    readDevProperties(reader);
                    reader.endObject();
                }
                case "packData"-> {
                    int i = 0;
                    reader.beginArray();
                    while (reader.hasNext()) {
                    reader.beginObject();
                    readPackProperties(reader, i++);
                    reader.endObject();
                    
                    }
                    reader.endArray();
                    
                }
            
            }
    }
        reader.endObject();

            /*
Solar Flow 800
http://solarflow-800/properties/report
{
 "timestamp":1762531064,
 "messageId":2,
  "sn":"WOB1NTMCN030166",
  "version":2,
  "product":"solarFlow800",
  "properties":
   {
    "heatState":0,
    "packInputPower":0,
    "outputPackPower":0,
    "outputHomePower":0,
    "remainOutTime":59940,
    "packState":0,
    "electricLevel":20,
    "gridInputPower":0,
    "solarInputPower":0,
    "solarPower1":0,
    "solarPower2":0,
    "pass":0,
    "reverseState":0,
    "socStatus":0,
    "hyperTmp":2861,
    "dcStatus":0,
    "pvStatus":1,
    "acStatus":0,
    "dataReady":1,
    "gridState":1,
    "BatVolt":4959,
    "socLimit":2,
    "writeRsp":0,
    "acMode":2,
    "inputLimit":284,
    "outputLimit":0,
    "socSet":960,
    "minSoc":200,
    "gridStandard":0,
    "gridReverse":1,
    "inverseMaxPower":800,
    "lampSwitch":1,
    "IOTState":2,
    "factoryModeState":0,
    "OTAState":0,
    "LCNState":0,
    "oldMode":0,
    "VoltWakeup":0,
    "ts":1762531060,
    "bindstate":0,
    "tsZone":13,
    "chargeMaxLimit":800,
    "smartMode":0,
    "packNum":1,
    "rssi":-75,
    "is_error":0
   },
   "packData":
    [
     {
      "sn":"CO4EHNA9N234526",
      "packType":70,
      "socLevel":20,
      "state":0,
      "power":0,
      "maxTemp":2831,
      "totalVol":4940,
      "batcur":0,
      "maxVol":330,
      "minVol":329,
      "softVersion":4117,
      "heatState":0
     }
    ]
}

             */
 /*
        // POST 请求
        URL postUrl = new URL("http://<server-ip>/properties/write");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String json = "{\"sn\": \"your_device_sn\", \"properties\": {\"acMode\": 2}}";
        try (OutputStream os = postConn.getOutputStream()) {
            os.write(json.getBytes());
        }
        System.out.println("POST Response Code: " + postConn.getResponseCode());
             */
        }
    }
