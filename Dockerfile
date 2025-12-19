FROM maven:3-eclipse-temurin-21 AS mavenbuild

VOLUME /corpusdata
VOLUME /lucene-indices

RUN apt update && apt install -y nano && rm -rf /var/lib/apt/lists/*

COPY local-deps/treetagger/* /build/treetagger/
RUN chmod +x /build/treetagger/install.sh
RUN cd /build/treetagger && ./install.sh

RUN curl https://dl.min.io/client/mc/release/linux-amd64/mc \
  --create-dirs \
  -o /usr/local/bin/mc && \
  chmod +x /usr/local/bin/mc

ENV ZUMULT_CONFIG_PATH /build/config/workflow_configuration.xml

COPY local-deps /build/local-deps

COPY src /build/src
COPY config /build/config
COPY pom.xml /build
COPY scripts /build/scripts
COPY work /work

ARG GITHUB_USER
ARG GITHUB_TOKEN
RUN cd /build && mvn --settings /build/config/maven_settings.xml package

