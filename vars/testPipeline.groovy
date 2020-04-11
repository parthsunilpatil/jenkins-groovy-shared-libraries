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
			                echo "1.1"
			            }
			        }

			        stage("1.2") {
			            steps {
			                echo "1.2"
			            }
			        }
				}
			}

			stage("2") {

				agent {
					label "slave-1"
				}

				stages {
					stage("2.1") {
			            steps {
			                echo "2.1"
			            }
			        }

			        stage("2.2") {
			            steps {
			                echo "2.2"
			            }
			        }
				}
			}

		}

		post {
			always {
				node("slave-1") {
					deleteDir()
				}
			}
		}
	}
}