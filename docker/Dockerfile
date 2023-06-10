FROM findepi/graalvm:22.3.1-java19-native as builder
RUN gu install native-image
WORKDIR /root/hildr
COPY . .
RUN ./gradlew buildJarForDocker
RUN cd /root/hildr/hildr-node/build/docker && \
native-image -jar hildr-node.jar --no-fallback -H:+StaticExecutableWithDynamicLibC --initialize-at-build-time=java.io.FileDescriptor,org.slf4j.MDC,io.opentelemetry.context,io.opentelemetry.api,io.opentelemetry.context,ch.qos.logback,io.netty.channel.AbstractChannel,org.slf4j.LoggerFactory --initialize-at-run-time=io.netty.handler.codec.compression.ZstdOptions,io.netty.handler.ssl.BouncyCastleAlpnSslUtils --no-fallback --enable-preview -J--add-modules=ALL-SYSTEM --report-unsupported-elements-at-runtime hildr-node

FROM eclipse-temurin:19-jdk

WORKDIR /usr/local/bin
COPY --from=builder /root/hildr/hildr-node/build/docker/hildr* .

RUN chmod 0755 hildr-node && export PATH=/usr/local/bin:$PATH




