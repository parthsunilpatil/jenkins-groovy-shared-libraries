#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
		sh script: """
			echo "Maven Build : config = ${config}"
			mvn clean package
			pwd; ls -ltr
		""", label: "Maven Build"
	}
}