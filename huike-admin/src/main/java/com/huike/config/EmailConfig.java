package com.huike.config;

import com.huike.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@Configuration
public class EmailConfig {

    @Bean
    public EmailUtils emailUtils(JavaMailSender javaMailSender){
        log.info("项目启动加载邮件的配置信息 ....");
        return new EmailUtils(javaMailSender);
    }

}
