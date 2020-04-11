#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.PipelineWrappers
import com.example.demo.YamlPodConfigurationBuilder

def call(Map config) {
	pipeline {
		agent none
		
		stages {
			agent {
				label "slave-1"
			}

			stage("1") {
				echo "1"
			}
		}
	}
}