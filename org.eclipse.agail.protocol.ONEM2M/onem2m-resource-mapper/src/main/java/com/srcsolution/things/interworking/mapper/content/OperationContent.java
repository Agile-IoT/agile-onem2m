package com.srcsolution.things.interworking.mapper.content;

import java.util.UUID;

import com.srcsolution.things.interworking.data.OperationData;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;

public class OperationContent {

    private final OperationData operation;

    private final ApplicationEntity applicationEntity;

    private final UUID token;

    public OperationContent(OperationData operation) {
        this(null, operation, null);
    }

    public OperationContent(ApplicationEntity applicationEntity, OperationData operation, UUID token) {
        this.applicationEntity = applicationEntity;
        this.operation = operation;
        this.token = token;
    }

    public OperationData getOperation() {
        return operation;
    }

    public ApplicationEntity getApplicationEntity() {
        return applicationEntity;
    }

    public String getUrl() {
        if (applicationEntity != null) {
            return getApplicationEntity().getId()
                    + "?op=" + operation.getName()
                    + "&token=" + token
                    + nullToEmpty(operation.getUrl());
        } else {
            return nullToEmpty(operation.getUrl());
        }
    }

    public static String nullToEmpty(String string) {
        return string == null ? "" : string;
    }

}
