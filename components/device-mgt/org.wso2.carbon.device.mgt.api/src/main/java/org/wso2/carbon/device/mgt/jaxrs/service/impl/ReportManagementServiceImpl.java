package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementException;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.ReportManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportManagementServiceImpl implements ReportManagementService {

    private static final Log log = LogFactory.getLog(NotificationManagementServiceImpl.class);

    @GET
    @Path("/devices")
    @Override
    public Response getDevicesByDuration(
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {

        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        PaginationResult result;

        DeviceList devices = new DeviceList();

        String msg;
        try {
            result = DeviceMgtAPIUtils.getReportManagementService().getDevicesByDuration(request, fromDate, toDate);
            devices.setList((List<Device>) result.getData());
            devices.setCount(result.getRecordsTotal());
            if (result == null || result.getData() == null || result.getData().size() <= 0) {
                msg = "No devices";
                return Response.status(Response.Status.OK).entity(msg).build();
            } else {
                return Response.status(Response.Status.OK).entity(devices).build();
            }
        } catch (ReportManagementException e) {
            msg = "Error occurred while retrieving device list";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}