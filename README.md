# Jenkins Groovy Shared Libraries
Custom Groovy Shared Libraries used across Jenkins Pipelines (Pipelines). Pipeline has support for creating "Shared Libraries" which can be defined in external source control repositories and loaded into existing Pipelines.

## Introduction
A Shared Library is defined with a name, a source code retrieval method such as by SCM, and optionally a default version. The name should be a short identifier as it will be used in scripts.

The best way to specify the SCM is using an SCM plugin which has been specifically updated to support a new API for checking out an arbitrary named version (Modern SCM option). As of this writing, the latest versions of the Git and Subversion plugins support this mode.

More information detailing installation and various features is present in the [Jenkins Documentation](https://jenkins.io/doc/book/pipeline/shared-libraries/)

## Project Description
This project contains custom groovy code pertaining to the shared library directory structure. These are custom implementations built to be used for these Pipelines.

### Project Directory Structure
The Directory Structure of this Shared Library is as follows:
The directory structure of a Shared Library repository is as follows:
```
(root)
+- src                     
|   +- com
|       +- example
|           +- demo  
|               +- GlobalVars.groovy   
+- vars
|   +- createYaml.groovy
|   +- dockerBuildDeploy.groovy
|   +- createYaml.groovy
```
