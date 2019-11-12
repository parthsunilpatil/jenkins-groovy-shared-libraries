#!/usr/bin/env groovy
package com.example.demo

@Grab(group = 'org.yaml', module='snakeyaml', version = "1.18")
import org.yaml.snakeyaml.Yaml

class GlobalVars {
    static final String PODTEMPLATE_YAML = """
    apiVersion: v1
    kind: Pod
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

    static def getYaml(String mode = 'BUILD') {
        Yaml yaml = new Yaml()
        def content = yaml.load(PODTEMPLATE_YAML)
        print "content: ${content}"
        print "yaml: " + yaml.dump(content)
        return yaml.dump(content)
    }
}