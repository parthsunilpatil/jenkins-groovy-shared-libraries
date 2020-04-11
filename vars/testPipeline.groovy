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

			config.iterations.each { iteration ->

				stage("${iteration}") {
					agent {
						label "slave-1"
					}

					stages {
						stage("${iteration}.1") {
				            steps {
				                echo "${iteration}.1"
				            }
				        }

				        stage("${iteration}.2") {
				            steps {
				                echo "${iteration}.2"
				            }
				        }
					}
				}

			}

			post {
				always {
					node("slave-1") {
						cleanWs()
					}
				}
			}

		}
	}
}