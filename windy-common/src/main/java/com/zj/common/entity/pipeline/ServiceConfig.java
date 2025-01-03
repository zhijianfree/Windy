package com.zj.common.entity.pipeline;

import com.zj.common.adapter.git.GitAccessInfo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;

@Data
public class ServiceConfig {

    /**
     * git访问信息
     */
    private GitAccessInfo gitAccessInfo;

    /**
     * 服务的上下文
     */
    private ServiceContext serviceContext;


    /**
     * 环境变量参数
     */
    private List<ContainerEnv> envParams;

    /**
     * 文件挂载参数
     */
    private List<ContainerVolume> volumes;

    /**
     * 端口映射参数
     */
    private List<ContainerPort> ports;

    /**
     * 节点选择策略
     */
    private NodeStrategy nodeStrategy;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 部署的镜像地址
     */
    private String imageName;

    /**
     * pod副本数
     */
    private Integer replicas;

    /**
     * 更新策略
     */
    private UpdateStrategy strategy;

    @Data
    public static class UpdateStrategy{
        private String type;
    }

    @Data
    public static class NodeStrategy{
        private String type;

        private Object value;
    }

    @Data
    public static class ContainerVolume {

        /**
         * 映射名称-k8s部署时使用，使用英文
         */
        private String name;

        /**
         * 宿主机路径
         */
        private String hostVolume;

        /**
         * 容器路径
         */
        private String volume;

        public boolean notExistEmpty() {
            return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(hostVolume)
                    && StringUtils.isNotBlank(
                    volume);
        }
    }

    @Data
    public static class ContainerEnv {

        /**
         * 环境名称
         */
        private String name;

        /**
         * 环境值
         */
        private String value;

        /**
         * 是否关联引用
         */
        private boolean isRelated;

        public boolean notExistEmpty() {
            return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value);
        }
    }


    @Data
    public static class ContainerPort {

        /**
         * 端口协议
         */
        private String protocol;

        /**
         * 宿主机端口
         */
        private Integer hostPort;

        /**
         * 容器端口
         */
        private Integer port;

        public boolean notExistEmpty() {
            return StringUtils.isNotBlank(protocol) && Objects.nonNull(port) && Objects.nonNull(hostPort);
        }
    }
}
