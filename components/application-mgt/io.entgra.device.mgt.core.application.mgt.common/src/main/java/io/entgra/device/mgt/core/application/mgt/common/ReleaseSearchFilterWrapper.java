package io.entgra.device.mgt.core.application.mgt.common;

import java.util.List;

public class ReleaseSearchFilterWrapper {
    private ReleaseSearchFilter releaseSearchFilter;
    private List<AppReleaseType> applicableReleaseTypes;
    private String endState;

    public ReleaseSearchFilter getReleaseSearchFilter() {
        return releaseSearchFilter;
    }

    public void setReleaseSearchFilter(ReleaseSearchFilter releaseSearchFilter) {
        this.releaseSearchFilter = releaseSearchFilter;
    }

    public List<AppReleaseType> getApplicableReleaseTypes() {
        return applicableReleaseTypes;
    }

    public void setApplicableReleaseTypes(List<AppReleaseType> applicableReleaseTypes) {
        this.applicableReleaseTypes = applicableReleaseTypes;
    }

    public String getEndState() {
        return endState;
    }

    public void setEndState(String endState) {
        this.endState = endState;
    }
}
