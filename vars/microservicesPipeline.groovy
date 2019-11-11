#!/usr/bin/env groovy
import com.example.demo.GlobalVars

def call(Map config) {
	pipeline {
		agent none

		stages {
			
			stage('Build') {

				agent {
					kubernetes {
						yaml GlobalVars.PODTEMPLATE_BUILD_YAML
					}
				}

				steps {
					container('git') {
						sh script: 'git --version', label: "Retrieve Git Version"
					}
					container('maven') {
						sh script: 'mvn --version', label: "Retrieve Maven Version"
					}
					container('docker') {
						sh 'docker --version'
					}
				}

			}

			stage('Promotion') {
				steps {
					echo "Promotion"
				}
			}

			stage('Deploy') {

				agent {
					kubernetes {
						yaml GlobalVars.PODTEMPLATE_DEPLOY_YAML
					}
				}

				steps {
					container('helm') {
						sh 'helm version'
					}
					container('kubectl') {
						sh 'kubectl version'
					}
				}

			}

		}
	}
} 