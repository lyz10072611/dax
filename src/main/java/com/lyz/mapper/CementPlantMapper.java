package com.lyz.mapper;

import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import com.lyz.pojo.PageBean;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 水泥厂数据访问层
 */
@Mapper
public interface CementPlantMapper {
    
    // 水泥厂相关操作
    @Select("SELECT * FROM cement_plants WHERE plant_id = #{plantId}")
    CementPlant findById(@Param("plantId") Long plantId);
    
    @Select("SELECT * FROM cement_plants WHERE plant_name = #{plantName}")
    CementPlant findByName(@Param("plantName") String plantName);
    
    @Select("SELECT * FROM cement_plants ORDER BY plant_id DESC")
    List<CementPlant> listPlants();
    
    @Insert("INSERT INTO cement_plants (plant_name, longitude, latitude, province, city, district, status, create_time, update_time) " +
            "VALUES (#{plantName}, #{longitude}, #{latitude}, #{province}, #{city}, #{district}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "plantId")
    void insert(CementPlant cementPlant);
    
    @Update("UPDATE cement_plants SET plant_name = #{plantName}, longitude = #{longitude}, latitude = #{latitude}, " +
            "province = #{province}, city = #{city}, district = #{district}, status = #{status}, update_time = NOW() " +
            "WHERE plant_id = #{plantId}")
    void update(CementPlant cementPlant);
    
    @Delete("DELETE FROM cement_plants WHERE plant_id = #{plantId}")
    void deleteById(@Param("plantId") Long plantId);
    
    // 根据地理位置范围查询水泥厂
    @Select("SELECT * FROM cement_plants WHERE " +
            "longitude BETWEEN #{minLng} AND #{maxLng} AND " +
            "latitude BETWEEN #{minLat} AND #{maxLat}")
    List<CementPlant> findByLocationRange(@Param("minLng") Double minLng, @Param("maxLng") Double maxLng,
                                         @Param("minLat") Double minLat, @Param("maxLat") Double maxLat);
    
    // 水泥厂识别记录相关操作
    @Select("SELECT cpi.*, cp.plant_name FROM cement_plant_identifications cpi " +
            "LEFT JOIN cement_plants cp ON cpi.plant_id = cp.plant_id " +
            "WHERE cpi.identification_id = #{identificationId}")
    CementPlantIdentification findIdentificationById(@Param("identificationId") Long identificationId);
    
    @Select("SELECT cpi.*, cp.plant_name FROM cement_plant_identifications cpi " +
            "LEFT JOIN cement_plants cp ON cpi.plant_id = cp.plant_id " +
            "ORDER BY cpi.identification_time DESC")
    List<CementPlantIdentification> listIdentifications();
    
    @Insert("INSERT INTO cement_plant_identifications (plant_id, longitude, latitude, identification_time, " +
            "data_source, image_uuid, ndvi_index, province, city, district, created_at) " +
            "VALUES (#{plantId}, #{longitude}, #{latitude}, #{identificationTime}, #{dataSource}, " +
            "#{imageUuid}, #{ndviIndex}, #{province}, #{city}, #{district}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "identificationId")
    void insertIdentification(CementPlantIdentification identification);
    
    @Update("UPDATE cement_plant_identifications SET longitude = #{longitude}, latitude = #{latitude}, " +
            "identification_time = #{identificationTime}, data_source = #{dataSource}, image_uuid = #{imageUuid}, " +
            "ndvi_index = #{ndviIndex}, province = #{province}, city = #{city}, district = #{district} " +
            "WHERE identification_id = #{identificationId}")
    void updateIdentification(CementPlantIdentification identification);
    
    @Delete("DELETE FROM cement_plant_identifications WHERE identification_id = #{identificationId}")
    void deleteIdentificationById(@Param("identificationId") Long identificationId);
    
    // 统计相关查询
    @Select("SELECT COUNT(*) FROM cement_plants WHERE status = 'active'")
    Integer countActivePlants();
    
    @Select("SELECT COUNT(*) FROM cement_plant_identifications WHERE plant_id = #{plantId}")
    Integer countIdentificationsByPlantId(@Param("plantId") Long plantId);
    
    @Select("SELECT COUNT(*) FROM cement_plant_identifications WHERE " +
            "identification_time >= #{startTime} AND identification_time <= #{endTime}")
    Integer countIdentificationsByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
