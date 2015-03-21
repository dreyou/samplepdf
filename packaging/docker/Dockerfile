FROM java:7
EXPOSE 80
RUN apt-get update && apt-get install -y locales-all && apt-get install -y wget
WORKDIR /opt/app
RUN wget http://wiki.dreyou.org/pub/misc/spring-boot/samplepdf-VERSION.jar -O samplepdf.jar
ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
CMD ["java","-Dlogging.level.org.dreyou=INFO","-Dserver.port=80","-jar","samplepdf.jar"]
