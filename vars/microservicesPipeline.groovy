#!/usr/bin/env groovy
import com.example.demo.GlobalVars

def call(Map config) {

  def PROJECT_NAME="${config.PROJECT_NAME}"

  def DOCKER_REGISTRY="${config.DOCKER_REGISTRY}"
  def DOCKER_REPOSITORY="${config.DOCKER_REPOSITORY}"
  def DOCKER_TAG="${config.DOCKER_TAG}"
  def GIT_REPOSITORY="${config.GIT_REPOSITORY}"
  def GIT_BRANCH="${config.GIT_BRANCH}"

  def HELM_CHART_NAME="${config.HELM_CHART_NAME}"
  def HELM_CHART_REPOSITORY_NAME="${config.HELM_CHART_REPOSITORY_NAME}"
  def HELM_CHART_REPOSITORY_URL="${config.HELM_CHART_REPOSITORY_URL}"

	pipeline {
		agent none

		stages {

      stage('Extra Stages') {
        steps {
          script {
            def deployYaml = GlobalVars.getYaml('DEPLOY')
            print "deployYaml: ${deployYaml}"
            if(config.containsKey('extraStages')) {
              config.extraStages.each {
                podTemplate(yaml: deployYaml) {
                  node {
                    stage('${it}') {
                      echo "${it}"
                    }
                  }
                }
              }
            }
          }
        }
      }
			
			stage('Build') {

				agent {
					kubernetes {
						yaml GlobalVars.getYaml()
					}
				}

				steps {

          script {
            
            stage('Checkout') {
              gitCheckout([
                containerName: 'git',
                gitRepository: GIT_REPOSITORY,
                gitBranch: GIT_BRANCH
              ])
            }

            stage('Maven Build') {
              mvnBuild([
                containerName: 'maven'
              ])
            }

            stage('Docker Build') {
              dockerBuildDeploy([
                containerName: 'docker',
                buildOnly: true,
                registry: DOCKER_REGISTRY,
                repository: DOCKER_REPOSITORY,
                tag: DOCKER_TAG
              ])
            }

          }

				}

			}

			stage('Deploy: DEV') {

				agent {
					kubernetes {
						yaml GlobalVars.getYaml('DEPLOY')
					}
				}

				steps {
					
          script {

            stage('Deploy') {
              helmInstall([
                containerName: 'helm',
                name: PROJECT_NAME + '-dev',
                namespace: 'kube-dev',
                overrides: [
                  "image.repository=${DOCKER_REGISTRY}/${DOCKER_REPOSITORY}",
                  "image.tag=${DOCKER_TAG}",
                  "service.type=NodePort"
                ],
                chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                chartName: HELM_CHART_NAME
              ])
            }

            stage('Test') {
              runCurl([
                containerName: 'kubectl',
                namespace: 'kube-dev',
                waitFor: [
                  [labels: ['app.kubernetes.io/instance=' + PROJECT_NAME + '-dev', 'app.kubernetes.io/name=' + HELM_CHART_NAME]]
                ],
                curl: [
                  [url: 'http://' + PROJECT_NAME + '-dev-' + HELM_CHART_NAME + '.kube-dev/status']
                ]
              ])
            }

          }
					
				}

			}

      stage('Promotion: DEV-PROD') {
        steps {
          timeout(time: 1, unit: 'DAYS') {
              input message: 'Promote to PROD?', 
              ok: 'Promote', 
              parameters: [string(defaultValue: '', description: 'Approver Comments', name: 'COMMENT', trim: false)], 
              submitter: 'admin'
          }
        }
      }

      stage('Deploy: PROD') {

        agent {
          kubernetes {
            yaml GlobalVars.getYaml('DEPLOY')
          }
        }

        steps {

          script {

            stage('Deploy') {
              helmInstall([
                containerName: 'helm',
                name: PROJECT_NAME + '-prod',
                namespace: 'kube-prod',
                overrides: [
                  "image.repository=${DOCKER_REGISTRY}/${DOCKER_REPOSITORY}",
                  "image.tag=${DOCKER_TAG}",
                  "service.type=LoadBalancer"
                ],
                chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                chartName: HELM_CHART_NAME
              ])
            }

            stage('Test') {
              runCurl([
                containerName: 'kubectl',
                namespace: 'kube-prod',
                waitFor: [
                  [labels: ['app.kubernetes.io/instance=' + PROJECT_NAME + '-prod', 'app.kubernetes.io/name=' + HELM_CHART_NAME]]
                ],
                curl: [
                  [url: 'http://' + PROJECT_NAME + '-prod-' + HELM_CHART_NAME + '.kube-prod/status']
                ]
              ])
            }

          }
          
        }

      }

		}
	}
} 