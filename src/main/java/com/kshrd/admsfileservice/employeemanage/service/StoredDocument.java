package com.kshrd.admsfileservice.employeemanage.service;

import org.springframework.core.io.Resource;

public record StoredDocument(Resource resource, String originalFileName, String contentType, long fileSize) {
}
