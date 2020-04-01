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
	def addContainer(containerDefinition) {
		def container = containerDefinition instanceof String ? new Yaml().load(containerDefinition) : containerDefinition
		if(!container.isEmpty()) {
			initMap(podTemplate, "spec")
			initArray(podTemplate.spec, "containers")
			podTemplate.spec.containers.add(container)
		}
		return this
	}

	@NonCPS
	def addContainers(containersDefinition) {
		def containers = containersDefinition instanceof String ? new Yaml().load(containersDefinition) : containersDefinition
		if(!containers.isEmpty()) {
			containers.each { container ->
				addContainer(container)
			}
		}
		return this
	}

	@NonCPS
	def addVolume(volumeDefinition) {
		def volume = volumeDefinition instanceof String ? new Yaml().load(volumeDefinition) : volumeDefinition
		if(!volume.isEmpty()) {
			initMap(podTemplate, "spec")
			initArray(podTemplate, "volumes")
			podTemplate.spec.volumes.add(volume)
		}
		return this
	}

	@NonCPS
	def addVolumes(volumesDefinition) {
		def volumes = volumesDefinition instanceof String ? new Yaml().load(volumesDefinition) : volumesDefinition
		if(!volumes.isEmpty()) {
			initArray(podTemplate, "volumes")
			volumes.each { volume ->
				addVolume(volume)			}
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