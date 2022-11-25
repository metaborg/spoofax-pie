DIR := $(patsubst %/,%,$(dir $(abspath $(lastword ${MAKEFILE_LIST}))))

# The directory where mkdocs.yml lives
ROOT         ?= ${DIR}/..
# The path to the Dockerfile
DOCKERFILE   ?= ${ROOT}/docs/Dockerfile
# The port on which to serve the documentation on localhost
PORT         ?= 8000
# The path to the mkdocs_requirements.txt file, relative to ${ROOT}
REQUIREMENTS ?= mkdocs_requirements.txt
# The path where Makefile.inc lives
include ${ROOT}/docs/Makefile.inc
