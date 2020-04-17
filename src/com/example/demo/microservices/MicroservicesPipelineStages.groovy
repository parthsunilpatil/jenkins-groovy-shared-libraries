#!/usr/bin/env groovy
package com.example.demo.microservices

class MicroservicesPipelineStages extends PipelineStages {

	MicroservicesPipelineStages(script) {
		super(script)
	}

	def distribute(config) {
		def dockerTag = script.readMavenPom().properties["docker-tag-version"]
		super.distribute(config: [dockerTag: dockerTag])
	}

}