package com.huike.task;

import com.huike.domain.business.TbBusiness;
import com.huike.domain.system.SysUser;
import com.huike.mapper.SysDictDataMapper;
import com.huike.mapper.TbAssignRecordMapper;
import com.huike.mapper.TbBusinessMapper;
import com.huike.utils.EmailUtils;
import com.huike.utils.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BusinessTrackNotifyTask {

    @Autowired
    private TbBusinessMapper businessMapper;

    @Autowired
    private TbAssignRecordMapper tbAssignRecordMapper; //商机分配人

    @Autowired
    private SysDictDataMapper sysDictDataMapper; //数据字典

    @Autowired
    private EmailUtils emailUtils; //发送邮件的工具类

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String from; //发件人

    private static final String BUSINESS_NOTIFY_PREFIX = "business:notify:";

    @Scheduled(cron = "0/30 * * * * ?")
    public void sendNotifyEmail() throws Exception {
        //1. 查询本次需要跟进的商机
        List<TbBusiness> businessList = businessMapper.selectByNextTime(LocalDateTime.now(), LocalDateTime.now().plusHours(2), "2");

        if(!CollectionUtils.isEmpty(businessList)){
            for (TbBusiness business : businessList) {
                //查询是否已发送
                Object flag = redisTemplate.opsForValue().get(BUSINESS_NOTIFY_PREFIX + business.getId());

                if(ObjectUtils.isEmpty(flag)){
                    //2. 查询商机跟进人的信息
                    SysUser sysUser = tbAssignRecordMapper.selectByAssignIdAndType(business.getId(), "1");

                    //3. 发送提醒邮件
                    sendMail(business, sysUser);
                }
            }
        }
    }

    private void sendMail(TbBusiness business, SysUser sysUser) throws Exception {
        //发送邮件
        String text = handleMailText(business, sysUser);
        emailUtils.sendMailWithoutAttachment(from, sysUser.getEmail(), "商机跟进提醒", text);

        log.info("发送邮件, 给指定员工: {} , 邮件正文: {}", sysUser.getEmail(), text);

        //存储已发送标识
        redisTemplate.opsForValue().set(BUSINESS_NOTIFY_PREFIX + business.getId(), "1", 150, TimeUnit.MINUTES);
    }


    private String handleMailText(TbBusiness business, SysUser sysUser) throws IOException {
        //1. 加载邮件的正文模板
        String file = this.getClass().getClassLoader().getResource("templates/BusinessNotifyTemplate.html").getFile();
        String text = FileUtils.readFileToString(new File(file), "UTF-8");

        //2. 填充数据
        String subject = business.getSubject(); //意向学科
        String subjectName = sysDictDataMapper.selectDictLabel("course_subject", subject);

        return String.format(text, sysUser.getRealName(), business.getId(), business.getName(), business.getPhone(), business.getSex().equals("0") ? "男" : "女", subjectName);
    }

}
