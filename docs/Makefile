SELF_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

DOCKER_IMAGE := squidfunk/mkdocs-material:7.0.6
ROOT_DIR := $(SELF_DIR)..

# SSH Agent Socket
ifeq ($(OS),Windows_NT)         # Windows
    SSH_AGENT_SOCK := {SSH_AUTH_SOCK}
else
    UNAME_S := $(shell uname -s)
    ifeq ($(UNAME_S),Darwin)    # macOS
        SSH_AGENT_SOCK := /run/host-services/ssh-auth.sock
    else                        # Linux
        SSH_AGENT_SOCK := {SSH_AUTH_SOCK}
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
  ${DOCKER_IMAGE}

serve:
	$(BASE_COMMAND) serve --dev-addr=0.0.0.0:8000

new:
	$(BASE_COMMAND) new $(ROOT_DIR)

build:
	$(BASE_COMMAND) build

deploy:
	$(BASE_COMMAND) gh-deploy --force

help:
	$(BASE_COMMAND) -h

.PHONY: serve new build deploy help
SILENT: