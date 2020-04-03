#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.YamlPodConfigurationBuilder

def call(Map config) {
	pipeline {
		agent none
		stages {
			stage("Test") {
				steps {
					script {
						echo config.welcomeMessage
						echo new YamlPodConfigurationBuilder().withDefaultBuildStages().build()
						echo new YamlPodConfigurationBuilder().withDefaultDeployStages().build()
					}
				}
			}
		}
	}
}