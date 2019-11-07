#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
		print "Test Deployment : config = ${config}"

		if(config.containsKey('waitFor')) {
			config.waitFor.each {
				def podSh = "kubectl -n ${config.namespace} get pod"
				if(it.containsKey('labels')) {
					it.labels.each {
						podSh += " -l \'${it}\'"
					}
				}
				podSh += " -o \'jsonpath={.items[0].metadata.name}\'"
				print "Shell to get pod name = ${podSh}"
				def pod = sh returnStdout: true, script: podSh
				sh "kubectl -n ${config.namespace} wait --timeout=3600s --for=condition=Ready pod/${pod}"
			}
		}

		if(config.containsKey('test')) {
			sh "pwd; ls -ltr"
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
				print "curl command = " + curl
				sh curl
			}
		}

	}
}