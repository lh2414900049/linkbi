package com.linkbi.datax.api.service.impl;

import com.linkbi.common.utils.SecurityUtils;
import com.linkbi.datatx.core.biz.model.ReturnT;
import com.linkbi.datatx.core.enums.ExecutorBlockStrategyEnum;
import com.linkbi.datatx.core.glue.GlueTypeEnum;
import com.linkbi.datatx.core.util.Constants;
import com.linkbi.datax.api.core.cron.CronExpression;
import com.linkbi.datax.api.core.route.ExecutorRouteStrategyEnum;
import com.linkbi.datax.api.core.util.I18nUtil;
import com.linkbi.datax.api.domain.JobGroup;
import com.linkbi.datax.api.domain.JobInfo;
import com.linkbi.datax.api.domain.JobTemplate;
import com.linkbi.datax.api.mapper.*;
import com.linkbi.datax.api.service.JobTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * core job action for xxl-job
 *
 * @author xuxueli 2016-5-28 15:30:33
 */
@Service
public class JobTemplateServiceImpl implements JobTemplateService {
    @Resource
    private JobGroupMapper jobGroupMapper;
    @Resource
    private JobTemplateMapper jobTemplateMapper;
    @Resource
    private JobLogMapper jobLogMapper;
    @Resource
    private JobLogGlueMapper jobLogGlueMapper;
    @Resource
    private JobInfoMapper jobInfoMapper;

    @Override
    public Map<String, Object> pageList(int start, int length, long jobGroup, String jobDesc, String executorHandler, long userId, long projectIds) {

        // page list
        List<JobTemplate> list = jobTemplateMapper.pageList(start, length, jobGroup, jobDesc, executorHandler, userId, projectIds);
        int list_count = jobTemplateMapper.pageListCount(start, length, jobGroup, jobDesc, executorHandler, userId, projectIds);

        // package result
        Map<String, Object> maps = new HashMap<>();
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);    // 过滤后的总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    @Override
    public ReturnT<String> add(JobTemplate jobTemplate) {
        // valid
        JobGroup group = jobGroupMapper.load(jobTemplate.getJobGroup());
        if (group == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose") + I18nUtil.getString("jobinfo_field_jobgroup")));
        }
        if (!CronExpression.isValidExpression(jobTemplate.getJobCron())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("jobinfo_field_cron_invalid"));
        }
        if (jobTemplate.getJobDesc() == null || jobTemplate.getJobDesc().trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_field_jobdesc")));
        }
        //if (jobTemplate.getUserId() == 0) {
        //    return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_field_author")));
        //}
        if (ExecutorRouteStrategyEnum.match(jobTemplate.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorRouteStrategy") + I18nUtil.getString("system_invalid")));
        }
        if (ExecutorBlockStrategyEnum.match(jobTemplate.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorBlockStrategy") + I18nUtil.getString("system_invalid")));
        }
        if (GlueTypeEnum.match(jobTemplate.getGlueType()) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_gluetype") + I18nUtil.getString("system_invalid")));
        }
        if (GlueTypeEnum.BEAN == GlueTypeEnum.match(jobTemplate.getGlueType()) && (jobTemplate.getExecutorHandler() == null || jobTemplate.getExecutorHandler().trim().length() == 0)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + "JobHandler"));
        }

        // fix "\r" in shell
        if (GlueTypeEnum.GLUE_SHELL == GlueTypeEnum.match(jobTemplate.getGlueType()) && jobTemplate.getGlueSource() != null) {
            jobTemplate.setGlueSource(jobTemplate.getGlueSource().replaceAll("\r", ""));
        }

        // ChildJobId valid
        if (jobTemplate.getChildJobId() != null && jobTemplate.getChildJobId().trim().length() > 0) {
            String[] childJobIds = jobTemplate.getChildJobId().split(",");
            for (String childJobIdItem : childJobIds) {
                if (StringUtils.isNotBlank(childJobIdItem) && isNumeric(childJobIdItem) && Long.parseLong(childJobIdItem) > 0) {
                    JobInfo jobInfo = jobInfoMapper.loadById(Long.parseLong(childJobIdItem));
                    if (jobInfo == null) {
                        return new ReturnT<String>(ReturnT.FAIL_CODE,
                                MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_not_found")), childJobIdItem));
                    }
                } else {
                    return new ReturnT<String>(ReturnT.FAIL_CODE,
                            MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_invalid")), childJobIdItem));
                }
            }
            // join , avoid "xxx,,"
            String temp = Constants.STRING_BLANK;
            for (String item : childJobIds) {
                temp += item + Constants.SPLIT_COMMA;
            }
            temp = temp.substring(0, temp.length() - 1);

            jobTemplate.setChildJobId(temp);
        }

        if (jobTemplate.getProjectId() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_field_jobproject")));
        }

        // add in db
        jobTemplate.setUserId(SecurityUtils.getLoginUser().getUser().getUserId());
        jobTemplate.setAddTime(new Date());
        jobTemplate.setUpdateTime(new Date());
        jobTemplate.setGlueUpdatetime(new Date());
        jobTemplateMapper.save(jobTemplate);
        if (jobTemplate.getId() < 1) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add") + I18nUtil.getString("system_fail")));
        }

        return new ReturnT<String>(String.valueOf(jobTemplate.getId()));
    }

