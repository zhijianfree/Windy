package com.zj.client.service;

import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.common.adapter.monitor.collector.InstanceCollector;
import com.zj.common.adapter.monitor.collector.PhysicsCollect;
import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.service.ToolLoadResult;
import com.zj.common.entity.service.ToolVersionDto;
import com.zj.common.enums.CodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
        File localFile = new File(toolVersionDto.getInstallPath());
        ToolLoadResult toolLoadResult = new ToolLoadResult();
        boolean showVersion = showVersion(toolVersionDto.getType(), toolVersionDto.getInstallPath());
        toolLoadResult.setSuccess(localFile.exists() && localFile.isDirectory() && showVersion);
        return toolLoadResult;
    }

    public boolean showVersion(String type, String installPath) {
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
        if (Objects.equals(CodeType.JAVA.getType(), type)) {
            command += "/bin/java -version";
        }
        if (Objects.equals(CodeType.GO.getType(), type)) {
            command += "/bin/go version";
        }
        if (Objects.equals(CodeType.MAVEN.getType(), type)) {
            command += "/bin/mvn -v";
        }
        return command;
    }
}
