#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.PipelineWrappers
import com.example.demo.YamlPodConfigurationBuilder

def call(Map config) {
	stage("1") {
		node("slave-1") {
			stage("1.1") {
				echo "1.1"
			}
			stage("1.2") {
				echo "1.2"
			}
		}
	}

	config.iterations.each {
		stage("${it}") {
			node("slave-1") {
				stage("${it}.1") {
					echo "${it}.1"
				}
				stage("${it}.2") {
					echo "${it}.2"
				}
			}
		}
	}
}