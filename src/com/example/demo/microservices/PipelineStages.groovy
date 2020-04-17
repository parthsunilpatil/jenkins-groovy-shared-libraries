#!/usr/bin/env groovy
package com.example.demo.microservices

import com.example.demo.PipelineWrappers

abstract class PipelineStages {

	def script

	PipelineStages(script) {
		this.script = script
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
						distribute(config.distribute.dockerTag)
					}
				}
			}
		}
	}

}