#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
		print "Maven Build : config = ${config}"
		sh "mvn clean package"
		print "Maven Build Completed"
		sh "pwd; ls -ltr"
	}
}