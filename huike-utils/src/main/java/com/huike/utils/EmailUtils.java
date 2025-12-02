package com.huike.utils;

import com.huike.common.constant.MessageConstants;
import com.huike.common.exception.CustomException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.internet.MimeMessage;
import java.io.File;

public class EmailUtils {

    private JavaMailSender javaMailSender;

    public EmailUtils(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * 发送的是不带附件的邮件
     * @param from 发件人
     * @param to 收件人
     * @param subject 主题
     * @param text 正文
     * @param cc 抄送
     */
    public void sendMailWithoutAttachment(String from, String to,  String subject, String text, String ... cc) throws Exception {
        if(StringUtils.isEmpty(from)) {
            throw new CustomException(MessageConstants.EMAIL_FROM_NOT_NULL);
        }
        if(StringUtils.isEmpty(to)){
            throw new CustomException(MessageConstants.EMAIL_TO_NOT_NULL);
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
        messageHelper.setFrom(from);//发件人
        messageHelper.setTo(to);//收件人
        if(cc != null && cc.length>0){
            messageHelper.setCc(cc);//抄送人
        }
        messageHelper.setSubject(subject);//主题
        messageHelper.setText(text , true);//内容
        javaMailSender.send(mimeMessage);
    }


    /**
     *
     * 发送带附件的邮件
     * @param from 发件人
     * @param to 收件人
     * @param subject 主题
     * @param text 正文
     * @param fileName 附件名称
     * @param file 文件
     */
    public void sendMailWithAttachment(String from, String to, String subject, String text, String fileName, File file) throws Exception {
        if(StringUtils.isEmpty(from)) {
            throw new CustomException(MessageConstants.EMAIL_FROM_NOT_NULL);
        }
        if(StringUtils.isEmpty(to)){
            throw new CustomException(MessageConstants.EMAIL_TO_NOT_NULL);
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

        messageHelper.setFrom(from);//发件人
        messageHelper.setTo(to);//收件人
        messageHelper.setSubject(subject);//主题
        messageHelper.setText(text , true);//内容
        messageHelper.addAttachment(fileName, file);
        javaMailSender.send(mimeMessage);
    }



    /**
     *
     * 发送带附件的邮件
     * @param from 发件人
     * @param to 收件人
     * @param subject 主题
     * @param text 正文
     * @param fileName 附件名称
     * @param bytes 文件的字节数组
     */
    public void sendMailWithAttachment(String from, String to, String subject, String text, String fileName, byte[] bytes) throws Exception {
        if(StringUtils.isEmpty(from)) {
            throw new CustomException(MessageConstants.EMAIL_FROM_NOT_NULL);
        }
        if(StringUtils.isEmpty(to)){
            throw new CustomException(MessageConstants.EMAIL_TO_NOT_NULL);
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

        messageHelper.setFrom(from);//发件人
        messageHelper.setTo(to);//收件人
        messageHelper.setSubject(subject);//主题
        messageHelper.setText(text , true);//内容
        messageHelper.addAttachment(fileName, new ByteArrayResource(bytes)); //附件
        javaMailSender.send(mimeMessage);
    }

}
