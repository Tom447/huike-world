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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class MonthExcelReportTask {

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

    //@Scheduled(cron = "0 0 23 L * ?")
    @Scheduled(cron = "0/30 * * * * ?")
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


        //3. 概览数据 - 这个月总共的线索数 , 商机数 , 合同数 , 销售额
        Integer totalClue = clueCountDTOList.stream().map(ClueCountDTO::getClueCount).reduce(0, Integer::sum); //总线索数
        Integer totalBusiness = businessCountDTOList.stream().map(BusinessCountDTO::getBusinessCount).reduce(0, Integer::sum); //总商机数
        Integer totalContract = contractCountDTOList.stream().map(ContractCountDTO::getContractCount).reduce(0, Integer::sum); //总合同数
        Double totalMoney = contractMoneyDTOList.stream().map(ContractMoneyDTO::getContractMoney).reduce(0.0, Double::sum); //总销售额

        String dateRange = "时间范围: " + begin + " 至 " + end; //时间


        //4. 构建数据模型 -- 明细列表
        List<DetailReportVo> reportVoList = dateList.stream().map(date -> {
            return DetailReportVo.builder()
                    .date(date)
                    .newClueCount(clueDataMap.get(date) == null ? 0 : clueDataMap.get(date))
                    .newBusinessCount(businessDataMap.get(date) == null ? 0 : businessDataMap.get(date))
                    .newContractCount(contractDataMap.get(date) == null ? 0 : contractDataMap.get(date))
                    .saleMoney(contractMoneyMap.get(date) == null ? 0.0 : contractMoneyMap.get(date))
                    .build();
        }).collect(Collectors.toList());


        //5. 加载Excel的模板文件
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/客达天下-月度运营统计.xlsx");
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        //6. 定位单元格 , 基于POI完成数据填充
        sheet.getRow(1).getCell(1).setCellValue(dateRange); //时间
        sheet.getRow(3).getCell(1).setCellValue(totalClue); //总线索数
        sheet.getRow(3).getCell(2).setCellValue(totalBusiness); //总商机数
        sheet.getRow(3).getCell(3).setCellValue(totalContract); //总合同数
        sheet.getRow(3).getCell(4).setCellValue(totalMoney); //总销售额

        //遍历列表数据, 填充每一天的运营数据
        for (int i = 0; i < reportVoList.size(); i++) {
            DetailReportVo reportVo = reportVoList.get(i);

            sheet.getRow(5 + i).getCell(0).setCellValue(i + 1);
            sheet.getRow(5 + i).getCell(1).setCellValue(reportVo.getDate());
            sheet.getRow(5 + i).getCell(2).setCellValue(reportVo.getNewClueCount());
            sheet.getRow(5 + i).getCell(3).setCellValue(reportVo.getNewBusinessCount());
            sheet.getRow(5 + i).getCell(4).setCellValue(reportVo.getNewContractCount());
            sheet.getRow(5 + i).getCell(5).setCellValue(reportVo.getSaleMoney());
        }


        //将Excel文件数据, 输出 --- > ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        //6. 发送附件邮件
        SysUser admin = sysUserMapper.selectUserById(1L);
        String subject = "月度运营统计报表";
        String text = "您好, 领导, 附件是本月公司运营详细统计 , 请及时查收 !";
        String fileName = "月度运营统计报表.xlsx";
        emailUtils.sendMailWithAttachment(from , admin.getEmail(), subject, text, fileName, outputStream.toByteArray());
        log.info("月底统计企业运营信息 , 发送附件邮件 ....");
    }


}
