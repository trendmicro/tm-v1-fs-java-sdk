TM_AM_LOG_LEVEL ?= debug

IMAGE_NAME := amaas/amaas-grpc-java-client:latest
AMAAS_SDK_NAME := file-security-java-sdk

VERSION_LOCATION := './VERSION'
VERSION := $(shell cat $(VERSION_LOCATION))
AMAAS_JAVA_MODULE_VERSION_ID ?= $(VERSION)

# BSD sed does not support --version argument, use this to check if the sed is GNU or BSD one
ifeq ($(shell sed --version >/dev/null 2>&1; echo $$?),0)
SED := sed -i
else
SED := sed -i ''
endif

all: build

build:
	$(SED) 's/__PACKAGE_VERSION__/$(AMAAS_JAVA_MODULE_VERSION_ID)/' pom.xml
	docker build \
		-t $(IMAGE_NAME) \
		--build-arg PACK_CMD=package \
		--build-arg BUILD_VERSION=$(AMAAS_JAVA_MODULE_VERSION_ID) \
		.
	$(SED) 's/$(AMAAS_JAVA_MODULE_VERSION_ID)/__PACKAGE_VERSION__/' pom.xml
	mkdir -p output
	docker run --rm $(IMAGE_NAME) tar -cz target/$(AMAAS_SDK_NAME)-$(AMAAS_JAVA_MODULE_VERSION_ID).jar | tar xzf - -C output

test:
	docker build \
		-t $(IMAGE_NAME) \
		--build-arg PACK_CMD=test \
		--build-arg BUILD_VERSION=$(AMAAS_JAVA_MODULE_VERSION_ID) \
		.

clean:
	rm -rf output target

.PHONY: all build test clean
