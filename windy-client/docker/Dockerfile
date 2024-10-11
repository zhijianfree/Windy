FROM registry-shdocker-registry.cn-shanghai.cr.aliyuncs.com/shenzhen_ka/openjdk:11

RUN mkdir -p /opt/windy-client

COPY ./*.jar /opt/windy-client/

COPY start.sh /opt/windy-client/

WORKDIR /opt/windy-client

CMD ["sh","start.sh"]

EXPOSE 8070