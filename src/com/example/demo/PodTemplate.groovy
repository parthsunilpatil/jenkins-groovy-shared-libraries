#!/usr/bin/env groovy
package com.example.demo

@Grab(group = 'org.yaml', module='snakeyaml', version = "1.18")
import org.yaml.snakeyaml.Yaml

class PodTemplate implements Serializable {
  
  Yaml yaml = new Yaml()
  def podTemplate = [:]

  PodTemplate(String content = GlobalVars.PODTEMPLATE_BUILD_YAML) {
    this.podTemplate = yaml.load(content)
  }

  def getPodTemplate() {
    return podTemplate
  }

  def setPodTemplate(Map podTemplate) {
    this.podTemplate = podTemplate
  }

  def toYamlStr() {
    return yaml.dump(podTemplate)
  }

}
