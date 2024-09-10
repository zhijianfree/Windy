# syntax=docker/dockerfile:1.2

FROM openjdk:8

RUN mkdir -p /opt/windy-console

COPY ./*.jar /opt/windy-console/

COPY start.sh /opt/windy-console/

WORKDIR /opt/windy-console

CMD ["sh","start.sh"]

EXPOSE 9768