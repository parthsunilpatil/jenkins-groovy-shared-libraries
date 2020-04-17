#!/usr/bin/env groovy
package com.example.demo.microservices

class MicroservicesPipelineStages extends PipelineStages {

	MicroservicesPipelineStages(script) {
		super(script)
	}

	def checkout() {
		script.sh script: """
			git clone -b ${script.params.GIT_BRANCH} ${script.params.GIT_REPOSITORY} .
			pwd; ls -ltr
		""", label: "Checkout Source Code"
	}

	def build() {
		script.sh script: """
			mvn clean install
		""", label: "Maven Build"
	}

	def distribute(config) {
		def dockerTag = script.readMavenPom().properties["docker-tag-version"]
		
		script.sh script: """
			docker build -t ${script.params.DOCKER_REGISTRY}/${script.params.DOCKER_REPOSITORY}:${dockerTag} .
		""", label: "Docker Build & Deploy"
	}

}