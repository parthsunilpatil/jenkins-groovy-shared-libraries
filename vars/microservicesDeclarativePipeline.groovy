#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.microservices.CIMicroservicesPipelineStages

def call(Map config) {

	def microservicesPipeline = new CIMicroservicesPipelineStages(this)
	def labelCode = "worker-" + UUID.randomUUID().toString()

	pipeline {

		agent none

		stages {

			stage("Hello") {
				steps {
					echo "Hello, ${config.name}!"
				}
			}

			stage("Build Stages") {
				steps {
					script {
						microservicesPipeline.dynamicAgentBuildStages([
							label: labelCode,
							yaml: new YamlPodConfigurationBuilder().withDefaultBuildStages().addNodeSelector("kubernetes.io/hostname: docker-desktop").build(),
							checkout: [container: "git"],
							build: [container: "maven"],
							distribute: [container: "docker", dockerTag: "default"]
						])
					}
				}
			}

			stage("Bye") {
				steps {
					echo "Bye, ${config.name}!"
				}
			}

		}

	}

}