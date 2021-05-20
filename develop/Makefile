SELF_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

MKDOCS_VERSION := 7.1.4
ROOT_DIR := $(SELF_DIR)..
CUSTOM_DOCKER_IMAGE := metaborg/mkdocs-material

# SSH Agent Socket
ifeq ($(OS),Windows_NT)         # WSL2 (Windows)
    SSH_AGENT_SOCK := ${SSH_AUTH_SOCK}
else
    UNAME_S := $(shell uname -s)
    ifeq ($(UNAME_S),Darwin)    # macOS
        SSH_AGENT_SOCK := /run/host-services/ssh-auth.sock
    else                        # Linux
        SSH_AGENT_SOCK := ${SSH_AUTH_SOCK}
    endif
endif

# Runs Docker with the specified image,
# mounting the docs directory
# mounting the known_hosts (for GitHub authentication)
# mounting the SSH agent socket (for GitHub authentication)
BASE_COMMAND := docker run --rm -it -p 8000:8000 \
  -v ${ROOT_DIR}:/docs \
  -v ~/.ssh/known_hosts:/root/.ssh/known_hosts \
  -v ${SSH_AGENT_SOCK}:/ssh-agent --env SSH_AUTH_SOCK=/ssh-agent \
  ${CUSTOM_DOCKER_IMAGE}

serve: build-image
	$(BASE_COMMAND) serve --dev-addr=0.0.0.0:8000

new: build-image
	$(BASE_COMMAND) new $(ROOT_DIR)

build: build-image
	$(BASE_COMMAND) build

deploy: build-image
	$(BASE_COMMAND) gh-deploy --force

help: build-image
	$(BASE_COMMAND) -h

build-image: Dockerfile
	docker build --build-arg "VERSION=${MKDOCS_VERSION}" -t ${CUSTOM_DOCKER_IMAGE} -f ${ROOT_DIR}/docs/Dockerfile ${ROOT_DIR}

.PHONY: serve new build deploy help build-image
SILENT:
