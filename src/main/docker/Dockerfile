FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD docker-spring-boot-1.0.0.jar spider.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/spider.jar"]