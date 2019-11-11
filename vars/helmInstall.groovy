#!/usr/env/bin groovy

def call(Map config) {
	container(config.containerName) {
		sh script: """
			echo "Helm Install : config = ${config}"
			helm init --client-only
		""", label: "Initialize Helm Client"
		
		if(config.chartsRepositoryName != "stable") {
			sh script: """
				helm repo add ${config.chartsRepositoryName} ${config.chartsRepositoryUrl}
				helm repo list
			""", label: "Add Helm Repository"
		}

		def helmCmd = "helm upgrade --install ${config.name} --namespace ${config.namespace}"

		if(config.containsKey("values")) {
			sh script: """
				echo "Getting src from ${config.values.gitRepository} which contains values file"
				git clone -b ${config.values.gitBranch} ${config.values.gitRepository} .
				pwd; ls -ltr
			""", label: "Checkout Source for Values Files"
			helmCmd += " -f ${config.values.file}"
		} else if(config.containsKey("overrides")) {
			config.overrides.each {
				helmCmd += " --set ${it}"
			}
		}

		helmCmd += " ${config.chartsRepositoryName}/${config.chartName}"
		print "Helm command = ${helmCmd}"
		sh script: helmCmd, label: "Install Helm Chart"
		
	}
}