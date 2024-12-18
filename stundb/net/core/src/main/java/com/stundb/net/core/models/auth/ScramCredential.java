/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stundb.net.core.models.auth;

/**
 * This class was taken from Apache Kafka org.apache.kafka.common.security.scram.ScramCredential. It
 * has been heavily modified for the needs of this project.
 *
 * <p>SCRAM credential class that encapsulates the credential data persisted for each user that is
 * accessible to the server. See <a href="https://tools.ietf.org/html/rfc5802#section-5">RFC
 * rfc5802</a> for details.
 */
public record ScramCredential(byte[] salt, byte[] serverKey, byte[] storedKey, int iterations) {

    public static ScramCredential toScramCredential(
            String salt, String serverKey, String storedKey, int iterations) {
        return new ScramCredential(
                hexToByteArray(salt),
                hexToByteArray(serverKey),
                hexToByteArray(storedKey),
                iterations);
    }

    private static String arrayToHex(byte[] array) {
        var sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static byte[] hexToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2]; // Each pair of hex digits represents one byte
        for (int i = 0; i < length; i += 2) {
            int byteValue = Integer.parseInt(hexString.substring(i, i + 2), 16);
            byteArray[i / 2] = (byte) byteValue;
        }
        return byteArray;
    }

    public String toString(String username) {
        return String.format(
                "%s,%s,%s,%s,%d",
                username,
                arrayToHex(salt),
                arrayToHex(serverKey),
                arrayToHex(storedKey),
                iterations);
    }

    @Override
    public String toString() {
        return String.format(
                "%s,%s,%s,%d",
                arrayToHex(salt), arrayToHex(serverKey), arrayToHex(storedKey), iterations);
    }
}
