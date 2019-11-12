#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
		sh script: """
			echo "Docker Build, Deploy & Cleanup : config = ${config}"
			docker build -t ${config.registry}/${config.repository}:${config.tag} .
		""", label: "Docker Build - image=${config.registry}/${config.repository}:${config.tag}"
		
		if(!config.buildOnly) {
			withCredentials([[$class:
				'UsernamePasswordMultiBinding',
				credentialsId: "${config.credentialsId}",
				usernameVariable: 'DOCKER_HUB_USER',
				passwordVariable: 'DOCKER_HUB_PASSWORD']]) {
				sh script: """
					docker login ${config.registry} -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}
					docker push ${config.registry}/${config.repository}:${config.tag}
				""", label: "Docker Deploy - image=${config.registry}/${config.repository}:${config.tag}"
			}
		}
	}
}