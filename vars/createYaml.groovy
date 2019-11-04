#!/usr/bin/env groovy

def call() {
	def yaml = new groovy.yaml.YamlBuilder()
	def yamlMap = [
		apiVersion: 'v1',
		kind: 'Pod',
		spec: [
			containers: [
				[
					name: 'maven',
					image: 'maven:alpine',
					command: ['cat'],
					tty: true
				],
				[
					name: 'helm',
					image: 'parthpatil3110/k8s-helm:latest',
					command: ['cat'],
					tty: true,
					volumeMounts: [
						name: 'kubeconfig',
						mountPath: '/root/.kube/config'
					]
				]
			],
			volumes: [
				[
					name: 'kubeconfig',
					hostPath: [
						path: '/c/Users/parthp/.kube/config'
					]
				]
			]
		]
	]

	echo yaml(yamlMap).toString()

}