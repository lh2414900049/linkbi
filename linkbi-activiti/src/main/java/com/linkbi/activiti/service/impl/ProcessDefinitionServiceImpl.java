package com.linkbi.activiti.service.impl;

import com.github.pagehelper.Page;
import com.linkbi.activiti.domain.dto.DefinitionIdDTO;
import com.linkbi.activiti.domain.vo.ActReDeploymentVO;
import com.linkbi.activiti.domain.dto.ProcessDefinitionDTO;
import com.linkbi.activiti.mapper.ActReDeploymentMapper;
import com.linkbi.activiti.service.IProcessDefinitionService;
import com.linkbi.common.config.LinkBIConfig;
import com.linkbi.common.core.page.PageDomain;
import com.linkbi.common.utils.StringUtils;
import com.linkbi.common.utils.file.FileUploadUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * 汇讯数码科技(深圳)有限公司
 * 创建日期:2020/10/23-9:52
 * 版本   开发者     日期
 * 1.0    Danny    2020/10/23
 */

@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ActReDeploymentMapper actReDeploymentMapper;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;

    @Override
    public Page<ProcessDefinitionDTO> selectProcessDefinitionList(ProcessDefinitionDTO processDefinition, PageDomain pageDomain) {
        Page<ProcessDefinitionDTO> list = new Page<>();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().orderByProcessDefinitionVersion().desc();
        if (StringUtils.isNotBlank(processDefinition.getName())) {
            processDefinitionQuery.processDefinitionNameLike("%" + processDefinition.getName() + "%");
        }
        if (StringUtils.isNotBlank(processDefinition.getKey())) {
            processDefinitionQuery.processDefinitionKeyLike("%" + processDefinition.getKey() + "%");
        }
        List<ProcessDefinition> processDefinitions = processDefinitionQuery.listPage((pageDomain.getPageNum() - 1) * pageDomain.getPageSize(), pageDomain.getPageSize());
        long count = processDefinitionQuery.count();
        list.setTotal(count);
        if (count!=0) {
            Set<String> ids = processDefinitions.parallelStream().map(pdl -> pdl.getDeploymentId()).collect(Collectors.toSet());
            List<ActReDeploymentVO> actReDeploymentVOS = actReDeploymentMapper.selectActReDeploymentByIds(ids);
            List<ProcessDefinitionDTO> processDefinitionDTOS = processDefinitions.stream()
                    .map(pd -> new ProcessDefinitionDTO((ProcessDefinitionEntityImpl) pd, actReDeploymentVOS.parallelStream().filter(ard -> pd.getDeploymentId().equals(ard.getId())).findAny().orElse(new ActReDeploymentVO())))
                    .collect(Collectors.toList());
            list.addAll(processDefinitionDTOS);
        }
        return list;
    }

    @Override
    public DefinitionIdDTO getDefinitionsByInstanceId(String instanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        String deploymentId = processInstance.getDeploymentId();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        return new DefinitionIdDTO(processDefinition);
    }

    @Override
    public int deleteProcessDefinitionById(String id) {
        repositoryService.deleteDeployment(id, false);
        return 1;
    }

    @Override
    public void uploadStreamAndDeployment(MultipartFile file) throws IOException {
        // 获取上传的文件名
        String fileName = file.getOriginalFilename();
        // 得到输入流（字节流）对象
        InputStream fileInputStream = file.getInputStream();
        // 文件的扩展名
        String extension = FilenameUtils.getExtension(fileName);

        if (extension.equals("zip")) {
            ZipInputStream zip = new ZipInputStream(fileInputStream);
            repositoryService.createDeployment()//初始化流程
                    .addZipInputStream(zip)
                    .deploy();
        } else {
            repositoryService.createDeployment()//初始化流程
                    .addInputStream(fileName, fileInputStream)

                    .deploy();
        }
    }

    @Override
    public void suspendOrActiveApply(String id, Integer suspendState) {
        if (1==suspendState) {
            // 当流程定义被挂起时，已经发起的该流程定义的流程实例不受影响（如果选择级联挂起则流程实例也会被挂起）。
            // 当流程定义被挂起时，无法发起新的该流程定义的流程实例。
            // 直观变化：act_re_procdef 的 SUSPENSION_STATE_ 为 2
            repositoryService.suspendProcessDefinitionById(id);
        } else if (2==suspendState) {
            repositoryService.activateProcessDefinitionById(id);
        }
    }

    @Override
    public String upload(MultipartFile multipartFile) throws IOException {
       return FileUploadUtils.upload(LinkBIConfig.getUploadPath()+"/processDefinition" , multipartFile);
    }

    @Override
    public void addDeploymentByString(String stringBPMN) {
        repositoryService.createDeployment()
                .addString("CreateWithBPMNJS.bpmn", stringBPMN)
                .deploy();
    }

    @Override
    public void getProcessDefineXML(HttpServletResponse response, String deploymentId, String resourceName) throws IOException {
        InputStream inputStream = repositoryService.getResourceAsStream(deploymentId, resourceName);
        int count = inputStream.available();
        byte[] bytes = new byte[count];
        response.setContentType("text/xml");
        OutputStream outputStream = response.getOutputStream();
        while (inputStream.read(bytes) != -1) {
            outputStream.write(bytes);
        }
        inputStream.close();
    }
}
