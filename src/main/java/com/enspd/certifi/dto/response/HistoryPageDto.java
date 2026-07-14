package com.enspd.certifi.dto.response;

import java.util.List;

public record HistoryPageDto(
    List<HistoryItemDto> items,
    int page,
    int pageSize,
    long totalItems
) {
}
