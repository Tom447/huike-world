package com.huike.task;

import com.huike.domain.business.TbBusiness;
import com.huike.domain.system.SysUser;
import com.huike.mapper.SysUserMapper;
import com.huike.mapper.TbBusinessMapper;
import com.huike.mapper.TbClueMapper;
import com.huike.mapper.TbContractMapper;
import com.huike.utils.EmailUtils;
import com.huike.utils.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class MonthReportTask {

    @Autowired
    private TbClueMapper clueMapper;

    @Autowired
    private TbBusinessMapper businessMapper;

    @Autowired
    private TbContractMapper contractMapper;

    @Autowired
    private EmailUtils emailUtils;

    @Value("${spring.mail.username}")
    private String from; //发件人

    @Autowired
    private SysUserMapper sysUserMapper;

    @Scheduled(cron = "0 0 23 L * ?")
    public void sendMonthReport() throws Exception {
        //1. 获取本月数据
        //1.1 获取当月的开始时间 与 结束时间
        LocalDate beginDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 1);
        LocalDate endDate = YearMonth.now().atEndOfMonth();

        String begin = beginDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String end = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //1.2 获取本月的线索总数
        int allClues = clueMapper.countAllClues(begin, end);

        //1.3 获取本月的商机总数
        int allBusiness = businessMapper.countAllBusiness(begin, end);

        //1.4 获取本月的合同总数
        int allContracts = contractMapper.countAllContracts(begin, end);

        //1.5 获取本月的销售额
        double totalSales = contractMapper.sumAllSalesStatistics(begin, end);



        //2 获取上一个月的数据值
        LocalDate _beginDate = LocalDate.of(LocalDate.now().minusMonths(1).getYear(), LocalDate.now().minusMonths(1).getMonthValue(), 1);
        LocalDate _endDate = YearMonth.now().minusMonths(1).atEndOfMonth();

        String _begin = _beginDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String _end = _endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //2.1 获取上月的线索总数
        int _allClues = clueMapper.countAllClues(_begin, _end);
        int diffClue = allClues - _allClues;

        //2.2 获取上月的商机总数
        int _allBusiness = businessMapper.countAllBusiness(_begin, _end);
        int diffBusiness = allBusiness - _allBusiness;

        //2.3 获取上月的合同总数
        int _allContracts = contractMapper.countAllContracts(_begin, _end);
        int diffContracts = allContracts - _allContracts;

        //2.4 获取上月的销售额
        double _totalSales = contractMapper.sumAllSalesStatistics(_begin, _end);
        double diffSales = totalSales - _totalSales;


        //3. 发送邮件
        //3.1 获取邮件模板 .
        String template = loadReportTemplate();

        //3.2 填充数据 .
        String text = String.format(template,
                allClues, diffClue * 100/_allClues,
                allBusiness, diffBusiness * 100/_allBusiness,
                allContracts, diffContracts * 100/_allContracts,
                Math.round(totalSales), Math.round(diffSales * 100/_totalSales));

        //3.3 发送邮件 .
        //获取超级管理员邮箱
        SysUser sysUser = sysUserMapper.selectUserById(1L);
        emailUtils.sendMailWithoutAttachment(from , sysUser.getEmail(), "月度统计报表", text);
    }



    private String loadReportTemplate() throws IOException {
        //1. 加载邮件的正文模板
        String file = this.getClass().getClassLoader().getResource("templates/MonthReport.html").getFile();
        String template = FileUtils.readFileToString(new File(file), "UTF-8");
        return template;
    }


}
