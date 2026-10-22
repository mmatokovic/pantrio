package dev.mmatokovic.pantrio.infra.job;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class QuartzConfig {

    public static final String JOB_NAME = "itemEnrichmentJob";
    public static final String TRIGGER_NAME = "itemEnrichmentTrigger";

    @Bean
    JobDetail itemEnrichmentJobDetail() {
        return JobBuilder.newJob(QuartzJob.class)
            .withIdentity(JOB_NAME)
            .storeDurably()
            .build();
    }

    @Bean
    Trigger itemEnrichmentTrigger(JobDetail itemEnrichmentJobDetail, BatchJobProperties batchJobProperties) {
        return TriggerBuilder.newTrigger()
            .forJob(itemEnrichmentJobDetail)
            .withIdentity(TRIGGER_NAME)
            .withSchedule(CronScheduleBuilder.cronSchedule(batchJobProperties.schedule()).withMisfireHandlingInstructionDoNothing())
            .build();
    }
}
