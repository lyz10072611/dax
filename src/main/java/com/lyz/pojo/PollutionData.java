package com.lyz.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PollutionData {
    private Long id;
    private String pollutantType; // 'NO2','PM25','PM10','O3'
    private Integer dataFormat; // 1..5
    private LocalDateTime produceTime;
    private LocalDateTime uploadTime;
    private BigDecimal avgConcentration; // DECIMAL(12,7)
    private BigDecimal maxConcentration; // DECIMAL(12,7)
    private String warningLocation; // 经纬度信息
    private String filePath; // 文件路径
}


