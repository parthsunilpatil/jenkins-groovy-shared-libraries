#!/usr/bin/env groovy
import com.example.demo.PodTemplateYamls
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.PipelineWrappers

def call(Map config) {
	
	def projectName = "${config.PROJECT_NAME}"

	def dockerRegistry = "${config.DOCKER_REGISTRY}"
	def dockerRepository = "${config.DOCKER_REPOSITORY}"
	def dockerTag = "v1"

	def gitRepository = "${config.GIT_REPOSITORY}"
	def gitBranch = "${config.GIT_BRANCH}"

	def helmChartName = "${config.HELM_CHART_NAME}"
	def helmChartsRepository = "${config.HELM_CHARTS_REPOSITORY}"
	def helmChartsRepositoryUrl = "${config.HELM_CHARTS_REPOSITORY_URL}"

	pipeline {

		agent none

		stages {

			stage("Build Stages") {
				steps {
					script {

						PipelineWrappers.dynamicAgentPodTemplate(this, [
							label: "dynamic-jenkins-build-agent",
							yaml: new YamlPodConfigurationBuilder().forDefaultBuildStages().build()
						], {

							PipelineWrappers.email(this, [
								status: currentBuild.result,
								name: "Checkout",
								recipients: "parth.patil@imaginea.com"
							], {
								stage("Checkout") {
									container("git") {
										sh script: """
											git clone -b ${gitBranch} ${gitRepository} .
											pwd; ls -ltr
										""", label: "Checkout Source Code"
									}
								}
							})

							PipelineWrappers.email(this, [
								status: currentBuild.result,
								name: "Maven Build",
								recipients: "parth.patil@imaginea.com"
							], {
								stage("Maven Build") {
									container("maven") {
										sh script: """
											mvn clean install
										""", label: "Maven Build"

										dockerTag = readMavenPom().properties["docker-tag-version"]
										echo dockerTag
									}
								}
							})

							PipelineWrappers.email(this, [
								status: currentBuild.result,
								name: "Docker Build & Deploy",
								recipients: "parth.patil@imaginea.com"
							], {
								stage("Docker Build & Deploy") {
									container("docker") {
										sh script: """
											docker build --no-cache -t ${dockerRegistry}/${dockerRepository}:${dockerTag} .
										""", label: "Docker Build & Deploy"
									}
								}
							})

						})

					}
				}
			}

		}

	}

}