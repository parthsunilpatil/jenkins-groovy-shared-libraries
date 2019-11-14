#!/usr/bin/env groovy
package com.example.demo
import com.example.demo.BuildStages
import com.example.demo.DeployStages

class PipelineStagesFactory {

	static def getStage(script, config) {
		
		def utility = config.utility ?: ""
		
		switch(config.utility) {
			case "git":
				BuildStages.git(script, config)
			break
			case "maven":
				BuildStages.mvn(script, config)
			break
			case "docker":
				BuildStages.dockerBuildDeploy(script, config)
			break
			case "helm":
				DeployStages.helm(script, config)
			break
			case "test":
				DeployStages.test(script, config)
			break
			case "dockerCleanup":
				DeployStages.dockerCleanup(script, config)
			break
			default:
				defaultStage(script, config)
			break
		}

	}

	static def defaultStage(script, config) {
		script.container(config.containerName) {
			script.sh script: config.sh, label: config.label
		}
	}

}