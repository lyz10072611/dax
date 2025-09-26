package com.lyz.mapper;

import com.lyz.pojo.PollutionData;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Mapper接口，用于操作污染数据表（pollution_data）。
 */
@Mapper
public interface PollutionDataMapper {

    /**
     * 根据条件查询污染数据列表。
     * @param dataFormat 数据格式
     * @param pollutantType 污染物类型
     * @param year 生产年份
     * @param month 生产月份
     * @param day 生产日期
     * @param hour 生产小时
     * @return 符合条件的污染数据列表
     */
    @Select({
            "<script>",
            "select id, pollutant_type as pollutantType, data_format as dataFormat,",
            "       produce_time as produceTime, upload_time as uploadTime,",
            "       avg_concentration as avgConcentration, max_concentration as maxConcentration,",
            "       warning_location as warningLocation, file_path as filePath",
            "  from pollution_data",
            " <where>",
            "  <if test=\"dataFormat != null\"> and data_format = #{dataFormat} </if>",
            "  <if test=\"pollutantType != null and pollutantType != ''\"> and pollutant_type = #{pollutantType} </if>",
            "  <if test=\"year != null\"> and YEAR(produce_time) = #{year} </if>",
            "  <if test=\"month != null\"> and MONTH(produce_time) = #{month} </if>",
            "  <if test=\"day != null\"> and DAY(produce_time) = #{day} </if>",
            "  <if test=\"hour != null\"> and HOUR(produce_time) = #{hour} </if>",
            " </where>",
            " order by produce_time desc",
            "</script>"
    })
    List<PollutionData> list(@Param("dataFormat") Integer dataFormat,
                              @Param("pollutantType") String pollutantType,
                              @Param("year") Integer year,
                              @Param("month") Integer month,
                              @Param("day") Integer day,
                              @Param("hour") Integer hour);

    /**
     * 根据ID查询污染数据。
     * @param id 数据ID
     * @return 对应的污染数据
     */
    @Select("select id, pollutant_type as pollutantType, data_format as dataFormat, produce_time as produceTime, upload_time as uploadTime, avg_concentration as avgConcentration, max_concentration as maxConcentration, warning_location as warningLocation, file_path as filePath from pollution_data where id=#{id}")
    PollutionData findById(Long id);

    /**
     * 添加新的污染数据。
     * @param data 污染数据对象
     */
    @Insert("insert into pollution_data(pollutant_type, data_format, produce_time, upload_time, avg_concentration, max_concentration, warning_location, file_path) values(#{pollutantType}, #{dataFormat}, #{produceTime}, now(), #{avgConcentration}, #{maxConcentration}, #{warningLocation}, #{filePath})")
    void add(PollutionData data);

    /**
     * 更新污染数据。
     * @param data 污染数据对象
     */
    @Update("update pollution_data set pollutant_type=#{pollutantType}, data_format=#{dataFormat}, produce_time=#{produceTime}, avg_concentration=#{avgConcentration}, max_concentration=#{maxConcentration}, warning_location=#{warningLocation}, file_path=#{filePath}, upload_time=now() where id=#{id}")
    void update(PollutionData data);

    /**
     * 根据ID删除污染数据。
     * @param id 数据ID
     */
    @Delete("delete from pollution_data where id=#{id}")
    void delete(Long id);

    /**
     * 根据多个ID查询污染数据。
     * @param ids 数据ID集合
     * @return 对应的污染数据列表
     */
    @Select({
            "<script>",
            "select id, pollutant_type as pollutantType, data_format as dataFormat, produce_time as produceTime, upload_time as uploadTime, ",
            "avg_concentration as avgConcentration, max_concentration as maxConcentration, warning_location as warningLocation, file_path as filePath ",
            "from pollution_data where id in",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach>",
            "</script>"
    })
    List<PollutionData> findByIds(@Param("ids") Collection<Long> ids);

    /**
     * 查询某一天的24小时图片路径。
     * @param date 日期，格式为yyyy-MM-dd
     * @return 对应日期的24小时图片路径列表
     */
    @Select("select file_path from pollution_data where produce_time >= #{date} and produce_time < date_add(#{date}, interval 1 day)")
    List<String> getImagesForDay(@Param("date") String date);

    /**
     * 流式传输所有污染数据，适用于大数据量的场景。
     * @param consumer 数据消费函数
     */
    void streamAllData(@Param("consumer") Consumer<PollutionData> consumer);
}


