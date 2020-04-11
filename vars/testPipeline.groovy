#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.PipelineWrappers
import com.example.demo.YamlPodConfigurationBuilder

def call(Map config) {
	pipeline {
		agent none
		
		stages {
			
			stage("1") {

				agent {
					label "slave-1"
				}

				stages {
					stage("1.1") {
			            steps {
			                echo "Hi"
			                sh """
			                    docker images
			                    docker ps
			                """
			            }
			        }
				}
			}
		}
	}
}