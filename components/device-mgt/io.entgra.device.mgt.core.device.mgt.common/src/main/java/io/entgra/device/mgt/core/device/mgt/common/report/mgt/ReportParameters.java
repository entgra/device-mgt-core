/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.common.report.mgt;

import com.google.gson.Gson;

import java.util.Map;

public class ReportParameters {
    String designFile;
    String reportType;
    Map<String, Object> parameters;

    public String getDesignFile() {
        return designFile;
    }

    public void setDesignFile(String designFile) {
        this.designFile = designFile;
    }

    public String getReportType() {return reportType;}

    public void setReportType(String reportType) {this.reportType = reportType;}

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getJSONString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
