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
            if(config.containsKey('extraStages')) {
              config.extraStages.each {
                stage("Promotion: ${it}") {
                  timeout(time: 1, unit: 'DAYS') {
                    input message: "Promote to ${it}?", 
                    ok: 'Promote', 
                    parameters: [string(defaultValue: '', description: 'Approver Comments', name: 'COMMENT', trim: false)], 
                    submitter: 'admin'
                  }
                }
                podTemplate(label: 'extraStages', yaml: GlobalVars.getYaml('DEPLOY')) {
                  node('extraStages') {
                    stage("${it}") {
                      echo "${it}"
                    }
                  }
                }
              }
            }
          }
        }
      }
			
			stage('Build Stages') {

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

            stage('Deploy: Dev') {
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

            stage('Test: Dev') {
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

            stage('Docker Clean-up') {
              container('docker') {
                sh script: """
                  echo "Cleaning up dangling images"
                  if ! docker rmi --force \$(docker images -f \"dangling=true\" -q); then
                    echo "Clean Up of dangling not in use docker images completed"
                  fi
                """, label: "Docker Clean-up"
              }
            }

          }

				}

			}

			stage('Deploy Stages') {

        steps {

          script {

            if(config.containsKey("deployments")) {

              config.deployments.each {

                stage("Promotion: ${it}") {
                  timeout(time: 1, unit: 'DAYS') {
                    input message: "Promote to ${it}?", 
                    ok: 'Promote', 
                    parameters: [string(defaultValue: '', description: 'Approver Comments', name: 'COMMENT', trim: false)], 
                    submitter: 'admin'
                  }
                }

                podTemplate(label: "deploy-${it}", yaml: GlobalVars.getYaml('DEPLOY')) {
                  node("deploy-${it}") {

                    stage("Deploy: ${it}") {
                      helmInstall([
                        containerName: 'helm',
                        name: "${PROJECT_NAME}-${it}",
                        namespace: "kube-${it}",
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

                    stage("Test: ${it}") {
                      runCurl([
                        containerName: 'kubectl',
                        namespace: "kube-${it}",
                        waitFor: [
                          [labels: ['app.kubernetes.io/instance=' + PROJECT_NAME + '-dev', 'app.kubernetes.io/name=' + HELM_CHART_NAME]]
                        ],
                        curl: [
                          [url: "http://" + PROJECT_NAME + "-${it}-" + HELM_CHART_NAME + ".kube-${it}/status"]
                        ]
                      ])
                    }

                    stage('Docker Clean-up') {
                      container('docker') {
                        sh script: """
                          echo "Cleaning up dangling images"
                          if ! docker rmi --force \$(docker images -f \"dangling=true\" -q); then
                            echo "Clean Up of dangling not in use docker images completed"
                          fi
                        """, label: "Docker Clean-up"
                      }
                    }

                  }

                }

              }

            }

          }

        }

			}

		}
	}
} 