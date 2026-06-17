package org.example.expert.domain.todo.dto.request;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record TodoSearchRequest(
        Integer page,
        Integer size,
        String keyword,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        String managerNickname
) {

    public TodoSearchRequest {
        page = page == null ? 1 : page;
        size = size == null ? 10 : size;
    }

    public void validate() {
        if (page < 1) {
            throw new InvalidRequestException("Page must be greater than or equal to 1.");
        }

        if (size < 1) {
            throw new InvalidRequestException("Size must be greater than or equal to 1.");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidRequestException("Start date cannot be after end date.");
        }
    }
}
