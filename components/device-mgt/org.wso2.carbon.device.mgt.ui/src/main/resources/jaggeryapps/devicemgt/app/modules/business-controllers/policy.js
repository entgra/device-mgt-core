/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2018, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

var policyModule;
policyModule = function () {
    var log = new Log("/app/modules/business-controllers/policy.js");

    var constants = require('/app/modules/constants.js');
    var utility = require("/app/modules/utility.js")["utility"];
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];

    var publicMethods = {};
    var privateMethods = {};

    privateMethods.handleGetAllPoliciesResponse = function (backendResponse) {
        var response = {};
        if (backendResponse.status == 200 && backendResponse.responseText) {
            var isUpdated = false;
            var policyListFromRestEndpoint = parse(backendResponse.responseText)["policies"];

            var policyListToView = [];
            var i, policyObjectFromRestEndpoint, policyObjectToView;
            for (i = 0; i < policyListFromRestEndpoint.length; i++) {
                // get list object
                policyObjectFromRestEndpoint = policyListFromRestEndpoint[i];
                // populate list object values to view-object
                policyObjectToView = {};
                policyObjectToView["id"] = policyObjectFromRestEndpoint["id"];
                policyObjectToView["priorityId"] = policyObjectFromRestEndpoint["priorityId"];
                policyObjectToView["name"] = policyObjectFromRestEndpoint["policyName"];
                policyObjectToView["platform"] = policyObjectFromRestEndpoint["profile"]["deviceType"];
                policyObjectFromRestEndpoint["policyType"] = policyListFromRestEndpoint["policyType"];
                policyObjectFromRestEndpoint["correctiveActions"] = policyListFromRestEndpoint["correctiveActions"];
                if (policyObjectToView["platform"] == "ios") {
                    policyObjectToView["deviceTypeIcon"] = "apple";
                } else {
                    policyObjectToView["deviceTypeIcon"] = policyObjectToView["platform"];
                }
                var ownershipType = "None";
                var deviceGroups = policyObjectFromRestEndpoint["deviceGroups"];
                if (deviceGroups) {
                    for (var j = 0; j < deviceGroups.length; j++) {
                        var deviceGroup = deviceGroups[j];
                        if (deviceGroup.name === "COPE") {
                            ownershipType = (ownershipType === "BYOD") ? "BYOD & COPE" : "COPE";
                        } else if (deviceGroup.name === "BYOD") {
                            ownershipType = (ownershipType === "COPE") ? "BYOD & COPE" : "BYOD";
                        }
                    }
                }
                policyObjectToView["ownershipType"] = ownershipType;

                var assignedRoleCount = policyObjectFromRestEndpoint["roles"].length;
                var assignedUserCount = policyObjectFromRestEndpoint["users"].length;

                if (assignedRoleCount == 0) {
                    policyObjectToView["roles"] = "None";
                } else if (assignedRoleCount == 1) {
                    policyObjectToView["roles"] = policyObjectFromRestEndpoint["roles"][0];
                } else if (assignedRoleCount > 1) {
                    policyObjectToView["roles"] = policyObjectFromRestEndpoint["roles"][0] + ", ...";
                }

                if (assignedUserCount == 0) {
                    policyObjectToView["users"] = "None";
                } else if (assignedUserCount == 1) {
                    policyObjectToView["users"] = policyObjectFromRestEndpoint["users"][0];
                } else if (assignedUserCount > 1) {
                    policyObjectToView["users"] = policyObjectFromRestEndpoint["users"][0] + ", ...";
                }

                policyObjectToView["compliance"] = policyObjectFromRestEndpoint["compliance"];

                if (policyObjectFromRestEndpoint["active"] == true &&
                    policyObjectFromRestEndpoint["updated"] == true) {
                    policyObjectToView["status"] = "Active/Updated";
                    isUpdated = true;
                } else if (policyObjectFromRestEndpoint["active"] == true &&
                           policyObjectFromRestEndpoint["updated"] == false) {
                    policyObjectToView["status"] = "Active";
                } else if (policyObjectFromRestEndpoint["active"] == false &&
                           policyObjectFromRestEndpoint["updated"] == true) {
                    policyObjectToView["status"] = "Inactive/Updated";
                    isUpdated = true;
                } else if (policyObjectFromRestEndpoint["active"] == false &&
                           policyObjectFromRestEndpoint["updated"] == false) {
                    policyObjectToView["status"] = "Inactive";
                }
                // push view-objects to list
                policyListToView.push(policyObjectToView);
            }
            // generate response
            response.updated = isUpdated;
            response.status = "success";
            response.content = policyListToView;

            return response;
        } else {
            response.status = "error";
            /* backendResponse.responseText == "Scope validation failed"
             Here the response.context("Scope validation failed") is used other then response.status(401).
             Reason for this is IDP return 401 as the status in 4 different situations such as,
             1. UnAuthorized.
             2. Scope Validation Failed.
             3. Permission Denied.
             4. Access Token Expired.
             5. Access Token Invalid.
             In these cases in order to identify the correct situation we have to compare the unique value from status and
             context which is context.
             */
            if (backendResponse.responseText == "Scope validation failed") {
                response.content = "Permission Denied";
            } else {
                response.content = backendResponse.responseText;
            }
            return response;
        }
    };

    /*
     @Updated
     */
    publicMethods.getAllPolicies = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            userModule.logout(function () {
                response.sendRedirect(devicemgtProps["appContext"] + "login");
            });
        }
        try {
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                      "/policies?offset=0&limit=100";
            return serviceInvokers.XMLHttp.get(url, privateMethods.handleGetAllPoliciesResponse);
        } catch (e) {
            throw e;
        }
    };

    /**
     * Retrieve all policies based on policy type
     */
    publicMethods.getAllPoliciesByType = function (policyType) {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            userModule.logout(function () {
                response.sendRedirect(devicemgtProps["appContext"] + "login");
            });
        }
        try {
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                      "/policies/type/" + policyType + "?offset=0&limit=100";
            return serviceInvokers.XMLHttp.get(url, privateMethods.handleGetAllPoliciesResponse);
        } catch (e) {
            log.error("Error occurred while retrieving policies by policy type " + policyType);
            throw e;
        }
    };

    /*
     Get policies count from backend services.
     */
    publicMethods.getPoliciesCount = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            userModule.logout(function () {
                response.sendRedirect(devicemgtProps["appContext"] + "login");
            });
        }
        try {
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                      "/policies?offset=0&limit=1";
            return serviceInvokers.XMLHttp.get(
                url, function (responsePayload) {
                    return parse(responsePayload["responseText"])["count"];
                },
                function (responsePayload) {
                    log.error(responsePayload["responseText"]);
                    return -1;
                }
            );
        } catch (e) {
            throw e;
        }
    };

    /*
     Get apps available in the store from backend service.
     */
    publicMethods.getStoreAppsForPolicy = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            userModule.logout(function () {
                response.sendRedirect(devicemgtProps["appContext"] + "login");
            });
        }
        try {
            var url = devicemgtProps["managerHTTPSURL"] + devicemgtProps["backendRestEndpoints"]["appMgt"] +
                      "/applications";
            var data = {
              limit: -1
            };
            return serviceInvokers.XMLHttp.post(url, data,
                function (backendResponse) {
                    var response = {};
                    if (backendResponse.status === 200 && backendResponse.responseText) {
                        var appListFromRestEndpoint = parse(backendResponse.responseText)["applications"];
                        var storeApps = [];
                        var i, appObjectFromRestEndpoint, appObjectToView;
                        for (i=0; i<appListFromRestEndpoint.length; i++) {
                            appObjectFromRestEndpoint = appListFromRestEndpoint[i];
                            appObjectToView = {};
                            appObjectToView["appName"] = appObjectFromRestEndpoint["name"];
                            appObjectToView["packageName"] = appObjectFromRestEndpoint["packageName"];
                            appObjectToView["appId"] = appObjectFromRestEndpoint["id"];
                            if ("WEB_CLIP" === appObjectFromRestEndpoint["type"]) {
                                appObjectToView["type"] = "Web Clip"
                            } else {
                                appObjectToView["type"] = "Mobile App"
                            }
                            appObjectToView["uuid"] = appObjectFromRestEndpoint["applicationReleases"][0]["uuid"];
                            appObjectToView["version"] = appObjectFromRestEndpoint["applicationReleases"][0]["version"];
                            appObjectToView["platform"] = appObjectFromRestEndpoint["deviceType"];
                            storeApps.push(appObjectToView);
                        }
                        response.status = "success";
                        response.content = storeApps;
                        return response;
                    } else {
                        response.status = "error";
                        if (backendResponse.responseText === "Scope validation failed") {
                            response.content = "Permission Denied";
                        } else {
                            response.content = backendResponse.responseText;
                        }
                        return response;
                    }
                });
        } catch (e) {
            throw e;
        }
    };

    /*
     @Updated - used by getAllPolicies
     */
    privateMethods.getElementsInAString = function (elementList) {
        var i, elementsInAString = "";
        for (i = 0; i < elementList.length; i++) {
            if (i == elementList.length - 1) {
                elementsInAString += elementList[i];
            } else {
                elementsInAString += elementList[i] + ", ";
            }
        }
        return elementsInAString;
    };

    return publicMethods;
}();