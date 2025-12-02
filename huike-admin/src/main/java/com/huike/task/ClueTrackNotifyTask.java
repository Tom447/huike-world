package com.huike.task;

import com.huike.domain.clues.TbClue;
import com.huike.domain.system.SysUser;
import com.huike.mapper.TbAssignRecordMapper;
import com.huike.mapper.TbClueMapper;
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
public class ClueTrackNotifyTask {

    @Autowired
    private TbClueMapper tbClueMapper; //线索
    @Autowired
    private TbAssignRecordMapper tbAssignRecordMapper; //线索分配人
    @Autowired
    private EmailUtils emailUtils; //发送邮件的工具类
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String from; //发件人

    private static final String CLUE_NOTIFY_PREFIX = "clue:notify:";

    @Scheduled(cron = "0/30 * * * * ?")
    public void sendNotifyEmail() throws Exception {
        //1. 查询本次需要跟进的线索
        List<TbClue> clueList = tbClueMapper.selectByNextTime(LocalDateTime.now(), LocalDateTime.now().plusHours(2), "2");

        if(!CollectionUtils.isEmpty(clueList)){
            for (TbClue clue : clueList) {
                //查询是否已发送
                Object flag = redisTemplate.opsForValue().get(CLUE_NOTIFY_PREFIX + clue.getId());
                if(ObjectUtils.isEmpty(flag)){
                    //2. 查询线索跟进人的信息
                    SysUser sysUser = tbAssignRecordMapper.selectByAssignIdAndType(clue.getId(), "0");

                    //3. 发送提醒邮件
                    sendMail(clue, sysUser);
                }
            }
        }
    }

    private void sendMail(TbClue clue, SysUser sysUser) throws Exception {
        //发送邮件
        String text = handleMailText(clue, sysUser); //TODO
        emailUtils.sendMailWithoutAttachment(from, sysUser.getEmail(), "线索跟进提醒", text);

        log.info("发送邮件, 给指定员工: {} , 邮件正文: {}", sysUser.getEmail(), text);

        //存储已发送标识
        redisTemplate.opsForValue().set(CLUE_NOTIFY_PREFIX + clue.getId(), "1", 150, TimeUnit.MINUTES);
    }


    private String handleMailText(TbClue clue, SysUser sysUser) throws IOException {
        //1. 加载邮件的正文模板
        String file = this.getClass().getClassLoader().getResource("templates/ClueNotifyTemplate.html").getFile();
        String text = FileUtils.readFileToString(new File(file), "UTF-8");
        //2. 填充数据
        return String.format(text, sysUser.getRealName(), clue.getId(), clue.getName(), clue.getPhone(), clue.getSex().equals("0") ? "男" : "女");
    }

}
