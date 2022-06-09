package org.graylog2.plugin.custom.timeseriesdb;

import org.graylog2.plugin.custom.pipeline.UpdateStatusFunction;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 调度构建
 */
public class TS_JobHandler {

    //创建调度器
    public static Scheduler getScheduler() throws SchedulerException {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            return schedulerFactory.getScheduler();
        } catch (Exception ex) {
            return null;
        }
    }

    public static void schedulerJob() throws SchedulerException {
        try {
            //创建任务
            JobDetail jobDetail = JobBuilder.newJob(TS_Job.class).withIdentity("job1", "group1").build();
            //创建触发器 每10秒钟执行一次
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(1000 * 10).repeatForever())
                    .build();
            Scheduler scheduler = getScheduler();
            //将任务及其触发器放入调度器
            scheduler.scheduleJob(jobDetail, trigger);
            //调度器开始调度任务
            scheduler.start();
        } catch (Exception ex) {
            System.out.println("schedulerJob error:" + ex.getMessage());
        }
    }
}
