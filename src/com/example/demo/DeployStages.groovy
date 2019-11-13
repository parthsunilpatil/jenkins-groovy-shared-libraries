#!/usr/bin/env groovy
package com.example.demo

class DeployStages {

    static def stages(script, stagesConfig) {
        stagesConfig.each { stage, config -> 
            script.stage(config.stageName) {
                factory(script, stage, config)
            }
        }
    }

    static def factory(script, name, config) {
        switch(name) {
            case "helm":
                helm(script, config)
                break
            case "test":
                test(script, config)
                break
            case "dockerCleanup":
                dockerCleanup(script, config)
                break
            default:
                break
        }
    }

    static def helm(script, config) {
        script.container(config.containerName) {
            script.sh script: """
                echo "Helm Install : config = ${config}"
                helm init --client-only
            """, label: "Initialize Helm Client"
            
            if(config.chartsRepositoryName != "stable") {
                script.sh script: """
                    helm repo add ${config.chartsRepositoryName} ${config.chartsRepositoryUrl}
                    helm repo list
                """, label: "Add Helm Repository - name=${config.chartsRepositoryName}, repo=${config.chartsRepositoryUrl}"
            }

            def helmCmd = "helm upgrade --install ${config.name} --namespace ${config.namespace}"

            if(config.containsKey("values")) {
                script.sh script: """
                    echo "Getting src from ${config.values.gitRepository} which contains values file"
                    git clone -b ${config.values.gitBranch} ${config.values.gitRepository} .
                    pwd; ls -ltr
                """, label: "Checkout Source for Values Files - branch=${config.values.gitBranch}, repo=${config.values.gitRepository}"
                helmCmd += " -f ${config.values.file}"
            } else if(config.containsKey("overrides")) {
                config.overrides.each {
                    helmCmd += " --set ${it}"
                }
            }

            helmCmd += " ${config.chartsRepositoryName}/${config.chartName}"
            script.sh script: helmCmd, label: "Install Helm Chart - name=${config.chartName}"
            
        }
    }

    static def test(script, config) {
        script.container(config.containerName) {
            if(config.containsKey('waitFor')) {
                config.waitFor.each {
                    def podSh = "kubectl -n ${config.namespace} get pod"
                    if(it.containsKey('labels')) {
                        it.labels.each {
                            podSh += " -l \'${it}\'"
                        }
                    }
                    podSh += " -o \'jsonpath={.items[0].metadata.name}\'"
                    def pod = script.sh returnStdout: true, script: podSh, label: "Get Pod Name - labels=${it.labels}"
                    script.sh script: "kubectl -n ${config.namespace} wait --timeout=3600s --for=condition=Ready pod/${pod}", label: "Wait for pod/${pod} to be ready"
                }
            }

            if(config.containsKey('curl')) {
                config.curl.each {
                    def curl = "curl -i"
                    if(it.containsKey('method')) {
                        curl += " -X ${it.method}"
                    }
                    if(it.containsKey('url')) {
                        curl += " --url ${it.url}"
                    }
                    if(it.containsKey('data')) {
                        it.data.each {
                            curl += " --data \'${it}\'"
                        }
                    }
                    script.sh script: curl, label: "Run Curl Command - url=${it.url}"
                }
            }

        }
    }

    static def dockerCleanup(script, config) {
        script.container(config.containerName) {
            script.sh script: """
              echo "Cleaning up dangling images"
              if ! docker rmi --force \$(docker images -f \"dangling=true\" -q); then
                echo "Clean Up of dangling not in use docker images completed"
              fi
            """, label: "Docker Clean-up"
        }
    }

}