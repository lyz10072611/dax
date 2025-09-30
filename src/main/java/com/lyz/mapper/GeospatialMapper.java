package com.lyz.mapper;

import com.lyz.pojo.GeospatialFile;
import com.lyz.pojo.CementPlantGeodata;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 地理数据数据访问层
 */
@Mapper
public interface GeospatialMapper {
    
    // 地理数据文件相关操作
    @Select("SELECT * FROM geospatial_files WHERE file_id = #{fileId}")
    GeospatialFile findFileById(@Param("fileId") Long fileId);
    
    @Select("SELECT * FROM geospatial_files WHERE file_path = #{filePath}")
    GeospatialFile findFileByPath(@Param("filePath") String filePath);
    
    @Select({
        "<script>",
        "SELECT * FROM geospatial_files",
        "<where>",
        "<if test='fileType != null and fileType != \"\"'>",
        "AND file_type = #{fileType}",
        "</if>",
        "<if test='status != null and status != \"\"'>",
        "AND status = #{status}",
        "</if>",
        "</where>",
        "ORDER BY upload_time DESC",
        "</script>"
    })
    List<GeospatialFile> listFiles(@Param("fileType") String fileType, @Param("status") String status);
    
    @Insert("INSERT INTO geospatial_files (file_name, file_path, file_type, file_size, coordinate_system, " +
            "bounds_west, bounds_east, bounds_south, bounds_north, resolution_x, resolution_y, bands_count, " +
            "data_type, upload_time, upload_user_id, status) " +
            "VALUES (#{fileName}, #{filePath}, #{fileType}, #{fileSize}, #{coordinateSystem}, " +
            "#{boundsWest}, #{boundsEast}, #{boundsSouth}, #{boundsNorth}, #{resolutionX}, #{resolutionY}, " +
            "#{bandsCount}, #{dataType}, NOW(), #{uploadUserId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "fileId")
    void insertFile(GeospatialFile geospatialFile);
    
    @Update("UPDATE geospatial_files SET file_name = #{fileName}, file_path = #{filePath}, file_type = #{fileType}, " +
            "file_size = #{fileSize}, coordinate_system = #{coordinateSystem}, bounds_west = #{boundsWest}, " +
            "bounds_east = #{boundsEast}, bounds_south = #{boundsSouth}, bounds_north = #{boundsNorth}, " +
            "resolution_x = #{resolutionX}, resolution_y = #{resolutionY}, bands_count = #{bandsCount}, " +
            "data_type = #{dataType}, status = #{status} WHERE file_id = #{fileId}")
    void updateFile(GeospatialFile geospatialFile);
    
    @Delete("DELETE FROM geospatial_files WHERE file_id = #{fileId}")
    void deleteFile(@Param("fileId") Long fileId);
    
    // 水泥厂地理数据关联相关操作
    @Select("SELECT cpg.*, cp.plant_name, gf.file_name, gf.file_path FROM cement_plant_geodata cpg " +
            "LEFT JOIN cement_plants cp ON cpg.plant_id = cp.plant_id " +
            "LEFT JOIN geospatial_files gf ON cpg.file_id = gf.file_id " +
            "WHERE cpg.relation_id = #{relationId}")
    CementPlantGeodata findGeodataById(@Param("relationId") Long relationId);
    
    @Select("SELECT cpg.*, cp.plant_name, gf.file_name, gf.file_path FROM cement_plant_geodata cpg " +
            "LEFT JOIN cement_plants cp ON cpg.plant_id = cp.plant_id " +
            "LEFT JOIN geospatial_files gf ON cpg.file_id = gf.file_id " +
            "WHERE cpg.plant_id = #{plantId} ORDER BY cpg.created_at DESC")
    List<CementPlantGeodata> listGeodataByPlantId(@Param("plantId") Long plantId);
    
    @Select("SELECT cpg.*, cp.plant_name, gf.file_name, gf.file_path FROM cement_plant_geodata cpg " +
            "LEFT JOIN cement_plants cp ON cpg.plant_id = cp.plant_id " +
            "LEFT JOIN geospatial_files gf ON cpg.file_id = gf.file_id " +
            "WHERE cpg.file_id = #{fileId} ORDER BY cpg.created_at DESC")
    List<CementPlantGeodata> listGeodataByFileId(@Param("fileId") Long fileId);
    
    @Insert("INSERT INTO cement_plant_geodata (plant_id, file_id, identification_id, data_type, " +
            "acquisition_date, processing_status, created_at) " +
            "VALUES (#{plantId}, #{fileId}, #{identificationId}, #{dataType}, " +
            "#{acquisitionDate}, #{processingStatus}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "relationId")
    void insertGeodataRelation(CementPlantGeodata geodata);
    
    @Update("UPDATE cement_plant_geodata SET plant_id = #{plantId}, file_id = #{fileId}, " +
            "identification_id = #{identificationId}, data_type = #{dataType}, " +
            "acquisition_date = #{acquisitionDate}, processing_status = #{processingStatus} " +
            "WHERE relation_id = #{relationId}")
    void updateGeodataRelation(CementPlantGeodata geodata);
    
    @Delete("DELETE FROM cement_plant_geodata WHERE relation_id = #{relationId}")
    void deleteGeodataRelation(@Param("relationId") Long relationId);
    
    // 瓦片数据相关操作
    @Select("SELECT tile_data FROM geospatial_tiles WHERE file_id = #{fileId} AND zoom_level = #{zoom} " +
            "AND tile_x = #{x} AND tile_y = #{y}")
    byte[] getTileData(@Param("fileId") Long fileId, @Param("zoom") Integer zoom, 
                      @Param("x") Integer x, @Param("y") Integer y);
    
    @Insert("INSERT INTO geospatial_tiles (file_id, zoom_level, tile_x, tile_y, tile_data, tile_size, format) " +
            "VALUES (#{fileId}, #{zoom}, #{x}, #{y}, #{tileData}, #{tileSize}, #{format}) " +
            "ON CONFLICT (file_id, zoom_level, tile_x, tile_y) DO UPDATE SET " +
            "tile_data = EXCLUDED.tile_data, created_at = NOW()")
    void insertTile(@Param("fileId") Long fileId, @Param("zoom") Integer zoom, 
                   @Param("x") Integer x, @Param("y") Integer y, 
                   @Param("tileData") byte[] tileData, @Param("tileSize") Integer tileSize, 
                   @Param("format") String format);
}
