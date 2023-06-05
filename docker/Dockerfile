FROM --platform=linux/amd64 debian:bullseye-slim

RUN apt update && \
 apt install --no-install-recommends -q --assume-yes curl=7* libjemalloc-dev=5.* && \
 apt clean

RUN ARCH=$(uname -m) && \
    if [ "$ARCH" = "aarch64" ]; then \
        curl -kL -o jdk-19.tar.gz https://github.com/adoptium/temurin19-binaries/releases/download/jdk-19.0.2%2B7/OpenJDK19U-jre_aarch64_linux_hotspot_19.0.2_7.tar.gz ; \
    elif [ "$(uname -s)" = "Darwin" ] && [ "$(uname -m)" = "arm64" ]; then \
        curl -kL -o jdk-19.tar.gz https://github.com/adoptium/temurin19-binaries/releases/download/jdk-19.0.2%2B7/OpenJDK19U-jre_aarch64_linux_hotspot_19.0.2_7.tar.gz ; \
    elif [ "$ARCH" = "x86_64" ]; then \
        curl -kL -o jdk-19.tar.gz https://github.com/adoptium/temurin19-binaries/releases/download/jdk-19.0.2%2B7/OpenJDK19U-jre_x64_linux_hotspot_19.0.2_7.tar.gz ; \
    else \
        echo "Unsupported platform: $ARCH"; exit 1; \
    fi

RUN tar -xzf jdk-19.tar.gz && \
    rm jdk-19.tar.gz && \
    mv jdk-19.0.2+7-jre /usr/bin/ && \
    update-alternatives --install "/usr/bin/java" "java" "/usr/bin/jdk-19.0.2+7-jre/bin/java" 1

ENV JAVA_HOME /usr/bin/jdk-19.0.2+7-jre
RUN export JAVA_HOME
RUN export PATH=$JAVA_HOME/bin:$PATH

WORKDIR /usr/local/bin
COPY . .
ENV HILDR_JAR /usr/local/bin/hildr-node.jar
ENV HILDR_MAIN_CLASS io.optimism.Hildr




