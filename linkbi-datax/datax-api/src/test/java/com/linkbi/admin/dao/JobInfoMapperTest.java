package com.linkbi.admin.dao;

import com.linkbi.common.utils.uuid.IdUtils;
import com.linkbi.datax.api.domain.JobInfo;
import com.linkbi.datax.api.mapper.JobInfoMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JobInfoMapperTest {
	
	@Resource
	private JobInfoMapper jobInfoMapper;
	
	@Test
	public void pageList(){
		List<JobInfo> list = jobInfoMapper.pageList(0, 20, 0, -1, null, null, 0,0);
		int list_count = jobInfoMapper.pageListCount(0, 20, 0, -1, null, null, 0,0);
		
		System.out.println(list);
		System.out.println(list_count);

		List<JobInfo> list2 = jobInfoMapper.getJobsByGroup(1);
	}
	
	@Test
	public void save_load(){
		JobInfo info = new JobInfo();
		info.setJobGroup(1L);
		info.setJobCron("jobCron");
		info.setJobDesc("desc");
		info.setUserId(1L);
		info.setAlarmEmail("setAlarmEmail");
		info.setExecutorRouteStrategy("setExecutorRouteStrategy");
		info.setExecutorHandler("setExecutorHandler");
		info.setExecutorParam("setExecutorParam");
		info.setExecutorBlockStrategy("setExecutorBlockStrategy");
		info.setGlueType("setGlueType");
		info.setGlueSource("setGlueSource");
		info.setGlueRemark("setGlueRemark");
		info.setChildJobId("1");

		info.setAddTime(new Date());
		info.setUpdateTime(new Date());
		info.setGlueUpdatetime(new Date());
		info.setId(IdUtils.getId());
		int count = jobInfoMapper.save(info);

		JobInfo info2 = jobInfoMapper.loadById(info.getId());
		info2.setJobCron("jobCron2");
		info2.setJobDesc("desc2");
		info2.setUserId(1L);
		info2.setAlarmEmail("setAlarmEmail2");
		info2.setExecutorRouteStrategy("setExecutorRouteStrategy2");
		info2.setExecutorHandler("setExecutorHandler2");
		info2.setExecutorParam("setExecutorParam2");
		info2.setExecutorBlockStrategy("setExecutorBlockStrategy2");
		info2.setGlueType("setGlueType2");
		info2.setGlueSource("setGlueSource2");
		info2.setGlueRemark("setGlueRemark2");
		info2.setGlueUpdatetime(new Date());
		info2.setChildJobId("1");

		info2.setUpdateTime(new Date());
		int item2 = jobInfoMapper.update(info2);

		jobInfoMapper.delete(info2.getId());

		List<JobInfo> list2 = jobInfoMapper.getJobsByGroup(1);

		int ret3 = jobInfoMapper.findAllCount();

	}

}
