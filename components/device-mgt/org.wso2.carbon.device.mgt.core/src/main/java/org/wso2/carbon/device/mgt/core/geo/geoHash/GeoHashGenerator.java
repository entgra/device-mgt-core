/*
 * Copyright (c) 2018-2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.geo.geoHash;

import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.geo.service.GeoCoordinate;

public class GeoHashGenerator {

    private static final String BASE_32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int GEOHASH_LENGTH = 16;

    private GeoHashGenerator() {
    }

    private static int divideRangeByValue(double value, double[] range) {
        double mid = middle(range);
        if (value >= mid) {
            range[0] = mid;
            return 1;
        } else {
            range[1] = mid;
            return 0;
        }
    }

    private static void divideRangeByBit(int bit, double[] range) {
        double mid = middle(range);
        if (bit > 0) {
            range[0] = mid;
        } else {
            range[1] = mid;
        }
    }

    private static double middle(double[] range) {
        return (range[0] + range[1]) / 2;
    }

    public static String encodeGeohash(double latitude, double longitude) {
        double[] latRange = new double[]{-90.0, 90.0};
        double[] lonRange = new double[]{-180.0, 180.0};
        boolean isEven = true;
        int bit = 0;
        int base32CharIndex = 0;
        StringBuilder geohash = new StringBuilder();

        while (geohash.length() < GEOHASH_LENGTH) {
            if (isEven) {
                base32CharIndex = (base32CharIndex << 1) | divideRangeByValue(longitude, lonRange);
            } else {
                base32CharIndex = (base32CharIndex << 1) | divideRangeByValue(latitude, latRange);
            }

            isEven = !isEven;

            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE_32.charAt(base32CharIndex));
                bit = 0;
                base32CharIndex = 0;
            }
        }

        return geohash.toString();
    }

    public static String encodeGeohash(DeviceLocation deviceLocation) {
        return encodeGeohash(deviceLocation.getLatitude(), deviceLocation.getLongitude());
    }

    public static GeoCoordinate decodeGeohash(String geohash) {
        double[] latRange = new double[]{-90.0, 90.0};
        double[] lonRange = new double[]{-180.0, 180.0};
        boolean isEvenBit = true;

        for (int i = 0; i < geohash.length(); i++) {
            int base32CharIndex = BASE_32.indexOf(geohash.charAt(i));
            for (int j = 4; j >= 0; j--) {
                if (isEvenBit) {
                    divideRangeByBit((base32CharIndex >> j) & 1, lonRange);
                } else {
                    divideRangeByBit((base32CharIndex >> j) & 1, latRange);
                }
                isEvenBit = !isEvenBit;
            }
        }
        return new GeoCoordinate(middle(latRange), middle(lonRange));
    }

}
