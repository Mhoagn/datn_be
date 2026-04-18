package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SliceResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private boolean hasNext;  // còn dữ liệu tiếp không
}
