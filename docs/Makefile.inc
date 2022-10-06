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

# Builds the Docker image and outputs the hash of the built image
DOCKER_BUILD_CMD = docker build --quiet --file $(DOCKERFILE) $(ROOT) | head -n 1
# Runs Docker with the specified image,
#   mounting the docs directory
#   mounting the known_hosts (for GitHub authentication)
#   mounting the SSH agent socket (for GitHub authentication)
DOCKER_BASE_CMD = docker run --rm -it -p $(PORT):$(PORT) \
  -v $(ROOT):/docs \
  -v ~/.ssh/known_hosts:/root/.ssh/known_hosts \
  -v ${SSH_AGENT_SOCK}:/ssh-agent --env SSH_AUTH_SOCK=/ssh-agent \
  $(shell $(DOCKER_BUILD_CMD))

# Serve the docs at <http://localhost:8000/> (default)
serve: $(DOCKERFILE)
	$(DOCKER_BASE_CMD) serve --dev-addr=0.0.0.0:$(PORT)

# Builds the docs
build: $(DOCKERFILE)
	$(DOCKER_BASE_CMD) build

# Deploys the docs
gh-deploy: $(DOCKERFILE)
	$(DOCKER_BASE_CMD) gh-deploy --force

# Print version
version: $(DOCKERFILE)
	$(DOCKER_BASE_CMD) --version

# Print help
help: $(DOCKERFILE)
	$(DOCKER_BASE_CMD) --help

# Builds the image and prints its hash
build-image: $(DOCKERFILE)
	echo $(shell $(DOCKER_BUILD_CMD))

.PHONY: serve new build deploy help build-image
.SILENT:
