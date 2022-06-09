FROM 172.21.33.10/library/maven:3-openjdk-8

ADD . /home/sources
COPY maven-settings.xml /usr/share/maven/conf/settings.xml
WORKDIR /home/sources
RUN mvn package