    private boolean isNumeric(String str) {
        try {
            Long.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public ReturnT<String> update(JobTemplate jobTemplate) {

        // valid
        if (!CronExpression.isValidExpression(jobTemplate.getJobCron())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("jobinfo_field_cron_invalid"));
        }
        if (jobTemplate.getJobDesc() == null || jobTemplate.getJobDesc().trim().length() == 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_field_jobdesc")));
        }
        //if (jobTemplate.getUserId() == 0) {
        //    return new ReturnT<>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_field_author")));
        //}
        if (ExecutorRouteStrategyEnum.match(jobTemplate.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorRouteStrategy") + I18nUtil.getString("system_invalid")));
        }
        if (ExecutorBlockStrategyEnum.match(jobTemplate.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorBlockStrategy") + I18nUtil.getString("system_invalid")));
        }

        // ChildJobId valid
        if (jobTemplate.getChildJobId() != null && jobTemplate.getChildJobId().trim().length() > 0) {
            String[] childJobIds = jobTemplate.getChildJobId().split(",");
            for (String childJobIdItem : childJobIds) {
                if (childJobIdItem != null && childJobIdItem.trim().length() > 0 && isNumeric(childJobIdItem)) {
                    JobTemplate childJobTemplate = jobTemplateMapper.loadById(Long.parseLong(childJobIdItem));
                    if (childJobTemplate == null) {
                        return new ReturnT<String>(ReturnT.FAIL_CODE,
                                MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_not_found")), childJobIdItem));
                    }
                } else {
                    return new ReturnT<String>(ReturnT.FAIL_CODE,
                            MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_invalid")), childJobIdItem));
                }
            }

            // join , avoid "xxx,,"
            String temp = Constants.STRING_BLANK;
            for (String item : childJobIds) {
                temp += item + Constants.SPLIT_COMMA;
            }
            temp = temp.substring(0, temp.length() - 1);

            jobTemplate.setChildJobId(temp);
        }

        // group valid
        JobGroup jobGroup = jobGroupMapper.load(jobTemplate.getJobGroup());
        if (jobGroup == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_jobgroup") + I18nUtil.getString("system_invalid")));
        }

        // stage job info
        JobTemplate exists_jobTemplate = jobTemplateMapper.loadById(jobTemplate.getId());
        if (exists_jobTemplate == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_id") + I18nUtil.getString("system_not_found")));
        }

        // next trigger time (5s后生效，避开预读周期)
        long nextTriggerTime = exists_jobTemplate.getTriggerNextTime();

        exists_jobTemplate.setJobGroup(jobTemplate.getJobGroup());
        exists_jobTemplate.setJobCron(jobTemplate.getJobCron());
        exists_jobTemplate.setJobDesc(jobTemplate.getJobDesc());
        exists_jobTemplate.setUserId(SecurityUtils.getLoginUser().getUser().getUserId());
        exists_jobTemplate.setAlarmEmail(jobTemplate.getAlarmEmail());
        exists_jobTemplate.setExecutorRouteStrategy(jobTemplate.getExecutorRouteStrategy());
        exists_jobTemplate.setExecutorHandler(jobTemplate.getExecutorHandler());
        exists_jobTemplate.setExecutorParam(jobTemplate.getExecutorParam());
        exists_jobTemplate.setExecutorBlockStrategy(jobTemplate.getExecutorBlockStrategy());
        exists_jobTemplate.setExecutorTimeout(jobTemplate.getExecutorTimeout());
        exists_jobTemplate.setExecutorFailRetryCount(jobTemplate.getExecutorFailRetryCount());
        exists_jobTemplate.setChildJobId(jobTemplate.getChildJobId());
        exists_jobTemplate.setTriggerNextTime(nextTriggerTime);
        exists_jobTemplate.setJobJson(jobTemplate.getJobJson());
        exists_jobTemplate.setJvmParam(jobTemplate.getJvmParam());
        exists_jobTemplate.setProjectId(jobTemplate.getProjectId());
        exists_jobTemplate.setUpdateTime(new Date());
        jobTemplateMapper.update(exists_jobTemplate);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> remove(long id) {
        JobTemplate xxlJobTemplate = jobTemplateMapper.loadById(id);
        if (xxlJobTemplate == null) {
            return ReturnT.SUCCESS;
        }

        jobTemplateMapper.delete(id);
        jobLogMapper.delete(id);
        jobLogGlueMapper.deleteByJobId(id);
        return ReturnT.SUCCESS;
    }

}
