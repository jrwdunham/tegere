.DEFAULT_GOAL := help
.PHONY: test

THIS_FILE := $(lastword $(MAKEFILE_LIST))

test:  ## Run the tests
	@clojure -A:test

help:  ## Print this help message.
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
