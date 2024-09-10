FROM openjdk:8

RUN mkdir -p /opt/windy-client

COPY ./*.jar /opt/windy-client/

COPY start.sh /opt/windy-client/

WORKDIR /opt/windy-client

CMD ["sh","start.sh"]

EXPOSE 8070