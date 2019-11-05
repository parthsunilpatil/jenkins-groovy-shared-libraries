#!/usr/bin/env groovy
package com.example.demo

class GlobalVars {
    static final String GLOBALVARS_DEFAULT_NAME = "||GlobalVars||DEFAULT_NAME||"
    static final String PODTEMPLATE_BUILD_YAML = """
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
      volumes:
      - name: dockersock
        hostPath:
          path: /var/run/docker.sock
      - name: mvnm2
        hostPath:
          path: /c/Users/parthp/m2
    """
    static final String PODTEMPLATE_DEPLOY_YAML = """
    apiVersion: v1
    kind: Pod
    spec:
      containers:
      - name: helm
        image: parthpatil3110/k8s-helm:2.16.0-rc.2
        command:
        - cat
        tty: true
        volumeMounts:
        - name: kubeconfig
          mountPath: /root/.kube/config
        - name: dockersock
          mountPath: /var/run/docker.sock
      volumes:
      - name: dockersock
        hostPath:
          path: /var/run/docker.sock
      - name: kubeconfig
        hostPath:
          path: /c/Users/parthp/.kube/config
    """
}