#!/usr/bin/env groovy
package com.example.demo

class PodTemplateYamls {
	
	static final String PODTEMPLATE_BASE_YAML = """
	apiVersion: v1
	kind: Pod
	metadata:
	  annotations:
	  	podTemplateClass: "com.example.demo.GlobalVars"
	  labels:
	  	app: "dynamic-jenkins-agent"
	"""

	static final String PODTEMPLATE_CONTAINER_DOCKER = """
	name: docker
	image: docker:19
	command:
	- cat
	tty: true
	volumeMounts:
	- name: dockersock
	  mountPath: /var/run/docker.sock
	"""

	static final String PODTEMPLATE_CONTAINER_GIT = """
	name: git
	image: alpine/git:1.0.7
	command:
	- cat
	tty: true
	"""

	static final String PODTEMPLATE_CONTAINER_MAVEN = """
	name: maven
	image: maven:3.3.9-jdk-8-alpine
	command:
	- cat
	tty: true
	volumeMounts:
	- name: mvnm2
	  mountPath: /root/.m2/repository
	"""

	static final String PODTEMPLATE_CONTAINER_HELM = """
	name: helm
	image: parthpatil3110/k8s-helm:2.16.1-1.15.5
	command:
	- cat
	tty: true
	volumeMounts:
	- name: kubeconfig
	  mountPath: /root/.kube
	"""

	static final String PODTEMPLATE_VOLUME_DOCKERSOCK = """
	name: dockersock
	hostPath:
	  path: /var/run/docker.sock
	"""

	static final String PODTEMPLATE_VOLUME_MVNM2 = """
	name: mvnm2
	hostPath:
	  path: /c/Users/parthp/volumes/m2
	"""

	static final String PODTEMPLATE_VOLUME_KUBECONFIG = """
	name: kubeconfig
	secret:
	  secretName: kubeconfig-dev
	  items:
	  - key: config
	    path: config
	"""
}