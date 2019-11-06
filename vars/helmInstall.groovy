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

		if(config.containsKey("values")) {
			print "Getting src from ${config.values.gitRepository} which contains values file"
			sh "git clone -b ${config.values.gitBranch} ${config.values.gitRepository} ."
			sh "pwd; ls -ltr"
			sh "helm upgrade --install ${config.name} --namespace ${config.namespace} -f ${config.values.file} ${config.chartsRepositoryName}/${config.chartName}"
		} else if(config.containsKey("image")) {
			sh "helm upgrade --install ${config.name} --namespace ${config.namespace} --set image.repository=${config.image.registry}/${config.image.repository},image.tag=${config.image.tag} ${config.chartsRepositoryName}/${config.chartName}"	
		} else {
			sh "helm upgrade --install ${config.name} --namespace ${config.namespace} ${config.chartsRepositoryName}/${config.chartName}"
		}
		
	}
}