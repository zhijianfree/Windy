FROM registry-shdocker-registry.cn-shanghai.cr.aliyuncs.com/shenzhen_ka/windy-dind:1.0

RUN mkdir -p /opt/windy-client

RUN mkdir -p /opt/windy

RUN chmod -R +x /opt/windy

COPY ./*.jar /opt/windy-client/

COPY start.sh /opt/windy-client/

WORKDIR /opt/windy-client

CMD ["sh","start.sh"]

EXPOSE 8070