#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
        sh script: """
        	echo "Checkout Steps : config = ${config}"
        	git clone -b ${config.gitBranch} ${config.gitRepository} .
        	pwd; ls -ltr
        """, label: "Source Code Checkout"
	}
}