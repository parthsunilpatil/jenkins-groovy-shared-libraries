#!/usr/env/bin groovy

def call(Map config) {
	container(config.containerName) {
		print "Helm Install : config = ${config}"
		sh "helm init --client-only"
		if(config.chartsRepositoryName != "stable") {
			sh """
				helm repo add ${config.chartsRepositoryName} ${config.chartsRepositoryUrl}
				helm repo list
			"""
		}
		sh "helm upgrade --install ${config.name} --namespace ${config.namespace} --set image.repository=${config.imageRegistry}/${config.imageRepository},image.tag=${config.imageTag} ${config.chartsRepositoryName}/${config.chartName}"
	}
}