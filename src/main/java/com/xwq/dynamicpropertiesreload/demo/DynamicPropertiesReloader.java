package com.xwq.dynamicpropertiesreload.demo;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Properties文件动态加载
 * @author by Joney on 2019/1/8 17:41
 */
@Component
public class DynamicPropertiesReloader {

    private static Logger logger = LoggerFactory.getLogger(DynamicPropertiesReloader.class);

    private static ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder;

    private static PropertiesConfiguration propertiesConfiguration;

    @PostConstruct
    public void reload() throws InterruptedException {
        // 基于文件的重新加载策略配置构建器
        Parameters params = new Parameters();
        builder = new ReloadingFileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(params.fileBased().setFile(new File("src/main/resources/application.properties")));

        // 重新加载调度器-每隔1秒
        PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(builder.getReloadingController(), null, 1, TimeUnit.SECONDS);
        trigger.start();

        // 添加事件监听器-重置事件RESET即为Reload
        builder.addEventListener(ConfigurationBuilderEvent.ANY, event -> {
            logger.info("Event:" + event);
            if (event.getEventType().equals(ConfigurationBuilderEvent.RESET)) {
                loadConfiguration();
                logger.info(propertiesConfiguration.getString("name"));
            }
        });
        loadConfiguration();

        Thread.sleep(6000);
        logger.info("name -> ", propertiesConfiguration.getString("name"));

//        while (true) {
//            Thread.sleep(6000);
//            System.out.println(builder.getConfiguration().getString("name"));
//        }
    }

    /**
     * 获取文件中的配置属性
     * @author by Joney on 2019/1/8 17:39
     */
    public void loadConfiguration() {
        try {
            propertiesConfiguration = builder.getConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
}
