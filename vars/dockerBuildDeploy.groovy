#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
		print "Docker Build & Delete : config = ${config}"
		if(config.buildOnly) {
			sh "docker build -t ${config.registry}/${config.repository}:${config.tag} ."
		} else {
			withCredentials([[$class:
				'UsernamePasswordMultiBinding',
				credentialsId: "${config.credentialsId}",
				usernameVariable: 'DOCKER_HUB_USER',
				passwordVariable: 'DOCKER_HUB_PASSWORD']]) {
				sh """
					docker login ${config.registry} -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}
					docker push ${config.registry}/${config.repository}:${config.tag}
				"""
			}
		}
	}
}