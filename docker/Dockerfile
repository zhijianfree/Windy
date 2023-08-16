FROM openjdk:8

RUN mkdir -p /opt/docker

COPY ./devops-starter-1.0-SNAPSHOT.jar /opt/docker

COPY start.sh /opt/docker

WORKDIR /opt/docker

CMD ["sh","start.sh"]

EXPOSE 9768