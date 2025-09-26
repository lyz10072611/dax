package com.lyz.service;

import com.lyz.pojo.PageBean;
import com.lyz.pojo.PollutionData;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface PollutionDataService {
    PageBean<PollutionData> page(Integer pageNum, Integer pageSize, Integer dataFormat, String pollutantType,
                                 Integer year, Integer month, Integer day, Integer hour);

    PollutionData findById(Long id);

    void add(PollutionData data);

    void update(PollutionData data);

    void delete(Long id);

    java.util.List<PollutionData> findByIds(java.util.List<Long> ids);

    ResponseEntity<String> download(List<Long> ids);

    void streamAllData(Consumer<PollutionData> consumer);

    CompletableFuture<Void> asyncUploadData(PollutionData data);
}


