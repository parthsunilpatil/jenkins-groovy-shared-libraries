#!/usr/bin/env groovy
import com.example.demo.GlobalVars
import com.example.demo.PodTemplate

def podTemplate = new PodTemplate(GlobalVars.getYaml())

def call(Map config) {
	pipeline {
		agent none

		stages {
			
			stage('Build') {

				agent {
					kubernetes {
						yaml podTemplate.getYamlStr()
					}
				}

				steps {
					container('git') {
						sh script: GlobalVars.gitVersion(), label: "Retrieve Git Version"
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
						yaml GlobalVars.getYaml('DEPLOY')
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