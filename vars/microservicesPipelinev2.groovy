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

  pipeline {
    agent none

    stages {

      stage("Build Stages") {
        steps {
          script {
            podTemplate(
              label: "dynamic-jenkins-agent",
              yaml: new YamlPodConfigurationBuilder().addAnnotations("""
                podTemplateClass: YamlPodConfigurationBuilder
                podTemplateType: build
              """).addLabels("""
                app: DynamicJenkinsAgent
              """).build()) {

              stage("Git Checkout") {
                BuildStages.git(this, [
                  containerName: 'git',
                  gitRepository: GIT_REPOSITORY,
                  gitBranch: GIT_BRANCH
                ])
              }

              stage("Maven Build") {
                BuildStages.mvn(this, [containerName: 'maven'])
              }

              stage("Docker Build & Deploy") {
                BuildStages.docker(this, [
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
      }

      stage("Deploy Stages") {
        steps {
          script {

            
            
          }
        }
      }

    }
  }
}