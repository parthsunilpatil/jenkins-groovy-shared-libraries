#!/usr/bin/env groovy
import com.example.demo.GlobalVars

def call(Map config) {
	pipeline {
		agent none

		stages {
			
			stage('Build') {

				agent {
					kubernetes {
						yaml GlobalVars.getYaml()
					}
				}

				steps {
					gitCheckout([
            containerName: 'git',
            gitRepository: 'https://github.com/parthsunilpatil/hello-techtonic.git',
            gitBranch: 'master'
          ])
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