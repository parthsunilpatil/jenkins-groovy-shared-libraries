#!/usr/bin/env groovy
import com.example.demo.GlobalVars
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.DeployStages

def call(Map config) {
	
    def HELM_CHART_REPOSITORY_NAME="${config.HELM_CHART_REPOSITORY_NAME}"
    def HELM_CHART_REPOSITORY_URL="${config.HELM_CHART_REPOSITORY_URL}"
    def PROJECT_K8S_DEPLOYMENT_NAMESPACE="${config.PROJECT_K8S_DEPLOYMENT_NAMESPACE}"

    def podConfigBuilder = new YamlPodConfigurationBuilder()

    pipeline {
        
        agent {
            kuberentes {
                yaml podConfigBuilder.addAnnotations("""
                        podTemplateClass: YamlPodConfigurationBuilder
                        podTemplateType: deploy
                    """).addLabels("""
                        app: DynamicJenkinsAgent
                        type: deploy
                    """).removeContainers(['git', 'maven'])
                    removeVolumes(['mvnm2']).build()    
            }
        }

        stages {

            stage('Deploy API Gateway & Dashboard') {
                steps {
                    script {
                        DeployStages.stages(this, [
                            [
                                method: 'helm',
                                stageName: 'Deploy API Gateway: Kong',
                                containerName: 'helm',
                                name: 'kong',
                                namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                                chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                                chartName: 'kong'
                            ],
                            [
                                method: 'helm',
                                stageName: 'Deploy Dashboard: Konga',
                                containerName: 'helm',
                                name: 'konga',
                                namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                                chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                                chartName: 'konga'
                            ],
                            [
                                method: 'test',
                                stageName: 'Test Kong',
                                containerName: 'kubectl',
                                namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                waitFor: [
                                    [labels: ['app=kong', 'release=kong', 'component=app']]
                                ],
                                curl: [
                                    [
                                        method: 'POST',
                                        url: "http://kong-kong-admin.${PROJECT_K8S_DEPLOYMENT_NAMESPACE}:8001/services",
                                        data: [
                                            'name=example-service',
                                            'url=http://mockbin.org'
                                        ]
                                    ],
                                    [
                                        method: 'POST',
                                        url: "http://kong-kong-admin.${PROJECT_K8S_DEPLOYMENT_NAMESPACE}:8001/services/example-service/routes",
                                        data: [
                                            'paths[]=/example'
                                        ]
                                    ]
                                ]
                            ]
                        ])
                    }
                }
            }

        }

    }

}