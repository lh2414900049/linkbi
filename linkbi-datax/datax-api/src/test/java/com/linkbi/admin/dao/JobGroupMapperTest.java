package com.linkbi.admin.dao;

import com.linkbi.common.utils.uuid.IdUtils;
import com.linkbi.datax.api.domain.JobGroup;
import com.linkbi.datax.api.mapper.JobGroupMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JobGroupMapperTest {

    @Resource
    private JobGroupMapper jobGroupMapper;

    @Test
    public void test(){
        List<JobGroup> list = jobGroupMapper.findAll();

        List<JobGroup> list2 = jobGroupMapper.findByAddressType(0);

        JobGroup group = new JobGroup();
        group.setAppName("setAppName");
        group.setTitle("setTitle");
        group.setOrder(1);
        group.setAddressType(0);
        group.setAddressList("setAddressList");
        group.setId(IdUtils.getId());
        int ret = jobGroupMapper.save(group);

        JobGroup group2 = jobGroupMapper.load(group.getId());
        group2.setAppName("setAppName2");
        group2.setTitle("setTitle2");
        group2.setOrder(2);
        group2.setAddressType(2);
        group2.setAddressList("setAddressList2");

        int ret2 = jobGroupMapper.update(group2);

        int ret3 = jobGroupMapper.remove(group.getId());
    }

}
