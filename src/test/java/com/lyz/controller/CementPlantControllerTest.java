package com.lyz.controller;

import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import com.lyz.pojo.CementPlantDataDTO;
import com.lyz.pojo.PageBean;
import com.lyz.pojo.Result;
import com.lyz.service.CementPlantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 水泥厂控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class CementPlantControllerTest {

    @Mock
    private CementPlantService cementPlantService;

    @InjectMocks
    private CementPlantController cementPlantController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cementPlantController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetPlantById_Success() throws Exception {
        // 准备测试数据
        Long plantId = 1L;
        CementPlant plant = new CementPlant();
        plant.setPlantId(plantId);
        plant.setPlantName("测试水泥厂");
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        when(cementPlantService.findById(plantId)).thenReturn(plant);

        // 执行测试
        mockMvc.perform(get("/cement-plant/{plantId}", plantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.plantId").value(plantId))
                .andExpect(jsonPath("$.data.plantName").value("测试水泥厂"));

        verify(cementPlantService).findById(plantId);
    }

    @Test
    void testGetPlantById_NotFound() throws Exception {
        // 准备测试数据
        Long plantId = 999L;
        when(cementPlantService.findById(plantId)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/cement-plant/{plantId}", plantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("水泥厂不存在"));

        verify(cementPlantService).findById(plantId);
    }

    @Test
    void testListPlants_Success() throws Exception {
        // 准备测试数据
        CementPlant plant1 = new CementPlant();
        plant1.setPlantId(1L);
        plant1.setPlantName("水泥厂1");

        CementPlant plant2 = new CementPlant();
        plant2.setPlantId(2L);
        plant2.setPlantName("水泥厂2");

        List<CementPlant> plants = Arrays.asList(plant1, plant2);
        when(cementPlantService.listPlants()).thenReturn(plants);

        // 执行测试
        mockMvc.perform(get("/cement-plant/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(cementPlantService).listPlants();
    }

    @Test
    void testPagePlants_Success() throws Exception {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantId(1L);
        plant.setPlantName("测试水泥厂");

        PageBean<CementPlant> pageBean = new PageBean<>(1L, Arrays.asList(plant));
        when(cementPlantService.pagePlants(anyInt(), anyInt(), any(), any(), any(), any()))
                .thenReturn(pageBean);

        // 执行测试
        mockMvc.perform(get("/cement-plant/page")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list").isArray());

        verify(cementPlantService).pagePlants(1, 10, null, null, null, null);
    }

    @Test
    void testAddPlant_Success() throws Exception {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("新水泥厂");
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        when(cementPlantService.findByName("新水泥厂")).thenReturn(null);
        doNothing().when(cementPlantService).addPlant(any(CementPlant.class));

        // 执行测试
        mockMvc.perform(post("/cement-plant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("水泥厂添加成功"));

        verify(cementPlantService).findByName("新水泥厂");
        verify(cementPlantService).addPlant(any(CementPlant.class));
    }

    @Test
    void testAddPlant_NameExists() throws Exception {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantName("已存在的水泥厂");

        CementPlant existingPlant = new CementPlant();
        existingPlant.setPlantId(1L);
        existingPlant.setPlantName("已存在的水泥厂");

        when(cementPlantService.findByName("已存在的水泥厂")).thenReturn(existingPlant);

        // 执行测试
        mockMvc.perform(post("/cement-plant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("水泥厂名称已存在"));

        verify(cementPlantService).findByName("已存在的水泥厂");
        verify(cementPlantService, never()).addPlant(any(CementPlant.class));
    }

    @Test
    void testUpdatePlant_Success() throws Exception {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantId(1L);
        plant.setPlantName("更新的水泥厂");

        CementPlant existingPlant = new CementPlant();
        existingPlant.setPlantId(1L);
        existingPlant.setPlantName("原水泥厂");

        when(cementPlantService.findById(1L)).thenReturn(existingPlant);
        doNothing().when(cementPlantService).updatePlant(any(CementPlant.class));

        // 执行测试
        mockMvc.perform(put("/cement-plant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("水泥厂更新成功"));

        verify(cementPlantService).findById(1L);
        verify(cementPlantService).updatePlant(any(CementPlant.class));
    }

    @Test
    void testDeletePlant_Success() throws Exception {
        // 准备测试数据
        Long plantId = 1L;
        CementPlant existingPlant = new CementPlant();
        existingPlant.setPlantId(plantId);
        existingPlant.setPlantName("要删除的水泥厂");

        when(cementPlantService.findById(plantId)).thenReturn(existingPlant);
        doNothing().when(cementPlantService).deletePlant(plantId);

        // 执行测试
        mockMvc.perform(delete("/cement-plant/{plantId}", plantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("水泥厂删除成功"));

        verify(cementPlantService).findById(plantId);
        verify(cementPlantService).deletePlant(plantId);
    }

    @Test
    void testFindPlantsByLocationRange_Success() throws Exception {
        // 准备测试数据
        CementPlant plant = new CementPlant();
        plant.setPlantId(1L);
        plant.setPlantName("位置范围内的水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);

        List<CementPlant> plants = Arrays.asList(plant);
        when(cementPlantService.findPlantsByLocationRange(117.0, 119.0, 31.0, 33.0))
                .thenReturn(plants);

        // 执行测试
        mockMvc.perform(get("/cement-plant/location-range")
                .param("minLng", "117.0")
                .param("maxLng", "119.0")
                .param("minLat", "31.0")
                .param("maxLat", "33.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(cementPlantService).findPlantsByLocationRange(117.0, 119.0, 31.0, 33.0);
    }

    @Test
    void testAddCementPlantData_Success() throws Exception {
        // 准备测试数据
        CementPlantDataDTO dataDTO = new CementPlantDataDTO();
        dataDTO.setPlantName("批量数据水泥厂");
        dataDTO.setLongitude(118.0);
        dataDTO.setLatitude(32.0);
        dataDTO.setProvince("江苏省");
        dataDTO.setCity("南京市");
        dataDTO.setStatus("active");
        dataDTO.setIdentificationTime(LocalDateTime.now());
        dataDTO.setDataSource("GF-2");

        doNothing().when(cementPlantService).addCementPlantData(any(CementPlantDataDTO.class));

        // 执行测试
        mockMvc.perform(post("/cement-plant/batch-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("水泥厂数据添加成功"));

        verify(cementPlantService).addCementPlantData(any(CementPlantDataDTO.class));
    }

    @Test
    void testGetActivePlantsCount_Success() throws Exception {
        // 准备测试数据
        when(cementPlantService.countActivePlants()).thenReturn(5);

        // 执行测试
        mockMvc.perform(get("/cement-plant/stats/active-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(cementPlantService).countActivePlants();
    }

    @Test
    void testGetIdentificationCountByPlantId_Success() throws Exception {
        // 准备测试数据
        Long plantId = 1L;
        when(cementPlantService.countIdentificationsByPlantId(plantId)).thenReturn(3);

        // 执行测试
        mockMvc.perform(get("/cement-plant/stats/identification-count/{plantId}", plantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));

        verify(cementPlantService).countIdentificationsByPlantId(plantId);
    }
}
