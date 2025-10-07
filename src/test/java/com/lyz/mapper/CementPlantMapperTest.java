package com.lyz.mapper;

import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 水泥厂Mapper集成测试
 * 注意：这些测试需要真实的数据库连接
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional // 确保测试后回滚数据
class CementPlantMapperTest {

    @Autowired
    private CementPlantMapper cementPlantMapper;

    @Test
    void testFindById_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        // 执行测试
        CementPlant result = cementPlantMapper.findById(plantId);

        // 验证结果
        assertNotNull(result);
        assertEquals(plantId, result.getPlantId());
        assertEquals("测试水泥厂", result.getPlantName());
        assertEquals(118.0, result.getLongitude());
        assertEquals(32.0, result.getLatitude());
        assertEquals("江苏省", result.getProvince());
        assertEquals("南京市", result.getCity());
        assertEquals("active", result.getStatus());
    }

    @Test
    void testFindById_NotFound() {
        // 执行测试
        CementPlant result = cementPlantMapper.findById(999L);

        // 验证结果
        assertNull(result);
    }

    @Test
    void testFindByName_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("按名称查找的水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);

        // 执行测试
        CementPlant result = cementPlantMapper.findByName("按名称查找的水泥厂");

        // 验证结果
        assertNotNull(result);
        assertEquals("按名称查找的水泥厂", result.getPlantName());
    }

    @Test
    void testFindByName_NotFound() {
        // 执行测试
        CementPlant result = cementPlantMapper.findByName("不存在的水泥厂");

        // 验证结果
        assertNull(result);
    }

    @Test
    void testListPlants_Success() {
        // 准备测试数据
        CementPlant plant1 = new CementPlant();
        plant1.setPlantName("水泥厂1");
        plant1.setLongitude(118.0);
        plant1.setLatitude(32.0);
        plant1.setProvince("江苏省");
        plant1.setCity("南京市");
        plant1.setStatus("active");

        CementPlant plant2 = new CementPlant();
        plant2.setPlantName("水泥厂2");
        plant2.setLongitude(119.0);
        plant2.setLatitude(33.0);
        plant2.setProvince("浙江省");
        plant2.setCity("杭州市");
        plant2.setStatus("active");

        cementPlantMapper.insert(plant1);
        cementPlantMapper.insert(plant2);

        // 执行测试
        List<CementPlant> result = cementPlantMapper.listPlants();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 2);
        
        // 验证数据按ID降序排列
        boolean foundPlant1 = result.stream().anyMatch(p -> "水泥厂1".equals(p.getPlantName()));
        boolean foundPlant2 = result.stream().anyMatch(p -> "水泥厂2".equals(p.getPlantName()));
        assertTrue(foundPlant1);
        assertTrue(foundPlant2);
    }

    @Test
    void testInsert_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("新插入的水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setDistrict("鼓楼区");
        plant.setStatus("active");

        // 执行测试
        cementPlantMapper.insert(plant);

        // 验证结果
        assertNotNull(plant.getPlantId());
        assertTrue(plant.getPlantId() > 0);

        // 验证数据是否正确插入
        CementPlant result = cementPlantMapper.findById(plant.getPlantId());
        assertNotNull(result);
        assertEquals("新插入的水泥厂", result.getPlantName());
        assertEquals(118.0, result.getLongitude());
        assertEquals(32.0, result.getLatitude());
        assertEquals("江苏省", result.getProvince());
        assertEquals("南京市", result.getCity());
        assertEquals("鼓楼区", result.getDistrict());
        assertEquals("active", result.getStatus());
    }

    @Test
    void testUpdate_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("原始水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        // 更新数据
        plant.setPlantName("更新后的水泥厂");
        plant.setLongitude(119.0);
        plant.setLatitude(33.0);
        plant.setProvince("浙江省");
        plant.setCity("杭州市");
        plant.setStatus("inactive");

        // 执行测试
        cementPlantMapper.update(plant);

        // 验证结果
        CementPlant result = cementPlantMapper.findById(plantId);
        assertNotNull(result);
        assertEquals("更新后的水泥厂", result.getPlantName());
        assertEquals(119.0, result.getLongitude());
        assertEquals(33.0, result.getLatitude());
        assertEquals("浙江省", result.getProvince());
        assertEquals("杭州市", result.getCity());
        assertEquals("inactive", result.getStatus());
    }

    @Test
    void testDeleteById_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("要删除的水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        // 验证数据存在
        CementPlant beforeDelete = cementPlantMapper.findById(plantId);
        assertNotNull(beforeDelete);

        // 执行测试
        cementPlantMapper.deleteById(plantId);

        // 验证结果
        CementPlant afterDelete = cementPlantMapper.findById(plantId);
        assertNull(afterDelete);
    }

    @Test
    void testFindByLocationRange_Success() {
        // 准备测试数据
        CementPlant plant1 = new CementPlant();
        plant1.setPlantName("范围内的水泥厂1");
        plant1.setLongitude(118.5);
        plant1.setLatitude(32.5);
        plant1.setProvince("江苏省");
        plant1.setCity("南京市");
        plant1.setStatus("active");

        CementPlant plant2 = new CementPlant();
        plant2.setPlantName("范围外的水泥厂");
        plant2.setLongitude(120.0);
        plant2.setLatitude(35.0);
        plant2.setProvince("山东省");
        plant2.setCity("济南市");
        plant2.setStatus("active");

        cementPlantMapper.insert(plant1);
        cementPlantMapper.insert(plant2);

        // 执行测试
        List<CementPlant> result = cementPlantMapper.findByLocationRange(118.0, 119.0, 32.0, 33.0);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 1);
        
        boolean foundInRange = result.stream().anyMatch(p -> "范围内的水泥厂1".equals(p.getPlantName()));
        boolean foundOutOfRange = result.stream().anyMatch(p -> "范围外的水泥厂".equals(p.getPlantName()));
        
        assertTrue(foundInRange);
        assertFalse(foundOutOfRange);
    }

    @Test
    void testFindIdentificationById_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("识别测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        CementPlantIdentification identification = new CementPlantIdentification();
        identification.setPlantId(plantId);
        identification.setLongitude(118.0);
        identification.setLatitude(32.0);
        identification.setIdentificationTime(LocalDateTime.now());
        identification.setDataSource("GF-2");
        identification.setImageUuid("test-uuid-123");
        identification.setNdviIndex(0.5);
        identification.setProvince("江苏省");
        identification.setCity("南京市");

        cementPlantMapper.insertIdentification(identification);
        Long identificationId = identification.getIdentificationId();

        // 执行测试
        CementPlantIdentification result = cementPlantMapper.findIdentificationById(identificationId);

        // 验证结果
        assertNotNull(result);
        assertEquals(identificationId, result.getIdentificationId());
        assertEquals(plantId, result.getPlantId());
        assertEquals("识别测试水泥厂", result.getPlantName());
        assertEquals(118.0, result.getLongitude());
        assertEquals(32.0, result.getLatitude());
        assertEquals("GF-2", result.getDataSource());
        assertEquals("test-uuid-123", result.getImageUuid());
        assertEquals(0.5, result.getNdviIndex());
    }

    @Test
    void testListIdentifications_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("识别列表测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        CementPlantIdentification identification1 = new CementPlantIdentification();
        identification1.setPlantId(plantId);
        identification1.setLongitude(118.0);
        identification1.setLatitude(32.0);
        identification1.setIdentificationTime(LocalDateTime.now().minusHours(1));
        identification1.setDataSource("GF-2");

        CementPlantIdentification identification2 = new CementPlantIdentification();
        identification2.setPlantId(plantId);
        identification2.setLongitude(118.1);
        identification2.setLatitude(32.1);
        identification2.setIdentificationTime(LocalDateTime.now());
        identification2.setDataSource("Landsat-8");

        cementPlantMapper.insertIdentification(identification1);
        cementPlantMapper.insertIdentification(identification2);

        // 执行测试
        List<CementPlantIdentification> result = cementPlantMapper.listIdentifications();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 2);
        
        // 验证数据按时间降序排列
        boolean foundId1 = result.stream().anyMatch(i -> i.getIdentificationId().equals(identification1.getIdentificationId()));
        boolean foundId2 = result.stream().anyMatch(i -> i.getIdentificationId().equals(identification2.getIdentificationId()));
        assertTrue(foundId1);
        assertTrue(foundId2);
    }

    @Test
    void testInsertIdentification_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("识别插入测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        CementPlantIdentification identification = new CementPlantIdentification();
        identification.setPlantId(plantId);
        identification.setLongitude(118.0);
        identification.setLatitude(32.0);
        identification.setIdentificationTime(LocalDateTime.now());
        identification.setDataSource("GF-2");
        identification.setImageUuid("insert-test-uuid");
        identification.setNdviIndex(0.7);
        identification.setProvince("江苏省");
        identification.setCity("南京市");
        identification.setDistrict("鼓楼区");

        // 执行测试
        cementPlantMapper.insertIdentification(identification);

        // 验证结果
        assertNotNull(identification.getIdentificationId());
        assertTrue(identification.getIdentificationId() > 0);

        // 验证数据是否正确插入
        CementPlantIdentification result = cementPlantMapper.findIdentificationById(identification.getIdentificationId());
        assertNotNull(result);
        assertEquals(plantId, result.getPlantId());
        assertEquals(118.0, result.getLongitude());
        assertEquals(32.0, result.getLatitude());
        assertEquals("GF-2", result.getDataSource());
        assertEquals("insert-test-uuid", result.getImageUuid());
        assertEquals(0.7, result.getNdviIndex());
        assertEquals("江苏省", result.getProvince());
        assertEquals("南京市", result.getCity());
        assertEquals("鼓楼区", result.getDistrict());
    }

    @Test
    void testUpdateIdentification_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("识别更新测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        CementPlantIdentification identification = new CementPlantIdentification();
        identification.setPlantId(plantId);
        identification.setLongitude(118.0);
        identification.setLatitude(32.0);
        identification.setIdentificationTime(LocalDateTime.now());
        identification.setDataSource("GF-2");
        identification.setImageUuid("original-uuid");
        identification.setNdviIndex(0.5);

        cementPlantMapper.insertIdentification(identification);
        Long identificationId = identification.getIdentificationId();

        // 更新数据
        identification.setLongitude(118.1);
        identification.setLatitude(32.1);
        identification.setDataSource("Landsat-8");
        identification.setImageUuid("updated-uuid");
        identification.setNdviIndex(0.8);

        // 执行测试
        cementPlantMapper.updateIdentification(identification);

        // 验证结果
        CementPlantIdentification result = cementPlantMapper.findIdentificationById(identificationId);
        assertNotNull(result);
        assertEquals(118.1, result.getLongitude());
        assertEquals(32.1, result.getLatitude());
        assertEquals("Landsat-8", result.getDataSource());
        assertEquals("updated-uuid", result.getImageUuid());
        assertEquals(0.8, result.getNdviIndex());
    }

    @Test
    void testDeleteIdentificationById_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("识别删除测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        CementPlantIdentification identification = new CementPlantIdentification();
        identification.setPlantId(plantId);
        identification.setLongitude(118.0);
        identification.setLatitude(32.0);
        identification.setIdentificationTime(LocalDateTime.now());
        identification.setDataSource("GF-2");

        cementPlantMapper.insertIdentification(identification);
        Long identificationId = identification.getIdentificationId();

        // 验证数据存在
        CementPlantIdentification beforeDelete = cementPlantMapper.findIdentificationById(identificationId);
        assertNotNull(beforeDelete);

        // 执行测试
        cementPlantMapper.deleteIdentificationById(identificationId);

        // 验证结果
        CementPlantIdentification afterDelete = cementPlantMapper.findIdentificationById(identificationId);
        assertNull(afterDelete);
    }

    @Test
    void testCountActivePlants_Success() {
        // 准备测试数据
        CementPlant activePlant = new CementPlant();
        activePlant.setPlantName("活跃水泥厂");
        activePlant.setLongitude(118.0);
        activePlant.setLatitude(32.0);
        activePlant.setProvince("江苏省");
        activePlant.setCity("南京市");
        activePlant.setStatus("active");

        CementPlant inactivePlant = new CementPlant();
        inactivePlant.setPlantName("非活跃水泥厂");
        inactivePlant.setLongitude(119.0);
        inactivePlant.setLatitude(33.0);
        inactivePlant.setProvince("浙江省");
        inactivePlant.setCity("杭州市");
        inactivePlant.setStatus("inactive");

        cementPlantMapper.insert(activePlant);
        cementPlantMapper.insert(inactivePlant);

        // 执行测试
        Integer result = cementPlantMapper.countActivePlants();

        // 验证结果
        assertNotNull(result);
        assertTrue(result >= 1);
    }

    @Test
    void testCountIdentificationsByPlantId_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("计数测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        // 插入多个识别记录
        for (int i = 0; i < 3; i++) {
            CementPlantIdentification identification = new CementPlantIdentification();
            identification.setPlantId(plantId);
            identification.setLongitude(118.0 + i * 0.1);
            identification.setLatitude(32.0 + i * 0.1);
            identification.setIdentificationTime(LocalDateTime.now().minusHours(i));
            identification.setDataSource("GF-2");
            cementPlantMapper.insertIdentification(identification);
        }

        // 执行测试
        Integer result = cementPlantMapper.countIdentificationsByPlantId(plantId);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result);
    }

    @Test
    void testCountIdentificationsByTimeRange_Success() {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("时间范围计数测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantMapper.insert(plant);
        Long plantId = plant.getPlantId();

        // 插入不同时间的识别记录
        CementPlantIdentification identification1 = new CementPlantIdentification();
        identification1.setPlantId(plantId);
        identification1.setLongitude(118.0);
        identification1.setLatitude(32.0);
        identification1.setIdentificationTime(LocalDateTime.of(2023, 6, 15, 10, 0));
        identification1.setDataSource("GF-2");
        cementPlantMapper.insertIdentification(identification1);

        CementPlantIdentification identification2 = new CementPlantIdentification();
        identification2.setPlantId(plantId);
        identification2.setLongitude(118.1);
        identification2.setLatitude(32.1);
        identification2.setIdentificationTime(LocalDateTime.of(2023, 7, 15, 10, 0));
        identification2.setDataSource("GF-2");
        cementPlantMapper.insertIdentification(identification2);

        // 执行测试
        Integer result = cementPlantMapper.countIdentificationsByTimeRange("2023-06-01", "2023-06-30");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result);
    }
}
