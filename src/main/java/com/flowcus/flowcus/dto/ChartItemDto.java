package com.flowcus.flowcus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A DTO to transport all data
 * from the service layer to the controller layer .
 */
@Data
@AllArgsConstructor
public class ChartItemDto {
    private String title;
    private int totalMinutes;
    private String formattedTime;
    private String colorHex;
}
