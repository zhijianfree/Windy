FROM openjdk:8

RUN mkdir -p /opt/windy

COPY ./*.jar /opt/windy

COPY start.sh /opt/windy

WORKDIR /opt/windy

CMD ["sh","start.sh"]

EXPOSE 9888