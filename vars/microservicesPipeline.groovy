#!/usr/bin/env groovy
import com.example.demo.GlobalVars
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.BuildStages
import com.example.demo.DeployStages
import com.example.demo.PipelineStages

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
			
			stage('Stages: Build & Deploy - dev') {
        steps {
          script {
            PipelineStages.stages(this, [
              podTemplate: [
                label: "dynamic-jenkins-agent",
                yaml: new YamlPodConfigurationBuilder().addAnnotations("""
                  podTemplateClass: YamlPodConfigurationBuilder
                  podTemplateType: build
                """).addLabels("""
                  app: DynamicJenkinsAgent
                """).removeContainers(['angular']).build()
              ],
              stages: [
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
                ],
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
                [parallel: [
                  [
                    utility: 'dockerCleanup',
                    stageName: 'Docker Image Cleanup',
                    containerName: 'docker'
                  ],
                  [
                    utility: 'waitFor',
                    stageName: "Wait For Pod Ready: ${PROJECT_NAME}",
                    containerName: 'kubectl',
                    namespace: 'kube-dev',
                    labels: ["app.kubernetes.io/instance=${PROJECT_NAME}-dev", "app.kubernetes.io/name=${HELM_CHART_NAME}"]
                  ]
                ]],
                [
                  utility: 'curl',
                  stageName: 'Test: dev',
                  containerName: 'kubectl',
                  namespace: 'kube-dev',
                  curl: [[url: "http://${PROJECT_NAME}-dev-${HELM_CHART_NAME}.kube-dev/status"]]
                ]
              ]
            ])
          }
        }
      }

			stage('Stages: Deploy') {
        steps {
          script {
            config.deployments.each { deployment ->
              PipelineStages.stages(this, [
                stages: [[
                  utility: 'promotion',
                  stageName: "Promotion: ${deployment.name}",
                  deployment: deployment.name,
                  submitters: deployment.submitters
                ]]
              ])
              PipelineStages.stages(this, [
                podTemplate: [
                  label: "dynamic-jenkins-agent",
                  yaml: new YamlPodConfigurationBuilder().addAnnotations("""
                    podTemplateClass: YamlPodConfigurationBuilder
                    podTemplateType: deploy
                  """).addLabels("""
                    app: DynamicJenkinsAgent
                  """).removeContainers(['git', 'maven', 'angular'])
                  .removeVolumes(['mvnm2']).build()
                ],
                stages: [
                  [
                    utility: 'helm',
                    stageName: "Deploy: ${deployment.name}",
                    containerName: 'helm',
                    name: "${PROJECT_NAME}-${deployment.name}",
                    namespace: "kube-${deployment.name}",
                    overrides: [
                      "image.repository=${DOCKER_REGISTRY}/${DOCKER_REPOSITORY}",
                      "image.tag=${DOCKER_TAG}",
                      "service.type=NodePort"
                    ],
                    chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                    chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                    chartName: HELM_CHART_NAME
                  ],
                  [parallel: [
                    [
                      utility: 'dockerCleanup',
                      stageName: 'Docker Image Cleanup',
                      containerName: 'docker'
                    ],
                    [
                      utility: 'waitFor',
                      stageName: "Wait For Pod Ready: ${PROJECT_NAME}",
                      containerName: 'kubectl',
                      namespace: "kube-${deployment,name}",
                      labels: ["app.kubernetes.io/instance=${PROJECT_NAME}-${deployment.name}", "app.kubernetes.io/name=${HELM_CHART_NAME}"]
                    ]
                  ]],
                  [
                    utility: 'curl',
                    stageName: "Test: ${deployment.name}",
                    containerName: 'kubectl',
                    namespace: "kube-${deployment.name}",
                    curl: [[url: "http://${PROJECT_NAME}-${deployment.name}-${HELM_CHART_NAME}.kube-${deployment.name}/status"]]
                  ]
                ]
              ])
            }
          }
        }
			}

		}

	}
} 