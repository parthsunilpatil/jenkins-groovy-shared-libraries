#!/usr/bin/env groovy
import com.example.demo.GlobalVars
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.PipelineStages

def call(Map config) {
	
    def HELM_CHART_REPOSITORY_NAME="${config.HELM_CHART_REPOSITORY_NAME}"
    def HELM_CHART_REPOSITORY_URL="${config.HELM_CHART_REPOSITORY_URL}"
    def PROJECT_K8S_DEPLOYMENT_NAMESPACE="${config.PROJECT_K8S_DEPLOYMENT_NAMESPACE}"

    def podConfigBuilder = new YamlPodConfigurationBuilder()

    pipeline {
        
        agent none

        stages {

            stage('Stages: Deploy API Gateway & Dashboard') {
                steps {
                    script {
                        PipelineStages.stages(this, [
                            podTemplate: [
                                label: "dynamic-jenkins-agent",
                                yaml: podConfigBuilder.addAnnotations("""
                                        podTemplateClass: YamlPodConfigurationBuilder
                                        podTemplateType: deploy
                                    """).addLabels("""
                                        app: DynamicJenkinsAgent-helm
                                        type: deploy
                                    """).removeContainers(['git', 'maven', 'kubectl', 'docker'])
                                    .removeVolumes(['mvnm2', 'dockersock']).build()
                            ],
                            stages: [
                                [
                                    utility: 'helm',
                                    stageName: 'Deploy API Gateway: Kong',
                                    containerName: 'helm',
                                    name: 'kong',
                                    namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                    chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                                    chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                                    chartName: 'kong',
                                    overrides: [
                                        "proxy.ingress.enabled=true"
                                    ]
                                ],
                                [
                                    utility: 'helm',
                                    stageName: 'Deploy Dashboard: Konga',
                                    containerName: 'helm',
                                    name: 'konga',
                                    namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                    chartsRepositoryName: HELM_CHART_REPOSITORY_NAME,
                                    chartsRepositoryUrl: HELM_CHART_REPOSITORY_URL,
                                    chartName: 'konga',
                                    overrides: [
                                        "ingress.enabled=true"
                                    ]
                                ]
                            ]
                        ])

                    }
                }
            }

            stage('Stages: Test Deployments') {
                steps {
                    script {
                        PipelineStages.stages(this, [
                            podTemplate: [
                                label: "dynamic-jenkins-agent",
                                yaml: podConfigBuilder.addAnnotations("""
                                        podTemplateClass: YamlPodConfigurationBuilder
                                        podTemplateType: deploy
                                    """).addLabels("""
                                        app: DynamicJenkinsAgent-kubectl
                                        type: deploy
                                    """).removeContainers(['git', 'maven', 'helm', 'docker'])
                                    .removeVolumes(['mvnm2', 'dockersock']).build()
                            ],
                            stages: [
                                [parallel: [
                                    [
                                        utility: 'waitFor',
                                        stageName: 'Wait For Pod Ready: postgres',
                                        containerName: 'kubectl',
                                        namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                        labels: ["app.kubernetes.io/instance=kong", "app.kubernetes.io/name=postgres"]
                                    ],
                                    [
                                        utility: 'waitFor',
                                        stageName: 'Wait For Pod Ready: kong',
                                        containerName: 'kubectl',
                                        namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                        labels: ["app=kong", "component=app", "release=kong"]
                                    ],
                                    [
                                        utility: 'waitFor',
                                        stageName: 'Wait For Pod Ready: konga',
                                        containerName: 'kubectl',
                                        namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                        labels: ["app.kubernetes.io/instance=konga", "app.kubernetes.io/name=konga"]
                                    ]
                                ]],
                                [parallel: [
                                    [
                                        utility: 'curl',
                                        stageName: 'Add Example Service & Route: /posts',
                                        containerName: 'kubectl',
                                        namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                        curl: [
                                            [
                                                method: 'POST',
                                                url: "http://kong-kong-admin.${PROJECT_K8S_DEPLOYMENT_NAMESPACE}:8001/services",
                                                data: [
                                                    'name=posts-example-service',
                                                    'url=http://jsonplaceholder.typicode.com/posts'
                                                ]
                                            ],
                                            [
                                                method: 'POST',
                                                url: "http://kong-kong-admin.${PROJECT_K8S_DEPLOYMENT_NAMESPACE}:8001/services/posts-example-service/routes",
                                                data: [
                                                    'name=posts-example-service-route',
                                                    'paths[]=/posts'
                                                ]
                                            ]
                                        ]
                                    ],
                                    [
                                        utility: 'curl',
                                        stageName: 'Add Example Service & Route: /users',
                                        containerName: 'kubectl',
                                        namespace: PROJECT_K8S_DEPLOYMENT_NAMESPACE,
                                        curl: [
                                            [
                                                method: 'POST',
                                                url: "http://kong-kong-admin.${PROJECT_K8S_DEPLOYMENT_NAMESPACE}:8001/services",
                                                data: [
                                                    'name=users-example-service',
                                                    'url=http://jsonplaceholder.typicode.com/users'
                                                ]
                                            ],
                                            [
                                                method: 'POST',
                                                url: "http://kong-kong-admin.${PROJECT_K8S_DEPLOYMENT_NAMESPACE}:8001/services/users-example-service/routes",
                                                data: [
                                                    'name=users-example-service-route',
                                                    'paths[]=/users'
                                                ]
                                            ]
                                        ]
                                    ]
                                ]]
                            ]
                        ])
                    }
                }
            }

        }

    }

}