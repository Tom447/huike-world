package com.huike.task;

import com.huike.domain.business.dto.BusinessCountDTO;
import com.huike.domain.clues.dto.ClueCountDTO;
import com.huike.domain.contract.dto.ContractCountDTO;
import com.huike.domain.contract.dto.ContractMoneyDTO;
import com.huike.domain.report.vo.DetailReportVo;
import com.huike.domain.system.SysUser;
import com.huike.mapper.SysUserMapper;
import com.huike.mapper.TbBusinessMapper;
import com.huike.mapper.TbClueMapper;
import com.huike.mapper.TbContractMapper;
import com.huike.utils.EmailUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MonthDetailReportTask {

    @Autowired
    private TbClueMapper clueMapper;
    @Autowired
    private TbBusinessMapper businessMapper;
    @Autowired
    private TbContractMapper contractMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Value("${spring.mail.username}")
    private String from; //发件人

    @Scheduled(cron = "0 0 23 L * ?")
    public void sendMonthDetailEmail() throws Exception {
        //1. 本月的开始时间 , 结束时间 .
        LocalDate begin = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        //1.1 获取每一天
        List<String> dateList = begin.datesUntil(end.plusDays(1)).map(localDate -> localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).collect(Collectors.toList());


        //2. 查询数据库获取指定原始数据 . - 线索 , 商机 , 合同 , 销售额
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //2.1 获取每一天的线索数量
        List<ClueCountDTO> clueCountDTOList = clueMapper.countByEveryDay(beginTime, endTime);
        Map<String, Integer> clueDataMap =  clueCountDTOList.stream().collect(Collectors.toMap(ClueCountDTO::getClueDate, ClueCountDTO::getClueCount));

        //2.2 获取每一天的商机数量
        List<BusinessCountDTO> businessCountDTOList = businessMapper.countByEveryDay(beginTime, endTime);
        Map<String, Integer> businessDataMap = businessCountDTOList.stream().collect(Collectors.toMap(BusinessCountDTO::getBusinessDate, BusinessCountDTO::getBusinessCount));

        //2.3 获取每一天的合同数量
        List<ContractCountDTO> contractCountDTOList = contractMapper.countByEveryDay(beginTime, endTime);
        Map<String, Integer> contractDataMap = contractCountDTOList.stream().collect(Collectors.toMap(ContractCountDTO::getContractDate, ContractCountDTO::getContractCount));

        //2.4 获取每一天的销售额
        List<ContractMoneyDTO> contractMoneyDTOList = contractMapper.sumMoneyByEveryDay(beginTime, endTime);
        Map<String, Double> contractMoneyMap = contractMoneyDTOList.stream().collect(Collectors.toMap(ContractMoneyDTO::getContractDate, ContractMoneyDTO::getContractMoney));

        //3. 构建数据模型 .
        Map<String,Object> dataModel = new HashMap<>();

        List<DetailReportVo> reportVoList = dateList.stream().map(date -> {
            return DetailReportVo.builder()
                    .date(date)
                    .newClueCount(clueDataMap.get(date) == null ? 0 : clueDataMap.get(date))
                    .newBusinessCount(businessDataMap.get(date) == null ? 0 : businessDataMap.get(date))
                    .newContractCount(contractDataMap.get(date) == null ? 0 : contractDataMap.get(date))
                    .saleMoney(contractMoneyMap.get(date) == null ? 0.0 : contractMoneyMap.get(date))
                    .build();
        }).collect(Collectors.toList());

        dataModel.put("itemList", reportVoList);


        //4. 加载模板 .
        Template template = configuration.getTemplate("MonthDetailReport.ftl");

        //5. 生成文本 -- 邮件正文 .
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, dataModel);

        //6. 发送邮件 .
        SysUser admin = sysUserMapper.selectUserById(1L);
        emailUtils.sendMailWithoutAttachment(from, admin.getEmail(), "月度运营数据统计-详细信息", content);
        log.info("已发送月度运营数据统计-详细信息 到 {} 邮箱", admin.getEmail());
    }


}
