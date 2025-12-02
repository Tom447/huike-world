package com.huike.config;

import com.huike.common.config.MinioConfig;
import com.huike.utils.MinioUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Bean
    public MinioUtils minioUtils(MinioConfig minioConfig){
        return new MinioUtils(minioConfig);
    }

}
