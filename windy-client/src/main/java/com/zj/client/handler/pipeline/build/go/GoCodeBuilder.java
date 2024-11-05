package com.zj.client.handler.pipeline.build.go;

import com.zj.client.handler.pipeline.build.CodeBuildContext;
import com.zj.client.handler.pipeline.build.IBuildNotifyListener;
import com.zj.client.handler.pipeline.build.ICodeBuilder;
import com.zj.common.enums.CodeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Component
public class GoCodeBuilder implements ICodeBuilder {
    @Override
    public String codeType() {
        return CodeType.GO.getType();
    }

    @Override
    public Integer build(CodeBuildContext codeBuildContext, IBuildNotifyListener notifyListener) {
        String targetPath = codeBuildContext.getTargetDir() + File.separator + "go_build.sh";
        copyBuildFile(targetPath);
        ProcessBuilder processBuilder = new ProcessBuilder(targetPath, codeBuildContext.getServiceName(), "1.0.0",
                codeBuildContext.getBuildFile());
        processBuilder.redirectErrorStream(true); // 合并标准错误流和标准输出流
        try {
            // 启动进程
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 实时读取输出
            String line;
            while ((line = reader.readLine()) != null) {
                notifyListener.notifyMessage(line);
            }

            int exitCode = process.waitFor();
            log.info("go build file execute result = {}", exitCode);
            return exitCode;
        } catch (IOException | InterruptedException e) {
            log.info("execute shell error", e);
        }
        return -1;
    }

    private static void copyBuildFile(String targetPath) {
        try {
            Resource resource = new ClassPathResource("build/go_build.sh");
            File targetFile = new File(targetPath);
            // 确保目标目录存在
            targetFile.getParentFile().mkdirs();
            FileUtils.copyInputStreamToFile(resource.getInputStream(), targetFile);
        } catch (IOException e) {
            log.info("can not copy go build file", e);
        }
    }
}
