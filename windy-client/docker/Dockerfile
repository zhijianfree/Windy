FROM registry-shdocker-registry.cn-shanghai.cr.aliyuncs.com/shenzhen_ka/docker:dind

# 更新系统包
RUN apt update && apt upgrade -y

# 安装必要的工具
RUN apt install -y wget curl software-properties-common gpg

# 安装 OpenJDK（假设需要 Java 11，可以根据需要调整版本）
RUN apt install -y openjdk-11-jdk

# 安装 Go 1.20
RUN add-apt-repository ppa:longsleep/golang-backports && \
    apt update && \
    apt install -y golang-go

# 设置环境变量
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH=$PATH:/usr/lib/jvm/java-11-openjdk-amd64/bin:/usr/local/go/bin

# 验证安装
RUN java -version && go version

RUN mkdir -p /opt/windy-client

COPY ./*.jar /opt/windy-client/

COPY start.sh /opt/windy-client/

WORKDIR /opt/windy-client

CMD ["sh","start.sh"]

EXPOSE 8070