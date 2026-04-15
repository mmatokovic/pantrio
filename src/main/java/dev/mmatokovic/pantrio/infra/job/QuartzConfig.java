package dev.mmatokovic.pantrio.infra.job;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    public static final String JOB_NAME = "itemEnrichmentJob";
    public static final String TRIGGER_NAME = "itemEnrichmentTrigger";

    @Value("${batch.job.schedule}")
    private String cronExpression;

    @Bean
    public JobDetail itemEnrichmentJobDetail() {
        return JobBuilder.newJob(QuartzJob.class)
            .withIdentity(JOB_NAME)
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger itemEnrichmentTrigger(JobDetail itemEnrichmentJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(itemEnrichmentJobDetail)
            .withIdentity(TRIGGER_NAME)
            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
            .build();
    }
}
