#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.microservices.CIMicroservicesPipelineStages
import com.example.demo.microservices.AIMicroservicesPipelineStages
import com.example.demo.microservices.factory.MicroservicesPipelineStagesFactory

def call(Map config) {

	def microservicesPipeline = MicroservicesPipelineStagesFactory.get(params.CLASS_NAME, this)
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