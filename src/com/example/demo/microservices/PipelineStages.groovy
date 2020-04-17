#!/usr/bin/env groovy
package com.example.demo.microservices

import com.example.demo.PipelineWrappers

class PipelineStages {

	def script

	PipelineStages(script) {
		this.script = script
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
		script.sh script: """
			docker build -t ${script.params.DOCKER_REGISTRY}/${script.params.DOCKER_REPOSITORY}:${config.dockerTag} .
		""", label: "Docker Build & Deploy"
	}

	def dynamicAgentBuildStages(config) {
		script.podTemplate([
			label: config.label,
			yaml: config.yaml
		]) {
			script.node(config.label) {
				script.stage("checkout") {
					script.container(config.checkout.container) {
						checkout()
					}
				}

				script.stage("build") {
					script.container(config.build.container) {
						build()
					}
				}

				script.stage("distribute") {
					script.container(config.distribute.container) {
						distribute(config.distribute.config)
					}
				}
			}
		}
	}

}