#!/usr/bin/env groovy

def call(Map config) {
	container(config.containerName) {
		print "Test Deployment : config = ${config}"

		if(config.containsKey('waitFor')) {
			config.waitFor.each {
				sh "kubectl -n ${config.namespace} wait --for=condition=Ready pod/\$(kubectl -n ${config.namespace} get pod -l app.kubernetes.io/instance=${it.instance} -l app.kubernetes.io/name=${it.name} -o 'jsonpath={.items[0].metadata.name}')"
			}
		}

		if(config.containsKey('test')) {
			config.test.each {
				if(it.psql) {
					sh "psql -h ${it.host} -p ${it.port} -U admin -d info_db -c '${it.sql}'"
				} else {
					sh "curl ${it.url}"
				}
			}
		}

	}
}