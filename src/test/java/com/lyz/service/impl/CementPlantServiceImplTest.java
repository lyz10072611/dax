package com.lyz.service.impl;

import com.lyz.mapper.CementPlantMapper;
import com.lyz.mapper.GeospatialMapper;
import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import com.lyz.pojo.CementPlantDataDTO;
import com.lyz.pojo.CementPlantGeodata;
import com.lyz.pojo.GeospatialFile;
import com.lyz.pojo.PageBean;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 水泥厂服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
class CementPlantServiceImplTest {

    @Mock
    private CementPlantMapper cementPlantMapper;

    @Mock
    private GeospatialMapper geospatialMapper;

    @InjectMocks
    private CementPlantServiceImpl cementPlantService;

    private CementPlant testPlant;
    private CementPlantIdentification testIdentification;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testPlant = new CementPlant();
        testPlant.setPlantId(1L);
        testPlant.setPlantName("测试水泥厂");
        testPlant.setLongitude(118.0);
        testPlant.setLatitude(32.0);
        testPlant.setProvince("江苏省");
        testPlant.setCity("南京市");
        testPlant.setStatus("active");

        testIdentification = new CementPlantIdentification();
        testIdentification.setIdentificationId(1L);
        testIdentification.setPlantId(1L);
        testIdentification.setLongitude(118.0);
        testIdentification.setLatitude(32.0);
        testIdentification.setIdentificationTime(LocalDateTime.now());
        testIdentification.setDataSource("GF-2");
    }

    @Test
    void testFindById_Success() {
        // 准备测试数据
        Long plantId = 1L;
        when(cementPlantMapper.findById(plantId)).thenReturn(testPlant);

        // 执行测试
        CementPlant result = cementPlantService.findById(plantId);

        // 验证结果
        assertNotNull(result);
        assertEquals(plantId, result.getPlantId());
        assertEquals("测试水泥厂", result.getPlantName());
        verify(cementPlantMapper).findById(plantId);
    }

    @Test
    void testFindById_NotFound() {
        // 准备测试数据
        Long plantId = 999L;
        when(cementPlantMapper.findById(plantId)).thenReturn(null);

        // 执行测试
        CementPlant result = cementPlantService.findById(plantId);

        // 验证结果
        assertNull(result);
        verify(cementPlantMapper).findById(plantId);
    }

    @Test
    void testFindByName_Success() {
        // 准备测试数据
        String plantName = "测试水泥厂";
        when(cementPlantMapper.findByName(plantName)).thenReturn(testPlant);

        // 执行测试
        CementPlant result = cementPlantService.findByName(plantName);

        // 验证结果
        assertNotNull(result);
        assertEquals(plantName, result.getPlantName());
        verify(cementPlantMapper).findByName(plantName);
    }

    @Test
    void testListPlants_Success() {
        // 准备测试数据
        List<CementPlant> plants = Arrays.asList(testPlant);
        when(cementPlantMapper.listPlants()).thenReturn(plants);

        // 执行测试
        List<CementPlant> result = cementPlantService.listPlants();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试水泥厂", result.get(0).getPlantName());
        verify(cementPlantMapper).listPlants();
    }

    @Test
    void testPagePlants_Success() {
        // 准备测试数据
        List<CementPlant> plants = Arrays.asList(testPlant);
        PageInfo<CementPlant> pageInfo = new PageInfo<>(plants);
        pageInfo.setTotal(1);

        try (MockedStatic<PageHelper> pageHelperMock = mockStatic(PageHelper.class)) {
            when(cementPlantMapper.listPlants()).thenReturn(plants);

            // 执行测试
            PageBean<CementPlant> result = cementPlantService.pagePlants(1, 10, null, null, null, null);

            // 验证结果
            assertNotNull(result);
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getList().size());
            verify(cementPlantMapper).listPlants();
        }
    }

    @Test
    void testAddPlant_Success() {
        // 准备测试数据
        CementPlant newPlant = new CementPlant();
        newPlant.setPlantName("新水泥厂");
        newPlant.setProvince("江苏省");
        newPlant.setCity("南京市");
        // status为null，应该设置默认值

        doNothing().when(cementPlantMapper).insert(any(CementPlant.class));

        // 执行测试
        cementPlantService.addPlant(newPlant);

        // 验证结果
        assertEquals("active", newPlant.getStatus());
        verify(cementPlantMapper).insert(newPlant);
    }

    @Test
    void testAddPlant_WithStatus() {
        // 准备测试数据
        CementPlant newPlant = new CementPlant();
        newPlant.setPlantName("新水泥厂");
        newPlant.setProvince("江苏省");
        newPlant.setCity("南京市");
        newPlant.setStatus("inactive");

        doNothing().when(cementPlantMapper).insert(any(CementPlant.class));

        // 执行测试
        cementPlantService.addPlant(newPlant);

        // 验证结果
        assertEquals("inactive", newPlant.getStatus());
        verify(cementPlantMapper).insert(newPlant);
    }

    @Test
    void testUpdatePlant_Success() {
        // 准备测试数据
        doNothing().when(cementPlantMapper).update(any(CementPlant.class));

        // 执行测试
        cementPlantService.updatePlant(testPlant);

        // 验证结果
        verify(cementPlantMapper).update(testPlant);
    }

    @Test
    void testDeletePlant_Success() {
        // 准备测试数据
        Long plantId = 1L;
        doNothing().when(cementPlantMapper).deleteById(plantId);

        // 执行测试
        cementPlantService.deletePlant(plantId);

        // 验证结果
        verify(cementPlantMapper).deleteById(plantId);
    }

    @Test
    void testFindPlantsByLocationRange_Success() {
        // 准备测试数据
        List<CementPlant> plants = Arrays.asList(testPlant);
        when(cementPlantMapper.findByLocationRange(117.0, 119.0, 31.0, 33.0)).thenReturn(plants);

        // 执行测试
        List<CementPlant> result = cementPlantService.findPlantsByLocationRange(117.0, 119.0, 31.0, 33.0);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cementPlantMapper).findByLocationRange(117.0, 119.0, 31.0, 33.0);
    }

    @Test
    void testFindIdentificationById_Success() {
        // 准备测试数据
        Long identificationId = 1L;
        when(cementPlantMapper.findIdentificationById(identificationId)).thenReturn(testIdentification);

        // 执行测试
        CementPlantIdentification result = cementPlantService.findIdentificationById(identificationId);

        // 验证结果
        assertNotNull(result);
        assertEquals(identificationId, result.getIdentificationId());
        verify(cementPlantMapper).findIdentificationById(identificationId);
    }

    @Test
    void testListIdentifications_Success() {
        // 准备测试数据
        List<CementPlantIdentification> identifications = Arrays.asList(testIdentification);
        when(cementPlantMapper.listIdentifications()).thenReturn(identifications);

        // 执行测试
        List<CementPlantIdentification> result = cementPlantService.listIdentifications();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cementPlantMapper).listIdentifications();
    }

    @Test
    void testAddIdentification_Success() {
        // 准备测试数据
        CementPlantIdentification newIdentification = new CementPlantIdentification();
        newIdentification.setPlantId(1L);
        newIdentification.setLongitude(118.0);
        newIdentification.setLatitude(32.0);
        // dataSource为null，应该设置默认值

        doNothing().when(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));

        // 执行测试
        cementPlantService.addIdentification(newIdentification);

        // 验证结果
        assertEquals("GF-2", newIdentification.getDataSource());
        verify(cementPlantMapper).insertIdentification(newIdentification);
    }

    @Test
    void testAddIdentification_WithDataSource() {
        // 准备测试数据
        CementPlantIdentification newIdentification = new CementPlantIdentification();
        newIdentification.setPlantId(1L);
        newIdentification.setLongitude(118.0);
        newIdentification.setLatitude(32.0);
        newIdentification.setDataSource("Landsat-8");

        doNothing().when(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));

        // 执行测试
        cementPlantService.addIdentification(newIdentification);

        // 验证结果
        assertEquals("Landsat-8", newIdentification.getDataSource());
        verify(cementPlantMapper).insertIdentification(newIdentification);
    }

    @Test
    void testUpdateIdentification_Success() {
        // 准备测试数据
        doNothing().when(cementPlantMapper).updateIdentification(any(CementPlantIdentification.class));

        // 执行测试
        cementPlantService.updateIdentification(testIdentification);

        // 验证结果
        verify(cementPlantMapper).updateIdentification(testIdentification);
    }

    @Test
    void testDeleteIdentification_Success() {
        // 准备测试数据
        Long identificationId = 1L;
        doNothing().when(cementPlantMapper).deleteIdentificationById(identificationId);

        // 执行测试
        cementPlantService.deleteIdentification(identificationId);

        // 验证结果
        verify(cementPlantMapper).deleteIdentificationById(identificationId);
    }

    @Test
    void testCountActivePlants_Success() {
        // 准备测试数据
        when(cementPlantMapper.countActivePlants()).thenReturn(5);

        // 执行测试
        Integer result = cementPlantService.countActivePlants();

        // 验证结果
        assertEquals(5, result);
        verify(cementPlantMapper).countActivePlants();
    }

    @Test
    void testCountIdentificationsByPlantId_Success() {
        // 准备测试数据
        Long plantId = 1L;
        when(cementPlantMapper.countIdentificationsByPlantId(plantId)).thenReturn(3);

        // 执行测试
        Integer result = cementPlantService.countIdentificationsByPlantId(plantId);

        // 验证结果
        assertEquals(3, result);
        verify(cementPlantMapper).countIdentificationsByPlantId(plantId);
    }

    @Test
    void testCountIdentificationsByTimeRange_Success() {
        // 准备测试数据
        String startTime = "2023-01-01";
        String endTime = "2023-12-31";
        when(cementPlantMapper.countIdentificationsByTimeRange(startTime, endTime)).thenReturn(10);

        // 执行测试
        Integer result = cementPlantService.countIdentificationsByTimeRange(startTime, endTime);

        // 验证结果
        assertEquals(10, result);
        verify(cementPlantMapper).countIdentificationsByTimeRange(startTime, endTime);
    }

    @Test
    void testAddCementPlantData_NewPlant() {
        // 准备测试数据
        CementPlantDataDTO dataDTO = new CementPlantDataDTO();
        dataDTO.setPlantName("新水泥厂");
        dataDTO.setLongitude(118.0);
        dataDTO.setLatitude(32.0);
        dataDTO.setProvince("江苏省");
        dataDTO.setCity("南京市");
        dataDTO.setStatus("active");
        dataDTO.setIdentificationTime(LocalDateTime.now());
        dataDTO.setDataSource("GF-2");

        when(cementPlantMapper.findByName("新水泥厂")).thenReturn(null);
        doNothing().when(cementPlantMapper).insert(any(CementPlant.class));
        doNothing().when(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));

        // 执行测试
        cementPlantService.addCementPlantData(dataDTO);

        // 验证结果
        verify(cementPlantMapper).findByName("新水泥厂");
        verify(cementPlantMapper).insert(any(CementPlant.class));
        verify(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));
    }

    @Test
    void testAddCementPlantData_ExistingPlant() {
        // 准备测试数据
        CementPlantDataDTO dataDTO = new CementPlantDataDTO();
        dataDTO.setPlantName("已存在的水泥厂");
        dataDTO.setLongitude(118.0);
        dataDTO.setLatitude(32.0);
        dataDTO.setProvince("江苏省");
        dataDTO.setCity("南京市");

        when(cementPlantMapper.findByName("已存在的水泥厂")).thenReturn(testPlant);
        doNothing().when(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));

        // 执行测试
        cementPlantService.addCementPlantData(dataDTO);

        // 验证结果
        verify(cementPlantMapper).findByName("已存在的水泥厂");
        verify(cementPlantMapper, never()).insert(any(CementPlant.class));
        verify(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));
    }

    @Test
    void testAddCementPlantData_WithTifFile() {
        // 准备测试数据
        CementPlantDataDTO dataDTO = new CementPlantDataDTO();
        dataDTO.setPlantName("新水泥厂");
        dataDTO.setLongitude(118.0);
        dataDTO.setLatitude(32.0);
        dataDTO.setProvince("江苏省");
        dataDTO.setCity("南京市");
        dataDTO.setTifFilePath("/path/to/file.tif");

        when(cementPlantMapper.findByName("新水泥厂")).thenReturn(null);
        doNothing().when(cementPlantMapper).insert(any(CementPlant.class));
        doNothing().when(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));
        when(geospatialMapper.findFileByPath("/path/to/file.tif")).thenReturn(null);
        doNothing().when(geospatialMapper).insertFile(any(GeospatialFile.class));
        doNothing().when(geospatialMapper).insertGeodataRelation(any(CementPlantGeodata.class));

        // 执行测试
        cementPlantService.addCementPlantData(dataDTO);

        // 验证结果
        verify(cementPlantMapper).findByName("新水泥厂");
        verify(cementPlantMapper).insert(any(CementPlant.class));
        verify(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));
        verify(geospatialMapper).findFileByPath("/path/to/file.tif");
        verify(geospatialMapper).insertFile(any(GeospatialFile.class));
        verify(geospatialMapper).insertGeodataRelation(any(CementPlantGeodata.class));
    }

    @Test
    void testAddCementPlantData_WithExistingTifFile() {
        // 准备测试数据
        CementPlantDataDTO dataDTO = new CementPlantDataDTO();
        dataDTO.setPlantName("新水泥厂");
        dataDTO.setLongitude(118.0);
        dataDTO.setLatitude(32.0);
        dataDTO.setProvince("江苏省");
        dataDTO.setCity("南京市");
        dataDTO.setTifFilePath("/path/to/existing.tif");

        GeospatialFile existingFile = new GeospatialFile();
        existingFile.setFileId(1L);
        existingFile.setFilePath("/path/to/existing.tif");

        when(cementPlantMapper.findByName("新水泥厂")).thenReturn(null);
        doNothing().when(cementPlantMapper).insert(any(CementPlant.class));
        doNothing().when(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));
        when(geospatialMapper.findFileByPath("/path/to/existing.tif")).thenReturn(existingFile);
        doNothing().when(geospatialMapper).insertGeodataRelation(any(CementPlantGeodata.class));

        // 执行测试
        cementPlantService.addCementPlantData(dataDTO);

        // 验证结果
        verify(cementPlantMapper).findByName("新水泥厂");
        verify(cementPlantMapper).insert(any(CementPlant.class));
        verify(cementPlantMapper).insertIdentification(any(CementPlantIdentification.class));
        verify(geospatialMapper).findFileByPath("/path/to/existing.tif");
        verify(geospatialMapper, never()).insertFile(any(GeospatialFile.class));
        verify(geospatialMapper).insertGeodataRelation(any(CementPlantGeodata.class));
    }
}
