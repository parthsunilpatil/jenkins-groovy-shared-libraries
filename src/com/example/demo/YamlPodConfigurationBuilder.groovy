#!/usr/bin/env groovy
package com.example.demo

@Grab(group = 'org.yaml', module='snakeyaml', version = "1.18")
import org.yaml.snakeyaml.Yaml

class YamlPodConfigurationBuilder {
	
	def podTemplate = [:]

	YamlPodConfigurationBuilder(String yamlStr = GlobalVars.PODTEMPLATE_YAML) {
		this.podTemplate =  new Yaml().load(yamlStr)
	}

    @NonCPS
    def initMap(Map parent, String key) {
        if(!parent.containsKey(key)) {
            parent.put(key, [:])
        }
    }

	@NonCPS
	def addAnnotations(String yamlStr = "") {
		if(yamlStr != "") {
            def annotations = new Yaml().load(yamlStr)
			annotations.each {
                initMap(podTemplate, "metadata")
                initMap(podTemplate.metadata, "annotations")
				podTemplate.metadata.annotations.put(it.key, it.value)
			}
		}
		return this
	}

	@NonCPS
	def addLabels(String yamlStr = "") {
        if(yamlStr != "") {
            def labels = new Yaml().load(yamlStr)
            labels.each {
                initMap(podTemplate, "metadata")
                initMap(podTemplate.metadata, "labels")
                podTemplate.metadata.labels.put(it.key, it.value)
            }
        }
        return this
	}
	
	@NonCPS
	def addSpec(String yamlStr = "") {
		if(yamlStr != "") {
			def spec = new Yaml().load(yamlStr)
            spec.each {
                initMap(podTemplate, "spec")
                podTemplate.spec.put(it.key, it.value)
            }
		}
		return this
	}

	@NonCPS
	def addContainers(String yamlStr = "") {
        if(yamlStr != "") {
            def containers = new Yaml().load(yamlStr)
            containers.each {
                initMap(podTemplate, "spec")
                initMap(podTemplate.spec, "containers")
                podTemplate.spec.containers.put(it.key, it.value)
            }
        }
        return this
	}

    @NonCPS
    def removeContainers(List names = []) {
        names.each { name ->
            def containers = [] << podTemplate.spec.containers
            containers.removeIf { containerName ->
                containerName == name
            }
            podTemplate.spec.containers = containers
        }
        return this
    }

    @NonCPS
    def addVolumes(String yamlStr = "") {
        if(yamlStr != "") {
            def volumes = new Yaml().load(yamlStr)
            volumes.each {
               initMap(podTemplate, "spec")
               initMap(podTemplate.spec, "volumes")
               podTemplate.spec.volumes.put(it.key, it.value) 
            }
        }
        return this
    }

    @NonCPS
    def removeVolumes(List names = []) {
        names.each { name ->
            def volumes = [] << podTemplate.spec.volumes
            volumes.removeIf { containerName ->
                containerName == name
            }
            podTemplate.spec.volumes = volumes
        }
        return this
    }

    @NonCPS
    def build() {
        return new Yaml().dump(podTemplate)
    }
	
}