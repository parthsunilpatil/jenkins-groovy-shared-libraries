#!/usr/bin/env groovy
package com.example.demo

@Grab(group = 'org.yaml', module='snakeyaml', version = "1.18")
import org.yaml.snakeyaml.Yaml

class YamlPodConfigurationBuilder implements Serializable {
	
	def podTemplate = [:]
	def yaml = new Yaml()

	PodConfigurationBuilder(String yamlStr = GlobalVars.PODTEMPLATE_YAML) {
		this.podTemplate =  yaml.load(yamlStr)
	}
	
	@NonCPS
	def addSpec(String yamlStr = "") {
		if(name != "" && yamlStr != "") {
			def spec = yaml.load(yamlStr)
			podTemplate.spec.put(spec.name, spec)
		}
		return this
	}
	
}