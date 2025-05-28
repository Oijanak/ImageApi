package com.janak.imageapi.utils;

import lombok.Data;

import java.util.List;
@Data
public class PaginatedResponse<T> {

    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean last;
    private boolean first;
}
