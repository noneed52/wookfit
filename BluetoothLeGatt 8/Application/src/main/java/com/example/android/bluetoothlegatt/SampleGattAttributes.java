/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String GATT_PARAMETER_CONTROL_SERVICE_UUID = "9fdc9c81-fffe-709d-e511-b4719ad35f2f";
    public static String GATT_SENSOR_DATA_READ_UUID = "9fdc9c81-fffe-709d-e511-b471cad55f2f";
    public static String GATT_DATA_READ_UUID = "9fdc9c81-fffe-709d-e511-b471b8d85f2f";
    public static String SNU_REPORTER_SERVICE = "9fdc9c81-fffe-709d-e511-b47162cf5f2f";

    static {
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "GAP Services");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "GATT Services");
        attributes.put(SNU_REPORTER_SERVICE, "SNU REPORTER SERVICE");
        // Sample Characteristics.
        attributes.put(GATT_PARAMETER_CONTROL_SERVICE_UUID, "CONTROL_SERVICE_UUID");
        attributes.put(GATT_SENSOR_DATA_READ_UUID, "SENSOR_READ_UUID");
        attributes.put(GATT_DATA_READ_UUID, "DATA_READ_UUID");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
