package com.neusoft.quartz;

import org.quartz.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @Title: quartztest
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/3013:47
 */
public class quartztest {

    public static class HelloJob implements Job{

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            int id = jobDataMap.getInt("id");
            int age = jobDataMap.getInt("age");
            System.out.println("id:"+id);
            System.out.println("age:"+age);

            System.out.println("now is :" + System.currentTimeMillis());
        }
    }

    public static void main(String[] args) throws SchedulerException {

        SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

        Scheduler sched = schedFact.getScheduler();

        sched.start();

        JobDataMap map = new JobDataMap();
        map.put("id", 12312342);

        // define the job and tie it to our HelloJob class
        JobDetail job = newJob(HelloJob.class)
                .withIdentity("myJob", "group1")
                .usingJobData(map)
                .usingJobData("age", 123)
                .build();

        // Trigger the job to run now, and then every 40 seconds
        Trigger trigger = newTrigger()
                .withIdentity("myTrigger", "group1")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .build();

        // Tell quartz to schedule the job using our trigger
        sched.scheduleJob(job, trigger);

    }

}
