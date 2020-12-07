.DEFAULT_GOAL := help
.PHONY: test

THIS_FILE := $(lastword $(MAKEFILE_LIST))

test:  ## Run the tests
	@clojure -A:test

pack-jar:  ## Build the tegere.jar file using Pack
	@clojure -A:pack \
		mach.pack.alpha.skinny \
		--no-libs \
		--project-path tegere.jar

deploy-clojars:  ## Release to Clojars
	@mvn deploy:deploy-file \
		-Dfile=tegere.jar \
		-DrepositoryId=clojars \
		-Durl=https://clojars.org/repo \
		-DpomFile=pom.xml

help:  ## Print this help message.
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
