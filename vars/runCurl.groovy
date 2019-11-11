#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
		if(config.containsKey('waitFor')) {
			config.waitFor.each {
				def podSh = "kubectl -n ${config.namespace} get pod"
				if(it.containsKey('labels')) {
					it.labels.each {
						podSh += " -l \'${it}\'"
					}
				}
				podSh += " -o \'jsonpath={.items[0].metadata.name}\'"
				def pod = sh returnStdout: true, script: podSh, label: "Get Pod Name - labels=${it.labels}"
				sh script: "kubectl -n ${config.namespace} wait --timeout=3600s --for=condition=Ready pod/${pod}", label: "Wait for pod/${pod} to be ready"
			}
		}

		if(config.containsKey('curl')) {
			config.curl.each {
				def curl = "curl -i"
				if(it.containsKey('method')) {
					curl += " -X ${it.method}"
				}
				if(it.containsKey('url')) {
					curl += " --url ${it.url}"
				}
				if(it.containsKey('data')) {
					it.data.each {
						curl += " --data \'${it}\'"
					}
				}
				sh script: curl, label: "Run Curl Command - url=${it.url}"
			}
		}

	}
}