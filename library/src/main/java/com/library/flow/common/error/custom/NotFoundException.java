package com.library.flow.common.error.custom;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, Object id) {
        super(entity + " not found with id=" + id);
    }
}
