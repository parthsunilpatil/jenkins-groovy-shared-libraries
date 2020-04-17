#!/usr/bin/env groovy
package com.example.demo.microservices

class MicroservicesPipelineStages implements PipelineStages {

	def distribute(config) {
		def dockerTag = script.readMavenPom().properties["docker-tag-version"]
		PipelineStages.super.distribute(config: [dockerTag: dockerTag])
	}

}