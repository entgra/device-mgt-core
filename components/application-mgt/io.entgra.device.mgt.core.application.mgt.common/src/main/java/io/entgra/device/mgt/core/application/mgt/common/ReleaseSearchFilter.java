/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.application.mgt.common;

public class ReleaseSearchFilter {
    private String packageName;
    private boolean isPackageNameFullMatch = false;
    private String releaseType;
    private String version;
    private boolean isVersionFullMatch = true;
    private int applicationId;
    private int limit = 20;
    private int offset = 0;
    private boolean isLimitAndOffsetDisable = false;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isPackageNameFullMatch() {
        return isPackageNameFullMatch;
    }

    public void setPackageNameFullMatch(boolean packageNameFullMatch) {
        isPackageNameFullMatch = packageNameFullMatch;
    }

    public boolean isVersionFullMatch() {
        return isVersionFullMatch;
    }

    public void setVersionFullMatch(boolean versionFullMatch) {
        isVersionFullMatch = versionFullMatch;
    }

    public boolean isLimitAndOffsetDisable() {
        return isLimitAndOffsetDisable;
    }
}
