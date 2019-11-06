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

		def helmCmd = "helm upgrade --install ${config.name} --namespace ${config.namespace}"

		if(config.containsKey("values")) {
			print "Getting src from ${config.values.gitRepository} which contains values file"
			sh "git clone -b ${config.values.gitBranch} ${config.values.gitRepository} ."
			sh "pwd; ls -ltr"
			helmCmd += " -f ${config.values.file}"
		} else if(config.containsKey("overrides")) {
			config.overrides.each {
				helmCmd += " --set ${it}"
			}
		}

		helmCmd += " ${config.chartsRepositoryName}/${config.chartName}"
		print "Helm command = ${helmCmd}"
		sh helmCmd
		
	}
}