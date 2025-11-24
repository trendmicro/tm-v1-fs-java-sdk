# Build stage
FROM eclipse-temurin:8-jdk@sha256:3b1808f3745f1bbbb3611a4f774a0a8940af9de318f1d725e49e2ea8fbc176e6 as build_env

ARG BUILD_VERSION
ARG PACK_CMD=package

RUN useradd -m su-amaas

# Dependencies

RUN apt-get update && apt-get install -y --no-install-recommends \
	maven \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/*

USER su-amaas

# WORKDIR always create folders as root user. need to create the folder first
RUN mkdir -p /home/su-amaas/amaas-java-module

WORKDIR /home/su-amaas/amaas-java-module

# Copy source code
COPY --chown=su-amaas:su-amaas pom.xml .

COPY --chown=su-amaas:su-amaas src/ src/

COPY --chown=su-amaas:su-amaas protos protos/

# Build java-sdk

RUN mvn clean ${PACK_CMD} -X
