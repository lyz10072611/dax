package com.lyz.config;

import com.lyz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置类
 * 用于处理系统定时任务，如每日下载计数重置（Redis管理）
 */
@Component
public class ScheduledTasks {
    
    @Autowired
    private UserService userService;
    
    /**
     * 每日凌晨2点重置用户下载计数（Redis）
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void resetDailyDownloadCount() {
        try {
            userService.resetDailyDownloadCount();
            System.out.println("每日下载计数重置任务执行完成（Redis）");
        } catch (Exception e) {
            System.err.println("每日下载计数重置任务执行失败: " + e.getMessage());
        }
    }
}
