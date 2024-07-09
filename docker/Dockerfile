FROM ghcr.io/graalvm/graalvm-community:21 as builder

WORKDIR /root/hildr
COPY . .
RUN ./gradlew clean buildJarForDocker

FROM ghcr.io/graalvm/graalvm-community:21

WORKDIR /usr/local/bin
COPY --from=builder /root/hildr/hildr-node/build/docker/hildr-node.jar .
COPY --from=builder /root/hildr/docker/start-hildr-node.sh .
ENV HILDR_JAR /usr/local/bin/hildr-node.jar
ENV HILDR_MAIN_CLASS io.optimism.Hildr

ENTRYPOINT ["java", "--enable-preview", "-cp" , "/usr/local/bin/hildr-node.jar", "io.optimism.Hildr"]
