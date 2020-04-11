#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.PipelineWrappers
import com.example.demo.YamlPodConfigurationBuilder

def call(Map config) {
	pipeline {
		agent none
		
		PipelineWrappers.stages(this, [
			agent: [label: "slave-1"],
			iterations: [
				[iteration: "1", value: "one"],
				[iteration: "2", value: "two"],
				[iteration: "3", value: "three"]
			]
		], { closureParams ->
			stage("${closureParams.iteration}") {
				steps {
					echo "${closureParams.value}"
				}
			}
		})
	}
}