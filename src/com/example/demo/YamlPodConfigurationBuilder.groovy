#!/usr/bin/env groovy
package com.example.demo
import com.example.demo.PodTemplateYamls

@Grab(group = 'org.yaml', module='snakeyaml', version = "1.18")
import org.yaml.snakeyaml.Yaml

class YamlPodConfigurationBuilder {

	def podTemplate = [:]

	YamlPodConfigurationBuilder(String yamlStr = PodTemplateYamls.PODTEMPLATE_BASE_YAML) {
		this.podTemplate = new Yaml().load(yamlStr)
	}

	@NonCPS
	def initMap(Map parent, String key) {
		if(!parent.containsKey(key)) {
			parent.put(key, [:])
		}
	}

	@NonCPS
	def initArray(Map parent, String key) {
		if(!parent.containsKey(key)) {
			parent.put(key, [])
		}
	}

	@NonCPS
	def addContainer(String yamlStr = "") {
		if(yamlStr != "") {
			def container = new Yaml().load(yamlStr)
			initMap(podTemplate, "spec")
			initArray(podTemplate.spec, "containers")
			podTemplate.spec.containers.add(container)
		}
		return this
	}

	@NonCPS
	def addContainer(Map container = [:]) {
		if(!container.isEmpty()) {
			initMap(podTemplate, "spec")
			initArray(podTemplate.spec, "containers")
			podTemplate.spec.containers.add(container)
		}
		return this
	}

	@NonCPS
	def addContainers(String yamlStr = "") {
		if(yamlStr != "") {
			def containers = new Yaml().load(yamlStr)
			initMap(podTemplate, "spec")
			initArray(podTemplate.spec, "containers")
			containers.each { container ->
				podTemplate.spec.containers.add(container)
			}
		}
		return this
	}

	@NonCPS
	def addContainers(List containers = []) {
		containers.each { container -> 
			addContainer(container)
		}
		return this
	}

	@NonCPS
	def addVolume(String yamlStr = "") {
		if(yamlStr != "") {
			def volume = new Yaml().load(yamlStr)
			initMap(podTemplate, "spec")
			initArray(podTemplate, "volumes")
			podTemplate.spec.volumes.add(volume)
		}
		return this
	}

	@NonCPS
	def addVolume(Map volume = [:]) {
		if(!volume.isEmpty()) {
			initMap(podTemplate, "spec")
			initArray(podTemplate, "volumes")
			podTemplate.spec.volumes.add(volume)
		}
		return this
	}

	@NonCPS
	def addVolumes(String yamlStr = "") {
		if(yamlStr != "") {
			def volumes = new Yaml().load(yamlStr)
			initMap(podTemplate, "spec")
			initArray(podTemplate, "volumes")
			volumes.each { volume ->
				podTemplate.spec.volumes.add(volume)
			}
		}
		return this
	}

	@NonCPS
	def addVolumes(List volumes = []) {
		volumes.each { volume ->
			addVolume(volume)
		}
		return this
	}

	@NonCPS
	def removeContainers(List names = []) {
		names.each { name ->
			podTemplate.spec.containers.removeIf { container ->
				container.name == name
			}
		}
		return this
	}

	@NonCPS
	def removeVolumes(List names = []) {
		names.each { name -> 
			podTemplate.spec.volumes.removeIf { volume ->
				volume.name == name
			}
		}
		return this
	}

	@NonCPS
	def setSecretNameForVolume(String volumeName = "", String secretName = "") {
		def volume = [:]
		podTemplate.spec.volumes.find { volumeItr ->
			if(volumeItr.name == volumeName) {
				volume = volumeItr
				return true
			}
			return false
		}
		volume.secret.secretName = secretName
		return this.removeVolumes([volumeName]).addVolume(volume)
	}

	@NonCPS
	def defaultBuildStages() {
		return this
				.addContainers([
					PodTemplateYamls.PODTEMPLATE_CONTAINER_DOCKER,
					PodTemplateYamls.PODTEMPLATE_CONTAINER_GIT,
					PodTemplateYamls.PODTEMPLATE_CONTAINER_MAVEN
				])
				.addVolumes([
					PodTemplateYamls.PODTEMPLATE_VOLUME_DOCKERSOCK,
					PodTemplateYamls.PODTEMPLATE_VOLUME_MVNM2
				])
	}

	@NonCPS
	def defaultDeployStages() {
		return this
				.addContainers([
					PodTemplateYamls.PODTEMPLATE_CONTAINER_DOCKER,
					PodTemplateYamls.PODTEMPLATE_CONTAINER_HELM
				])
				.addVolumes([
					PodTemplateYamls.PODTEMPLATE_VOLUME_DOCKERSOCK,
					PodTemplateYamls.PODTEMPLATE_VOLUME_KUBECONFIG
				])
	}

	@NonCPS
	def build() {
		return new Yaml().dump(podTemplate)
	}

}