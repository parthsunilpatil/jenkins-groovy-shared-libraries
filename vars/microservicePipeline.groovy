#!/usr/bin/env groovy

def call(Map config) {
	print "Running Pipeline with passed config = ${config}"
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

          stage('Checkout') {
            steps {
              container('git') {
                sh 'git --version'
              }
            }
          }

          stage('Maven Build') {
            steps {
              container('mvn') {
                sh 'mvn --version'
              }
            }
          }

          stage('Docker Build') {
            container('docker') {
              sh 'docker --version'
            }
          }

        }

      }

      stage('Deploy') {

        agent {
          kubernetes {
            yaml GlobalVars.PODTEMPLATE_DEPLOY_YAML
          }
        }

        steps {

          stage('Deploy - DEV') {
            container('helm') {
              sh 'helm version'
            }
          }

          stage('Test - DEV') {
            container('kubectl') {
              sh 'kubectl version'
            }
          }

        }

      }

		}
	}
}	