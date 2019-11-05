#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
        print "Checkout Steps : config = ${config}"
        sh "git clone -b ${config.gitBranch} ${config.gitRepository} ."
        print "Repository ${config.gitRepository} cloned:"
        sh "pwd; ls -ltr"
	}
}