FROM ubuntu:latest as builder
RUN apt-get update && apt-get install -y wget curl tar build-essential libz-dev zlib1g-dev musl-tools
ENV JAVA_PKG=https://download.oracle.com/graalvm/21/latest/graalvm-jdk-21_linux-x64_bin.tar.gz \
JAVA_HOME=/usr/java/jdk-21
#RUN curl --output /tmp/jdk.tgz $JAVA_PKG
RUN wget -c $JAVA_PKG -O /tmp/jdk.tar.gz
RUN mkdir -p $JAVA_HOME
RUN tar -zxvf /tmp/jdk.tar.gz --directory $JAVA_HOME --strip-components 1
#RUN /usr/java/jdk-20/bin/gu install native-image
ENV PATH=$JAVA_HOME/bin:$PATH
RUN java -version
RUN native-image --version
WORKDIR /root/hildr
COPY . .
#RUN ./gradlew -Pagent run --args="--network optimism-goerli --jwt-secret bf549f5188556ce0951048ef467ec93067bc4ea21acebe46ef675cd4e8e015ff --l1-rpc-url https://eth-goerli.g.alchemy.com/v2/4fUcO9-bSvCtGYwwibuWbz7ktjWwuoN3 --l2-rpc-url http://192.168.3.230:8545 --l2-engine-url http://192.168.3.230:8551 --rpc-port 9545 --sync-mode full"
#RUN ./gradlew metadataCopy --task run --dir src/main/resources/META-INF/native-image
#RUN ./gradlew buildJarForDocker
RUN #./gradlew :hildr-node:clean :hildr-node:nativeCompile -Pmusl
RUN ./gradlew :hildr-node:clean :hildr-node:buildBinary
#RUN cd /root/hildr/hildr-node/build/docker && \
#native-image -jar hildr-node.jar --no-fallback --enable-http --enable-https --enable-preview --add-modules jdk.incubator.concurrent -H:EnableURLProtocols=http,https  --initialize-at-build-time=org.slf4j.LoggerFactory,ch.qos.logback.core.CoreConstants,ch.qos.logback.core.util.Loader,ch.qos.logback.core.util.StatusPrinter,ch.qos.logback.core.status.InfoStatus,ch.qos.logback.classic.Logger,ch.qos.logback.core.rolling.helper.FileNamePattern,ch.qos.logback.classic.Level,ch.qos.logback.core.status.StatusBase,io.opentelemetry.api.trace.ArrayBasedTraceStateBuilder,io.opentelemetry.context.LazyStorage,ch.qos.logback.core.util.FileSize,ch.qos.logback.core.rolling.helper.RollingCalendar,io.opentelemetry.api.internal.ImmutableSpanContext,io.opentelemetry.api.internal.OtelEncodingUtils,ch.qos.logback.classic.PatternLayout,io.opentelemetry.context.ThreadLocalContextStorage,io.opentelemetry.api.trace.PropagatedSpan,io.opentelemetry.context.ContextStorageWrappers,ch.qos.logback.core.rolling.helper.Compressor$1,io.opentelemetry.api.trace.ImmutableTraceFlags,ch.qos.logback.core.rolling.helper.RollingCalendar$1,ch.qos.logback.classic.model.ConfigurationModel,ch.qos.logback.core.model.processor.DefaultProcessor$1,ch.qos.logback.core.model.processor.ImplicitModelHandler$1,ch.qos.logback.core.subst.Token,ch.qos.logback.core.pattern.parser.Parser,ch.qos.logback.core.subst.Parser$1,ch.qos.logback.core.util.Duration,ch.qos.logback.core.model.processor.ChainedModelFilter$1,ch.qos.logback.classic.model.processor.ConfigurationModelHandler,ch.qos.logback.classic.model.processor.LogbackClassicDefaultNestedComponentRules,ch.qos.logback.core.subst.NodeToStringTransformer$1,ch.qos.logback.core.pattern.parser.TokenStream$1,ch.qos.logback.core.subst.Tokenizer$1 --initialize-at-run-time=io.netty.channel.AbstractChannel,io.netty.channel.socket.nio.SelectorProviderUtil,io.netty.util.concurrent.DefaultPromise,io.netty,org.slf4j.MDC,org.github.gestalt.config hildr-node

FROM ubuntu:latest
RUN apt-get update

WORKDIR /usr/local/bin
COPY --from=builder /root/hildr/hildr-node/build/binary/hildr* .
#COPY --from=builder /root/hildr/hildr-node/build/native/nativeCompile/hildr* .

RUN chmod 0755 hildr-node && export PATH=/usr/local/bin:$PATH




