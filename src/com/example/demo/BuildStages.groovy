#!/usr/bin/env groovy
package com.example.demo

class BuildStages {

	static def git(script, config) {
		script.container(config.containerName) {
			script.sh script: """
            	echo "Checkout Steps : config = ${config}"
            	git clone -b ${config.gitBranch} ${config.gitRepository} .
            	pwd; ls -ltr
            """, label: "Source Code Checkout - branch=${config.gitBranch},url=${config.gitRepository}"
		}
	}

    static def mvn(script, config) {
        script.container(config.containerName) {
            script.sh script: """
                mvn clean package
                pwd; ls -ltr
            """, label: "Maven Build"
        }
    }

    static def dockerBuildDeploy(script, config) {
        script.container(config.containerName) {
            script.sh script: """
                echo "Docker Build, Deploy & Cleanup : config = ${config}"
                docker build -t ${config.registry}/${config.repository}:${config.tag} .
            """, label: "Docker Build - image=${config.registry}/${config.repository}:${config.tag}"
            
            if(!config.buildOnly) {
                script.withCredentials([[$class:
                    'UsernamePasswordMultiBinding',
                    credentialsId: "${config.credentialsId}",
                    usernameVariable: 'DOCKER_HUB_USER',
                    passwordVariable: 'DOCKER_HUB_PASSWORD']]) {
                    script.sh script: """
                        docker login ${config.registry} -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}
                        docker push ${config.registry}/${config.repository}:${config.tag}
                    """, label: "Docker Deploy - image=${config.registry}/${config.repository}:${config.tag}"
                }
            }
        }
    }

    static def factory(script, config) {
        switch(config.method) {
            case "git":
                git(script, config)
                break
            case "maven":
                mvn(script, config)
                break
            case "docker":
                dockerBuildDeploy(script, config)
                break
            default:
                break
        }
    }

    static def stages(script, stagesConfig) {
        stagesConfig.each { config -> 
            script.stage(config.stageName) {
                factory(script, config)
            }
        }
    }

}