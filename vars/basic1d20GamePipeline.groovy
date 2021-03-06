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
	def helmValuesUrl = "${config.HELM_VALUES_URL}"

	def deploymentEnvironments = config.DEPLOYMENT_ENVIRONMENTS

	pipeline {

		agent none

		stages {

			stage("Build Stages") {
				steps {
					script {

						PipelineWrappers.dynamicAgentPodTemplate(this, [
							label: "dynamic-jenkins-build-agent",
							yaml: new YamlPodConfigurationBuilder().withDefaultBuildStages().addNodeSelector("kubernetes.io/hostname: docker-desktop").build()
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
											docker build -t ${dockerRegistry}/${dockerRepository}:${dockerTag} .
										""", label: "Docker Build & Deploy"
									}
								}
							})

						})

					}
				}
			}

			stage("Deploy Stages") {
				steps {
					script {

						PipelineWrappers.rollingDeployments(this, [
							promotion: [
								submitters: "admin",
								recipients: "parth.patil@imaginea.com"
							],
							environments: deploymentEnvironments
						], { closureParams ->

							PipelineWrappers.dynamicAgentPodTemplate(this, [
								label: "dynamic-jenkins-deploy-agent",
								yaml: new YamlPodConfigurationBuilder().addNodeSelector("kubernetes.io/hostname: docker-desktop").withDefaultDeployStages().withDocker().setSecretNameForVolume("kubeconfig", "kubeconfig-${closureParams.environment}").build()
							], {

								PipelineWrappers.email(this, [
									status: currentBuild.result,
									name: "Checkout Deployment Properties - ${closureParams.environment}",
									recipients: "parth.patil@imaginea.com"
								], {
									
									stage("Checkout Deployment Properties - ${closureParams.environment}") {
										container("helm") {	
											sh script: """
												wget ${helmValuesUrl}/values-${closureParams.environment}.yaml
											""", label: "Checkout Deployment Properties"
										}
									}

								})

								PipelineWrappers.email(this, [
									status: currentBuild.result,
									name: "Deploy ${closureParams.environment}",
									recipients: "parth.patil@imaginea.com"
								], {

									stage("Deploy ${closureParams.environment}") {
										container("helm") {
											sh script: """
												helm init --client-only
												helm repo add ${helmChartsRepository} ${helmChartsRepositoryUrl}
	    										helm upgrade --install ${projectName}-kube-${closureParams.environment} -f values-${closureParams.environment}.yaml --set image.repository=${dockerRegistry}/${dockerRepository} --set image.tag=${dockerTag} --namespace kube-${closureParams.environment} ${helmChartsRepository}/${helmChartName}

											""", label: "Helm Deployment"
										}
									}

								})

								PipelineWrappers.email(this, [
									status: currentBuild.result,
									name: "Docker Cleanup",
									recipients: "parth.patil@imaginea.com"
								], {
									container("docker") {
										sh script: """
											echo "Cleaning up dangling images"
								            if ! docker rmi --force `docker images -f "dangling=true" -q`; then
								            	echo "Clean Up of dangling not in use docker images completed"
								            fi
										""", label: "Docker Cleanup"
									}		
								})

							})

						})

					}
				}
			}

		}

	}

}