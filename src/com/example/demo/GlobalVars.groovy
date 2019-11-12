#!/usr/bin/env groovy
package com.example.demo

@Grab(group = 'org.yaml', module='snakeyaml', version = "1.18")
import org.yaml.snakeyaml.Yaml

class GlobalVars {
    static final String PODTEMPLATE_YAML = """
    apiVersion: v1
    kind: Pod
    metadata:
      annotations:
        podTemplateClass: "GlobalVars"
      labels:
        app: "dynamic-jenkins-agent"
    spec:
      containers:
      - name: docker
        image: docker:19
        command:
        - cat
        tty: true
        volumeMounts:
        - name: dockersock
          mountPath: /var/run/docker.sock
      - name: maven
        image: maven:3.3.9-jdk-8-alpine
        command:
        - cat
        tty: true
        volumeMounts:
        - name: mvnm2
          mountPath: /root/.m2/repository
      - name: git
        image: alpine/git:1.0.7
        command:
        - cat
        tty: true
      - name: helm
        image: parthpatil3110/k8s-helm:2.16.0-rc.2
        command:
        - cat
        tty: true
        volumeMounts:
        - name: kubeconfig
          mountPath: /root/.kube/config
      - name: kubectl
        image: parthpatil3110/k8s-kubectl-psql:custom
        command:
        - cat
        tty: true
        volumeMounts:
        - name: kubeconfig
          mountPath: /root/.kube/config
      volumes:
      - name: dockersock
        hostPath:
          path: /var/run/docker.sock
      - name: mvnm2
        hostPath:
          path: /c/Users/parthp/volumes/m2
      - name: kubeconfig
        hostPath:
          path: /c/Users/parthp/.kube/config
    """

    @NonCPS
    static def truncateYaml(content, removals) {
        def returnContent = [:] << content
        def containers = content.spec.containers
        removals.containers.each { name ->
            containers.removeIf {
                it.name == name
            }
        }
        returnContent.spec.containers = containers
        def volumes = content.spec.volumes
        removals.volumes.each { name ->
            volumes.removeIf {
                it.name == name
            }
        }
        returnContent.spec.volumes = volumes
        return returnContent
    }

    @NonCPS
    static def getYaml(String mode = 'BUILD') {
        Yaml yaml = new Yaml()
        def content = yaml.load(PODTEMPLATE_YAML)
        def updatedContent = [:] << content
        print "content: ${content}"
        if(mode == 'DEPLOY') {
            updatedContent = truncateYaml(content, [
                containers: ['maven', 'git'],
                volumes: ['mvnm2']
            ])
        }
        print " Updated yaml: " + yaml.dump(updatedContent)
        return yaml.dump(updatedContent)
    }
}