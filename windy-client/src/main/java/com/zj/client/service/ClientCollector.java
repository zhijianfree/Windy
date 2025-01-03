package com.zj.client.service;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.dto.MavenConfigDto;
import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.common.adapter.monitor.collector.InstanceCollector;
import com.zj.common.adapter.monitor.collector.PhysicsCollect;
import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.service.ToolLoadResult;
import com.zj.common.entity.service.ToolVersionDto;
import com.zj.common.enums.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Slf4j
@Service
public class ClientCollector {

    @Value("${windy.client.version}")
    private String clientVersion;
    private final NodeStatusQueryLooper nodeStatusQueryLooper;

    public ClientCollector(NodeStatusQueryLooper nodeStatusQueryLooper) {
        this.nodeStatusQueryLooper = nodeStatusQueryLooper;
    }

    public ClientCollectDto getInstanceInfo() {
        ClientCollectDto clientCollectDto = new ClientCollectDto();
        PhysicsCollect physics = InstanceCollector.collectPhysics();
        clientCollectDto.setPhysics(physics);
        Integer waitQuerySize = nodeStatusQueryLooper.getWaitQuerySize();
        clientCollectDto.setWaitQuerySize(waitQuerySize);
        clientCollectDto.setVersion(clientVersion);
        return clientCollectDto;
    }

    public ToolLoadResult loadLocalToolVersion(ToolVersionDto toolVersionDto) {
        boolean toolExist = checkToolExist(toolVersionDto.getType(), toolVersionDto.getInstallPath());
        boolean configSetting = true;
        if (toolExist && Objects.equals(toolVersionDto.getType(), ToolType.MAVEN.getType())) {
            MavenConfigDto mavenConfig = new MavenConfigDto();
            mavenConfig.setMavenPath(toolVersionDto.getInstallPath());
            List<MavenConfigDto.RemoteRepository> repositories = JSON.parseArray(toolVersionDto.getBuildConfig(),
                    MavenConfigDto.RemoteRepository.class);
            mavenConfig.setRemoteRepositories(repositories);
            configSetting = MavenSettingHelper.configSetting(mavenConfig);
        }

        File localFile = new File(toolVersionDto.getInstallPath());
        ToolLoadResult toolLoadResult = new ToolLoadResult();
        toolLoadResult.setSuccess(localFile.exists() && localFile.isDirectory() && toolExist && configSetting);
        return toolLoadResult;
    }

    public boolean checkToolExist(String type, String installPath) {
        try {
            String command = exchangeCommand(type, installPath);
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sh", "-c", command);
            Process process = processBuilder.start();

            // 读取标准输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("load shell info: " + line);
                }
            }

            // 读取错误输出
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("load shell error: " + errorLine);
                }
            }

            return process.waitFor() == 0;
        } catch (Exception e) {
            log.error("get build tool ={} version error ", type, e);
        }
        return false;
    }

    private static String exchangeCommand(String type, String installPath) {
        String command = installPath;
        if (Objects.equals(ToolType.JAVA.getType(), type)) {
            command += "/bin/java -version";
        }
        if (Objects.equals(ToolType.GO.getType(), type)) {
            command += "/bin/go version";
        }
        if (Objects.equals(ToolType.MAVEN.getType(), type)) {
            command += "/bin/mvn -v";
        }
        return command;
    }
}
