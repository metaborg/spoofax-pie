SELF_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

DOCKER_IMAGE := squidfunk/mkdocs-material:7.0.6
ROOT_DIR := $(SELF_DIR)..
BASE_COMMAND := docker run --rm -it -p 8000:8000 -v ${ROOT_DIR}:/docs ${DOCKER_IMAGE}

serve:
	$(BASE_COMMAND) serve --dev-addr=0.0.0.0:8000

new:
	$(BASE_COMMAND) new $(ROOT_DIR)

build:
	$(BASE_COMMAND) build

help:
	$(BASE_COMMAND) -h

.PHONY: serve new build help
SILENT: