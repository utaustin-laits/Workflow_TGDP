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

ENV ZUMULT_CONFIG_PATH /config/workflow_configuration.xml

COPY local-deps /build/local-deps
RUN mvn install:install-file \
   -Dfile=/build/local-deps/zumult-api.jar \
   -DgroupId=org.zumult \
   -DartifactId=zumultapi \
   -Dversion=0.1.14 \
   -Dpackaging=jar \
   -DgeneratePom=true

COPY src /build/src
COPY config /build/config
COPY pom.xml /build
# RUN cd /build && mvn package

COPY scripts /build/scripts
COPY work /work

