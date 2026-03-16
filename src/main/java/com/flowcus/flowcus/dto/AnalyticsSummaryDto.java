package com.flowcus.flowcus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * A DTO to transport all chart and summary analytics data
 * from the service layer to the controller layer .
 */
@Data
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private boolean hasData;
    private int focusSessionCount;
    private List<ChartItemDto> chartItems;
    private List<String> chartLabels;
    private List<Integer> chartData;
    private List<String> chartColors;
}