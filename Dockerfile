# Build stage
FROM openjdk:8-jdk-bullseye@sha256:c25fa22ba2961758802ea63bc4622999a305086405d9d03b065925da00cb8bb6 as build_env

ARG BUILD_VERSION
ARG PACK_CMD=package

RUN useradd -m su-amaas

# Dependencies

RUN apt-get update && apt-get install -y --no-install-recommends \
	maven=3.6.3-5 \
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
