#!/usr/bin/env groovy
package com.example.demo

class GlobalVars {
    static final String GLOBALVARS_DEFAULT_NAME = "||GlobalVars||DEFAULT_NAME||"
    static final String PODTEMPLATE_YAML = """
    apiVersion: v1
    kind: Pod
    spec:
      containers:
      - name: maven
        image: maven:alpine
        command:
        - cat
        tty: true
      - name: helm
        image: parthpatil3110/k8s-helm:latest
        command:
        - cat
        tty: true
        volumeMounts:
        - name: kubeconfig
          mountPath: /root/.kube/config
      volumes:
      - name: kubeconfig
        hostPath:
          path: /c/Users/parthp/.kube/config
    """
}