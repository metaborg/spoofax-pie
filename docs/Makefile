DIR := $(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))

ROOT         ?= $(DIR)/..
DOCKERFILE   ?= $(ROOT)/docs/Dockerfile
PORT         ?= 8000

include $(ROOT)/docs/Makefile.inc