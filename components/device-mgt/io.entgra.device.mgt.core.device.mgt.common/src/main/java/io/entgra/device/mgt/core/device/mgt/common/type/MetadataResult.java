package io.entgra.device.mgt.core.device.mgt.common.type;

import java.util.List;

public class MetadataResult<T> {
    private boolean exists;
    private List<T> definitions;

    public MetadataResult(boolean exists, List<T> definitions) {
        this.exists = exists;
        this.definitions = definitions;
    }

    public boolean isExists() {
        return exists;
    }

    public List<T> getDefinitions() {
        return definitions;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public void setDefinitions(List<T> definitions) {
        this.definitions = definitions;
    }
}

