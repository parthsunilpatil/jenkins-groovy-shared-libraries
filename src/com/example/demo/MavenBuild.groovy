#!/usr/bin/env groovy
package com.example.demo

class MavenBuild implements Serializable {

	def steps

	MavenBuild(steps) {
		this.steps = steps
	}

	def build(String containerName = "maven") {
		steps.container(containerName) {
			steps.sh script: """
				mvn clean package
				pwd; ls -ltr
			""", label: "Maven Build"
		}
	}

}