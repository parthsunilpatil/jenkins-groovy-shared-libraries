#!/usr/bin/env groovy
package com.example.demo.microservices

class CIMicroservicesPipelineStages extends MicroServicesPipelineStages {

	CIMicroservicesPipelineStages(script) {
		super(script)
	}

	def distribute(config) {
		println "Docker Tag: ${config.dockerTag}"

		def dockerTag = script.readMavenPom().properties["docker-tag-version"]

		println "New Docker Tag: ${config.dockerTag}"

		config.dockerTag = dockerTag
		super.distribute(config)
	}

}