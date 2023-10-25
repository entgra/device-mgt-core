/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.device.mgt.common.BasicUserInfoMetadata;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UserManagementException;
import io.entgra.device.mgt.core.device.mgt.core.service.UserManagementProviderService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.eclipse.wst.common.uriresolver.internal.util.URIEncoder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.OTPManagementException;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.EmailMetaInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ActivityList;
import io.entgra.device.mgt.core.device.mgt.common.BasicUserInfo;
import io.entgra.device.mgt.core.device.mgt.common.BasicUserInfoList;
import io.entgra.device.mgt.core.device.mgt.common.BasicUserInfoWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.Credential;
import io.entgra.device.mgt.core.device.mgt.common.EnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.OldPasswordResetWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.PermissionList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.RoleList;
import io.entgra.device.mgt.core.device.mgt.common.UserInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.UserStoreList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.exception.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.UserManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.CredentialManagementResponseBuilder;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementAdminService;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.LocalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.user.store.count.UserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserManagementServiceImpl implements UserManagementService {

    private static final String ROLE_EVERYONE = "Internal/everyone";
    private static final String API_BASE_PATH = "/users";
    private static final Log log = LogFactory.getLog(UserManagementServiceImpl.class);

    // Permissions that are given for a normal device user.
    private static final Permission[] PERMISSIONS_FOR_DEVICE_USER = {
            new Permission("/permission/admin/Login", "ui.execute"),
            new Permission("/permission/admin/device-mgt/device/api/subscribe", "ui.execute"),
            new Permission("/permission/admin/device-mgt/devices/enroll", "ui.execute"),
            new Permission("/permission/admin/device-mgt/devices/disenroll", "ui.execute"),
            new Permission("/permission/admin/device-mgt/devices/owning-device/view", "ui.execute"),
            new Permission("/permission/admin/manage/portal", "ui.execute")
    };

    @POST
    @Override
    public Response addUser(UserInfo userInfo) {
        try {
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(userInfo.getUsername())) {
                // if user already exists
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userInfo.getUsername() +
                            " already exists. Therefore, request made to add user was refused.");
                }
                // returning response with bad request state
                String msg = "User by username: " + userInfo.getUsername() + " already exists. Try with another username." ;
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            }
            BasicUserInfoWrapper userInfoWrapper = new BasicUserInfoWrapper();
            userInfoWrapper =  ums.addUser(userInfo);
            return Response.created(new URI(API_BASE_PATH + "/" + URIEncoder.encode(userInfo.getUsername(),
                    "UTF-8"))).entity(userInfoWrapper).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to add user '" + userInfo.getUsername() + "' to the " +
                    "underlying user management system";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the location URI, which represents information of the " +
                    "newly created user '" + userInfo.getUsername() + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Error occurred while encoding username in the URI for the newly created user " +
                    userInfo.getUsername();
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserManagementException e) {
            String msg = "Error occurred while trying to add the user '" +  userInfo.getUsername() + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    public Response getUser(@QueryParam("username") String username, @QueryParam("domain") String domain,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
        }
        try {
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist.");
                }
                String msg = "User by username: " + username + " does not exist.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            BasicUserInfo user = ums.getUser(username);
            return Response.status(Response.Status.OK).entity(user).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving information of the user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserManagementException e) {
            String message = "Error occurred while trying to get the user '" + username + "'";
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                    .build();
        }
    }

    @PUT
    @Override
    public Response updateUser(@QueryParam("username") String username, @QueryParam("domain") String domain, UserInfo userInfo) {
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
        }
        try {
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username +
                            " doesn't exists. Therefore, request made to update user was refused.");
                }
                String msg = "User by username: " + username + " does not exist.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.ok().entity(ums.updateUser(username, userInfo)).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to update user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserManagementException e) {
            String message = "Error occurred while trying to update the user '" + username + "'";
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                    .build();
        }
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Override
    public Response removeUser(@QueryParam("username") String username, @QueryParam("domain") String domain) {
        boolean nameWithDomain = false;
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
            nameWithDomain = true;
        }
        try {
            int deviceCount;
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by user: " + username + " does not exist for removal.");
                }
                String msg = "User by user: " + username + " does not exist for removal.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            DeviceManagementProviderService deviceManagementService = DeviceMgtAPIUtils.getDeviceManagementService();
            if (nameWithDomain) {
                deviceCount = deviceManagementService.getDeviceCount(username.split("/")[1]);
            } else {
                deviceCount = deviceManagementService.getDeviceCount(username);
            }
            if (deviceCount == 0) {
                userStoreManager.deleteUser(username);
                if (log.isDebugEnabled()) {
                    log.debug("User '" + username + "' was successfully removed.");
                }
                return Response.status(Response.Status.OK).build();
            } else {
                String msg = "There are enrolled devices for user: " + username + ". Please remove them before deleting the user.";
                log.error(msg);
                return Response.status(400).entity(msg).build();
            }
        } catch (DeviceManagementException | UserStoreException e) {
            String msg = "Exception in trying to remove user by user: " + username;
            log.error(msg, e);
            return Response.status(400).entity(msg).build();
        }
    }

    @GET
    @Path("/roles")
    @Override
    public Response getRolesOfUser(@QueryParam("username") String username, @QueryParam("domain") String domain) {
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
        }
        try {
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist for role retrieval.");
                }
                String msg = "User by username: " + username + " does not exist for role retrieval.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }

            RoleList result = new RoleList();
            result.setList(ums.getRoles(username));
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserManagementException e) {
            String message = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                    .build();
        }
    }

    @GET
    @Path("/list")
    @Override
    public Response getUsers(@QueryParam("filter") String filter, @HeaderParam("If-Modified-Since") String timestamp,
                             @QueryParam("offset") int offset, @QueryParam("limit") int limit,
                             @QueryParam("domain") String domain) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of users with all user-related information");
        }

        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (limit == 0) {
            limit = Constants.DEFAULT_PAGE_LIMIT;
        }
        List<BasicUserInfo> userList, offsetList;
        String appliedFilter = ((filter == null) || filter.isEmpty() ? "*" : filter + "*");
        // to get whole set of users, appliedLimit is set to -1
        // by default, this whole set is limited to 100 - MaxUserNameListLength of user-mgt.xml
        int appliedLimit = -1;

        try {
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            BasicUserInfoList basicUserInfoList = ums.getUsers(appliedFilter,appliedLimit, domain, limit, offset);
            return Response.status(Response.Status.OK).entity(basicUserInfoList).build();
        }  catch (UserManagementException e) {
            String message = "Error occurred while trying to retrieve all users of domain '" + domain + "'";
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                    .build();
        }
    }

    @GET
    @Path("/search")
    @Override
    public Response getUsers(@QueryParam("username") String username, @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName, @QueryParam("emailAddress") String emailAddress,
            @HeaderParam("If-Modified-Since") String timestamp, @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {

        if (RequestValidationUtil.isNonFilterRequest(username,firstName, lastName, emailAddress)) {
            return getUsers(null, timestamp, offset, limit, null);
        }

        RequestValidationUtil.validatePaginationParameters(offset, limit);

        if(log.isDebugEnabled()) {
            log.debug("Filtering users - filter: {username: " + username  +", firstName: " + firstName + ", lastName: "
                    + lastName + ", emailAddress: " + emailAddress + "}");
        }

        if (limit == 0) {
            limit = Constants.DEFAULT_PAGE_LIMIT;
        }

        BasicUserInfo basicUserInfo = new BasicUserInfo();
        basicUserInfo.setUsername(username);
        basicUserInfo.setFirstname(firstName);
        basicUserInfo.setLastname(lastName);
        basicUserInfo.setEmailAddress(emailAddress);

        try {
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            BasicUserInfoList result = ums.getUsersSearch(basicUserInfo, offset, limit);
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserManagementException e) {
            String msg = "Error occurred while retrieving the list of users.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/count")
    @Override
    public Response getUserCount() {
        try {
            UserStoreCountRetriever userStoreCountRetrieverService = DeviceMgtAPIUtils.getUserStoreCountRetrieverService();
            RealmConfiguration secondaryRealmConfiguration = DeviceMgtAPIUtils.getUserRealm().getRealmConfiguration()
                    .getSecondaryRealmConfig();

            if (secondaryRealmConfiguration != null) {
                if (!secondaryRealmConfiguration.isPrimary() && !Constants.JDBC_USERSTOREMANAGER.
                        equals(secondaryRealmConfiguration.getUserStoreClass().getClass())) {
                    return getUserCountViaUserStoreManager();
                }
            }
            if (userStoreCountRetrieverService != null) {
                long count = userStoreCountRetrieverService.countUsers("");
                if (count != -1) {
                    BasicUserInfoList result = new BasicUserInfoList();
                    result.setCount(count);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
            }
        } catch (UserStoreCounterException e) {
            String msg =
                    "Error occurred while retrieving the count of users that exist within the current tenant";
            log.error(msg, e);
        } catch (UserStoreException e) {
            String msg =
                    "Error occurred while retrieving user stores.";
            log.error(msg, e);
        }
        return getUserCountViaUserStoreManager();
    }

    /**
     * This method returns the count of users using UserStoreManager.
     *
     * @return user count
     */
    private Response getUserCountViaUserStoreManager() {
        if (log.isDebugEnabled()) {
            log.debug("Getting the user count");
        }

        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            int userCount = userStoreManager.listUsers("*", -1).length;
            BasicUserInfoList result = new BasicUserInfoList();
            result.setCount(userCount);
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the user count.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/checkUser")
    @Override
    public Response isUserExists(@QueryParam("username") String userName) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(userName)) {
                return Response.status(Response.Status.OK).entity(true).build();
            } else {
                return Response.status(Response.Status.OK).entity(false).build();
            }
        } catch (UserStoreException e) {
            String msg = "Error while retrieving the user.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/search/usernames")
    @Override
    public Response getUserNames(@QueryParam("filter") String filter, @QueryParam("domain") String domain,
                                 @HeaderParam("If-Modified-Since") String timestamp,
                                 @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of users with all user-related information using the filter : " + filter);
        }
        String userStoreDomain = Constants.PRIMARY_USER_STORE;
        if (domain != null && !domain.isEmpty()) {
            userStoreDomain = domain;
        }
        if (limit == 0){
            //If there is no limit is passed, then return all.
            limit = -1;
        }
        List<UserInfo> userList;
        try {
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            userList = ums.getUserNames(filter, userStoreDomain, offset, limit);
            return Response.status(Response.Status.OK).entity(userList).build();
        } catch (UserManagementException e) {
            String msg = "Error occurred while retrieving the list of users using the filter : " + filter;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/credentials")
    @Override
    public Response resetPassword(OldPasswordResetWrapper credentials) {
        return CredentialManagementResponseBuilder.buildChangePasswordResponse(credentials);
    }


    @POST
    @Path("/send-invitation")
    @Produces({MediaType.APPLICATION_JSON})
    public Response inviteExistingUsersToEnrollDevice(DeviceEnrollmentInvitation deviceEnrollmentInvitation) {
        if (deviceEnrollmentInvitation.getUsernames() == null || deviceEnrollmentInvitation.getUsernames().isEmpty()) {
            String msg = "Error occurred while validating list of user-names. User-names cannot be empty.";
            log.error(msg);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                            .build());
        }
        if (log.isDebugEnabled()) {
            log.debug("Sending device enrollment invitation mail to existing user/s.");
        }
        OTPManagementService oms = DeviceMgtAPIUtils.getOTPManagementService();
        try {
            oms.sendDeviceEnrollmentInvitationMail(deviceEnrollmentInvitation);
        } catch (OTPManagementException e) {
            String msg = "Error occurred while generating OTP and inviting user/s to enroll their device/s.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Invitation mails have been sent.").build();
    }

    @POST
    @Path("/enrollment-invite")
    @Override
    public Response inviteToEnrollDevice(EnrollmentInvitation enrollmentInvitation) {
        if (log.isDebugEnabled()) {
            log.debug("Sending enrollment invitation mail to existing user.");
        }
        DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            Set<String> recipients = new HashSet<>();
            recipients.addAll(enrollmentInvitation.getRecipients());
            Properties props = new Properties();
            String username = DeviceMgtAPIUtils.getAuthenticatedUser();
            String firstName = getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME);
            String lastName = getClaimValue(username, Constants.USER_CLAIM_LAST_NAME);
            if (firstName == null) {
                firstName = username;
            }
            if (lastName == null) {
                lastName = "";
            }
            props.setProperty("first-name", firstName);
            props.setProperty("last-name", lastName);
            props.setProperty("device-type", enrollmentInvitation.getDeviceType());
            EmailMetaInfo metaInfo = new EmailMetaInfo(recipients, props);
            dms.sendEnrolmentInvitation(getEnrollmentTemplateName(enrollmentInvitation.getDeviceType()), metaInfo);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while inviting user to enrol their device";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while getting claim values to invite user";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while sending the email invitations. Mail server not configured.";
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Invitation mails have been sent.").build();
    }

    @POST
    @Path("/validate")
    @Override
    public Response validateUser(Credential credential) {
        try {
            credential.validateRequest();
            RealmService realmService = DeviceMgtAPIUtils.getRealmService();
            String tenant = credential.getTenantDomain();
            int tenantId;
            if (tenant == null || tenant.trim().isEmpty()) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                tenantId = realmService.getTenantManager().getTenantId(tenant);
            }
            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                String msg = "Error occurred while validating the user. Invalid tenant domain " + tenant;
                log.error(msg);
                throw new BadRequestException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                                .build());
            }
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            JsonObject result = new JsonObject();
            if (userRealm.getUserStoreManager().authenticate(credential.getUsername(), credential.getPassword())) {
                result.addProperty("valid", true);
                return Response.status(Response.Status.OK).entity(result).build();
            } else {
                result.addProperty("valid", false);
                return Response.status(Response.Status.OK).entity(result).build();
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving user store to validate user";
            log.error(msg, e);
            return Response.serverError().entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build())
                    .build();
        }
    }

    @GET
    @Override
    @Path("/device/activities")
    public Response getActivities(
            @QueryParam("since") String since,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        long ifModifiedSinceTimestamp;
        long sinceTimestamp;
        long timestamp = 0;
        boolean isIfModifiedSinceSet = false;
        String initiatedBy;
        if (log.isDebugEnabled()) {
            log.debug("getActivities since: " + since + " , offset: " + offset + " ,limit: " + limit + " ,"
                    + "ifModifiedSince: " + ifModifiedSince);
        }
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            Date ifSinceDate;
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_SIMPLE_DATE_FORMAT);
            try {
                ifSinceDate = format.parse(ifModifiedSince);
            } catch (ParseException e) {
                String msg = "Invalid date string is provided in [If-Modified-Since] header";
                return Response.status(400).entity(msg).build();
            }
            ifModifiedSinceTimestamp = ifSinceDate.getTime();
            isIfModifiedSinceSet = true;
            timestamp = ifModifiedSinceTimestamp / 1000;
        } else if (since != null && !since.isEmpty()) {
            Date sinceDate;
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_SIMPLE_DATE_FORMAT);
            try {
                sinceDate = format.parse(since);
            } catch (ParseException e) {
                String msg = "Invalid date string is provided in [since] filter";
                return Response.status(400).entity(msg).build();
            }
            sinceTimestamp = sinceDate.getTime();
            timestamp = sinceTimestamp / 1000;
        }

        if (timestamp == 0) {
            //If timestamp is not sent by the user, a default value is set, that is equal to current time-12 hours.
            long time = System.currentTimeMillis() / 1000;
            timestamp = time - 42300;
        }
        if (log.isDebugEnabled()) {
            log.debug("getActivities final timestamp " + timestamp);
        }

        List<Activity> activities;
        int count;
        ActivityList activityList = new ActivityList();
        DeviceManagementProviderService dmService;

        initiatedBy = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Calling database to get activities.");
            }
            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            activities = dmService.getActivitiesUpdatedAfterByUser(timestamp, initiatedBy, limit, offset);
            if (log.isDebugEnabled()) {
                log.debug("Calling database to get activity count with timestamp and user.");
            }
            count = dmService.getActivityCountUpdatedAfterByUser(timestamp, initiatedBy);
            if (log.isDebugEnabled()) {
                log.debug("Activity count: " + count);
            }

            activityList.setList(activities);
            activityList.setCount(count);
            if ((activities == null || activities.isEmpty()) && isIfModifiedSinceSet) {
                return Response.notModified().build();
            }
            return Response.ok().entity(activityList).build();
        } catch (OperationManagementException e) {
            String msg =
                    "Error Response occurred while fetching the activities updated after given time stamp for the user "
                            + initiatedBy + ".";
            log.error(msg, e);
            return Response.serverError().entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build())
                    .build();
        }
    }

    @PUT
    @Override
    @Path("/claims")
    public Response updateUserClaimsForDevices(
            @QueryParam("username") String username, JsonArray deviceList,
            @QueryParam("domain") String domain) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (domain != null && !domain.isEmpty()) {
                username = domain + Constants.FORWARD_SLASH + username;
            } else {
                RealmConfiguration realmConfiguration = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm()
                        .getRealmConfiguration();
                domain = realmConfiguration
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (!StringUtils.isBlank(domain)) {
                    username = domain + Constants.FORWARD_SLASH + username;
                }
            }
            if (!userStoreManager.isExistingUser(username)) {
                String msg = "User by username: " + username + " does not exist.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            ClaimMetadataManagementAdminService
                    claimMetadataManagementAdminService = new ClaimMetadataManagementAdminService();
            //Get all available claim URIs
            String[] allUserClaims = userStoreManager.getClaimManager().getAllClaimUris();
            //Check they contains a claim attribute for external devices
            if (!Arrays.asList(allUserClaims).contains(Constants.USER_CLAIM_DEVICES)) {
                List<ClaimPropertyDTO> claimPropertyDTOList = new ArrayList<>();
                claimPropertyDTOList
                        .add(DeviceMgtAPIUtils.buildClaimPropertyDTO
                                (Constants.ATTRIBUTE_DISPLAY_NAME, Constants.EXTERNAL_DEVICE_CLAIM_DISPLAY_NAME));
                claimPropertyDTOList
                        .add(DeviceMgtAPIUtils.buildClaimPropertyDTO
                                (Constants.ATTRIBUTE_DESCRIPTION, Constants.EXTERNAL_DEVICE_CLAIM_DESCRIPTION));

                LocalClaimDTO localClaimDTO = new LocalClaimDTO();
                localClaimDTO.setLocalClaimURI(Constants.USER_CLAIM_DEVICES);
                localClaimDTO.setClaimProperties(claimPropertyDTOList.toArray(
                        new ClaimPropertyDTO[claimPropertyDTOList.size()]));

                AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();
                attributeMappingDTO.setAttributeName(Constants.DEVICES);
                attributeMappingDTO.setUserStoreDomain(domain);
                localClaimDTO.setAttributeMappings(new AttributeMappingDTO[]{attributeMappingDTO});

                claimMetadataManagementAdminService.addLocalClaim(localClaimDTO);
            }
            Map<String, String> userClaims =
                    this.buildExternalDevicesUserClaims(username, domain, deviceList, userStoreManager);
            userStoreManager.setUserClaimValues(username, userClaims, domain);
            return Response.status(Response.Status.OK).entity(userClaims).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while updating external device claims of the user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ClaimMetadataException e) {
            String msg = "Error occurred while adding claim attribute";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/claims")
    public Response getUserClaimsForDevices(
            @QueryParam("username") String username, @QueryParam("domain") String domain) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            Map<String, String> claims = new HashMap<>();
            if (domain != null && !domain.isEmpty()) {
                username = domain + Constants.FORWARD_SLASH + username;
            } else {
                RealmConfiguration realmConfiguration = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm()
                        .getRealmConfiguration();
                domain = realmConfiguration
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (!StringUtils.isBlank(domain)) {
                    username = domain + Constants.FORWARD_SLASH + username;
                }
            }
            if (!userStoreManager.isExistingUser(username)) {
                String msg = "User by username: " + username + " does not exist.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            String[] allUserClaims = userStoreManager.getClaimManager().getAllClaimUris();
            if (!Arrays.asList(allUserClaims).contains(Constants.USER_CLAIM_DEVICES)) {
                if (log.isDebugEnabled()) {
                    log.debug("Claim attribute for external device doesn't exist.");
                }
                return Response.status(Response.Status.OK).entity(claims).build();
            }
            String[] claimArray = {Constants.USER_CLAIM_DEVICES};
            claims = userStoreManager.getUserClaimValues(username, claimArray, domain);
            return Response.status(Response.Status.OK).entity(claims).build();
        } catch (UserStoreException e) {
            String msg = "Error  occurred while retrieving external device claims of the user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Path("/claims")
    public Response deleteUserClaimsForDevices(
            @QueryParam("username") String username, @QueryParam("domain") String domain) {
        try {
            String[] claimArray = new String[1];
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (domain != null && !domain.isEmpty()) {
                username = domain + Constants.FORWARD_SLASH + username;
            } else {
                RealmConfiguration realmConfiguration = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm()
                        .getRealmConfiguration();
                domain = realmConfiguration
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (!StringUtils.isBlank(domain)) {
                    username = domain + Constants.FORWARD_SLASH + username;
                }
            }
            if (!userStoreManager.isExistingUser(username)) {
                String msg = "User by username: " + username + " does not exist.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            String[] allUserClaims = userStoreManager.getClaimManager().getAllClaimUris();
            if (!Arrays.asList(allUserClaims).contains(Constants.USER_CLAIM_DEVICES)) {
                if (log.isDebugEnabled()) {
                    log.debug("Claim attribute for external device doesn't exist.");
                }
                return Response.status(Response.Status.OK).entity(claimArray).build();
            }
            claimArray[0] = Constants.USER_CLAIM_DEVICES;
            userStoreManager.deleteUserClaimValues(
                    username,
                    claimArray,
                    domain);
            return Response.status(Response.Status.OK).entity(claimArray).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while deleting external device claims of the user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/current-user/permissions")
    public Response getPermissionsOfUser() {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            UserManagementProviderService ums = DeviceMgtAPIUtils.getUserManagementService();
            if (!userStoreManager.isExistingUser(username)) {
                String message = "User by username: " + username + " does not exist for permission retrieval.";
                log.error(message);
                return Response.status(Response.Status.NOT_FOUND).entity(message).build();
            }
            List<String> permissions = ums.getPermissions(username);
            PermissionList permissionList = new PermissionList();
            permissionList.setList(permissions);
            return Response.status(Response.Status.OK).entity(permissionList).build();
        } catch (UserStoreException e) {
            String message = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                    .build();
        } catch (UserManagementException e) {
            String message = "Error occurred while trying to retrieve permissions of the user '" + username + "'";
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                    .build();
        }
    }

    /**
     * This method is used to build String map for user claims with updated external device details
     *
     * @param username username of the particular user
     * @param domain domain of the particular user
     * @param deviceList Array of external device details
     * @param userStoreManager {@link UserStoreManager} instance
     * @return String map
     * @throws UserStoreException If any error occurs while calling into UserStoreManager service
     */
    private Map<String, String> buildExternalDevicesUserClaims(
            String username,
            String domain,
            JsonArray deviceList,
            UserStoreManager userStoreManager) throws UserStoreException {
        Map<String, String> userClaims;
        String[] claimArray = {
                Constants.USER_CLAIM_FIRST_NAME,
                Constants.USER_CLAIM_LAST_NAME,
                Constants.USER_CLAIM_EMAIL_ADDRESS,
                Constants.USER_CLAIM_MODIFIED
        };
        userClaims = userStoreManager.getUserClaimValues(username, claimArray, domain);
        if (userClaims.containsKey(Constants.USER_CLAIM_DEVICES)) {
            userClaims.replace(Constants.USER_CLAIM_DEVICES, deviceList.toString());
        } else {
            userClaims.put(Constants.USER_CLAIM_DEVICES, deviceList.toString());
        }
        if (log.isDebugEnabled()) {
            log.debug("Claim map is created for user: " + username + ", claims:" + userClaims.toString());
        }
        return userClaims;
    }

    private String getClaimValue(String username, String claimUri) throws UserStoreException {
        UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
        return userStoreManager.getUserClaimValue(username, claimUri, null);
    }

    private String getEnrollmentTemplateName(String deviceType) {
        String templateName = deviceType + "-enrollment-invitation";
        File template = new File(CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "resources" + File.separator + "email-templates" + File.separator + templateName
                + ".vm");
        if (template.exists()) {
            return templateName;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("The template that is expected to use is not available. Therefore, using default template.");
            }
        }
        return DeviceManagementConstants.EmailAttributes.DEFAULT_ENROLLMENT_TEMPLATE;
    }


    /**
     * Returns a Response with the list of user stores available for a tenant
     * @return list of user stores
     * @throws UserStoreException If unable to search for user stores
     */
    @GET
    @Path("/user-stores")
    @Override
    public Response getUserStores() {
        String domain;
        List<String> userStores = new ArrayList<>();
        UserStoreList userStoreList = new UserStoreList();
        try {
            RealmConfiguration realmConfiguration = DeviceMgtAPIUtils.getUserRealm().getRealmConfiguration();
            userStores.add(realmConfiguration
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));

            while (realmConfiguration != null) {
                realmConfiguration = realmConfiguration.getSecondaryRealmConfig();
                if (realmConfiguration != null) {
                    domain = realmConfiguration
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    userStores.add(domain);
                } else {
                    break;
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving user stores.";
            log.error(msg, e);
        }
        userStoreList.setList(userStores);
        userStoreList.setCount(userStores.size());
        return Response.status(Response.Status.OK).entity(userStoreList).build();
    }
}
