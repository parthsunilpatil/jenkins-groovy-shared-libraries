#!/usr/bin/env groovy
import com.example.demo.GlobalVars
import com.example.demo.YamlPodConfigurationBuilder
import com.example.demo.DeployStages
import com.example.demo.PipelineStages

def call(Map config) {

    pipeline {

        agent {
            kubernetes {
                yaml new YamlPodConfigurationBuilder().addAnnotations("""
                    podTemplateClass: YamlPodConfigurationBuilder
                    podTemplateType: deploy
                """).addLabels("""
                    app: DynamicJenkinsAgent-helm
                    type: deploy
                """).removeContainers(['git', 'maven', 'docker'])
                .removeVolumes(['mvnm2', 'dockersock']).build()
            }
        }

        stages {

            stage('Deploy: nginx-ingress') {
                steps {
                    script {
                        DeployStages.helm(this, [
                            containerName: 'helm',
                            name: 'nginx-ingress',
                            namespace: 'kube-ingress',
                            chartsRepositoryName: 'stable',
                            chartName: 'nginx-ingress'
                        ])
                    }
                }
            }

            stage('Test: nginx-ingress') {
                steps {
                    script {
                        PipelineStages.stages(this, [
                            stages: [
                                [parallel: [
                                    [
                                        utility: 'waitFor',
                                        stageName: 'Wait For Pod Ready: nginx-ingress-controller',
                                        containerName: 'kubectl',
                                        namespace: 'kube-ingress',
                                        labels: ["app=nginx-ingress", "component=controller", "release=nginx-ingress"]
                                    ],
                                    [
                                        utility: 'waitFor',
                                        stageName: 'Wait For Pod Ready: nginx-ingress-default-backend',
                                        containerName: 'kubectl',
                                        namespace: 'kube-ingress',
                                        labels: ["app=nginx-ingress", "component=default-backend", "release=nginx-ingress"]
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