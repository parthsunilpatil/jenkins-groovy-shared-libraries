#!/usr/bin/env groovy
package com.example.demo

class DeployStages {

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
                config.overrides.each { override ->
                    helmCmd += " --set ${override}"
                }
            }

            helmCmd += " ${config.chartsRepositoryName}/${config.chartName}"
            script.sh script: helmCmd, label: "Install Helm Chart - name=${config.chartName}"
            
        }
    }

    static def waitFor(script, config) {
        script.container(config.containerName) {
            def podSh = "kubectl -n ${config.namespace} get pod"
            config.labels.each { label ->
                podSh += " -l \'${label}\'"
            }
            podSh += " -o \'jsonpath={.items[0].metadata.name}\'"
            def podName = script.sh returnStdout: true, script: podSh, label: "Get Pod Name - labels=${config.labels}"
            script.sh script: "kubectl -n ${config.namespace} wait --timeout=3600s --for=condition=Ready pod/${podName}", label: "Wait for pod/${podName} to be ready"
        }
    }

    static def curl(script, config) {
        script.container(config.containerName) {
            config.curl.each { cmd ->
                def curl = "curl -i"
                if(cmd.containsKey('method')) {
                    curl += " -X ${cmd.method}"
                }
                if(cmd.containsKey('url')) {
                    curl += " --url ${cmd.url}"
                }
                if(cmd.containsKey('data')) {
                    cmd.data.each { data -> 
                        curl += " --data \'${data}\'"
                    }
                }
                script.sh script: curl, label: "Run Curl Command - url=${cmd.url}"
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