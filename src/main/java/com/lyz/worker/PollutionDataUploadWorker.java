package com.lyz.worker;

import com.lyz.mapper.PollutionDataMapper;
import com.lyz.pojo.PollutionData;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static com.lyz.config.RabbitConfig.UPLOAD_QUEUE;

@Component
public class PollutionDataUploadWorker {

    @Autowired
    private PollutionDataMapper mapper;

    @RabbitListener(queues = UPLOAD_QUEUE)
    public void handleUploadTask(Map<String, Object> data) {
        PollutionData pollutionData = new PollutionData();
        pollutionData.setPollutantType((String) data.get("pollutantType"));
        pollutionData.setDataFormat((Integer) data.get("dataFormat"));
        pollutionData.setProduceTime(LocalDateTime.parse((String) data.get("produceTime")));
        pollutionData.setUploadTime(LocalDateTime.now());
        pollutionData.setAvgConcentration(new BigDecimal((String) data.get("avgConcentration")));
        pollutionData.setMaxConcentration(new BigDecimal((String) data.get("maxConcentration")));
        pollutionData.setWarningLocation((String) data.get("warningLocation"));
        pollutionData.setFilePath((String) data.get("filePath"));

        mapper.add(pollutionData);
    }
}