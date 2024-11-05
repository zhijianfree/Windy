FROM registry-shdocker-registry.cn-shanghai.cr.aliyuncs.com/shenzhen_ka/docker:dind

FROM registry-shdocker-registry.cn-shanghai.cr.aliyuncs.com/shenzhen_ka/openjdk:11

FROM registry-shdocker-registry.cn-shanghai.cr.aliyuncs.com/shenzhen_ka/golang:1.20.14-alpine

RUN mkdir -p /opt/windy-client

COPY ./*.jar /opt/windy-client/

COPY start.sh /opt/windy-client/

WORKDIR /opt/windy-client

CMD ["sh","start.sh"]

EXPOSE 8070