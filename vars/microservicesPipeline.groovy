#!/usr/bin/env groovy
import com.example.demo.GlobalVars
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.BuildStages
import com.example.demo.DeployStages

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

  def podConfigBuilder = new YamlPodConfigurationBuilder()

	pipeline {
		agent none

		stages {
			
			stage('Build Stages') {

				agent {
					kubernetes {
						yaml podConfigBuilder.addAnnotations("""
                podTemplateClass: YamlPodConfigurationBuilder
                podTemplateType: build
              """).addLabels("""
                app: DynamicJenkinsAgent
              """).build()
					}
				}

				steps {

          script {

            BuildStages.stages(this, [
              [
                utility: 'git',
                stageName: 'Git Checkout',
                containerName: 'git',
                gitRepository: GIT_REPOSITORY,
                gitBranch: GIT_BRANCH
              ],
              [
                utility: 'maven',
                stageName: 'Maven Build',
                containerName: 'maven'
              ],
              [
                utility: 'docker',
                stageName: 'Docker Build & Deploy',
                containerName: 'docker',
                buildOnly: true,
                registry: DOCKER_REGISTRY,
                repository: DOCKER_REPOSITORY,
                tag: DOCKER_TAG
              ]
            ])

            DeployStages.stages(this, [
              [
                utility: 'helm',
                stageName: 'Deploy: dev',
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
              ],
              [
                utility: 'test',
                stageName: 'Test: dev',
                containerName: 'kubectl',
                namespace: 'kube-dev',
                waitFor: [
                  [labels: ["app.kubernetes.io/instance=${PROJECT_NAME}-dev", "app.kubernetes.io/name=${HELM_CHART_NAME}"]]
                ],
                curl: [
                  [url: "http://${PROJECT_NAME}-dev-${HELM_CHART_NAME}.kube-dev/status"]
                ]
              ],
              [
                method: 'dockerCleanup'
                stageName: 'Docker Image Cleanup',
                containerName: 'docker'
              ]
            ])

          }

				}

			}

			stage('Deploy Stages') {

        steps {

          script {

            if(config.containsKey("deployments")) {

              config.deployments.each { deployment ->

                stage("Promotion: ${deployment}") {
                  timeout(time: 1, unit: 'DAYS') {
                    input message: "Promote to ${deployment}?", 
                    ok: 'Promote', 
                    parameters: [string(defaultValue: '', description: 'Approver Comments', name: 'COMMENT', trim: false)], 
                    submitter: 'admin'
                  }
                }

                podTemplate(label: "deploy-${deployment}", yaml: podConfigBuilder.addAnnotations("""
                    podTemplateClass: YamlPodConfigurationBuilder
                    podTemplateType: deploy
                  """).addLabels("""
                    app: DynamicJenkinsAgent
                    type: deploy
                  """).removeContainers(['git', 'maven'])
                  .removeVolumes(['mvnm2']).build()) {
                  node("deploy-${deployment}") {

                    DeployStages.stages(this, [
                      [
                        utility: 'helm',
                        stageName: "Deploy: ${deployment}",
                        containerName: 'helm',
                        name: "${PROJECT_NAME}-${deployment}",
                        namespace: "kube-${deployment}",
                        overrides: [
                          "image.repository=${DOCKER_REGISTRY}/${DOCKER_REPOSITORY}",
                          "image.tag=${DOCKER_TAG}",
                          "service.type=NodePort"
                        ],
                        chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                        chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                        chartName: HELM_CHART_NAME
                      ],
                      [
                        utility: 'test',
                        stageName: "Test: ${deployment}",
                        containerName: 'kubectl',
                        namespace: "kube-${deployment}",
                        waitFor: [
                          [labels: ["app.kubernetes.io/instance=${PROJECT_NAME}-${deployment}", "app.kubernetes.io/name=${HELM_CHART_NAME}"]]
                        ],
                        curl: [
                          [url: "http://${PROJECT_NAME}-${deployment}-${HELM_CHART_NAME}.kube-${deployment}/status"]
                        ]
                      ],
                      [
                        utility: 'dockerCleanup',
                        stageName: 'Docker Image Cleanup',
                        containerName: 'docker'
                      ]
                    ])

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