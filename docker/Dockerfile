FROM findepi/graalvm:22.3.1-java19-native as builder
RUN gu install native-image
WORKDIR /root/hildr
COPY . .
RUN ./gradlew buildJarForDocker
RUN cd /root/hildr/hildr-node/build/docker && \
native-image -jar hildr-node.jar hildr-node

FROM eclipse-temurin:19-jdk

WORKDIR /usr/local/bin
COPY --from=builder /root/hildr/hildr-node/build/docker/hildr* .

RUN chmod 0755 hildr-node && export PATH=/usr/local/bin:$PATH




