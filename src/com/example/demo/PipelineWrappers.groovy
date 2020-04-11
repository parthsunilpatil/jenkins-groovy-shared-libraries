#!/usr/bin/env groovy
package com.example.demo

class PipelineWrappers {

	static def callClosure(config, Closure closure) {
		if(config.containsKey("closureParams")) {
			closure(config.closureParams)
		} else {
			closure()
		}
	}

	static def emailNotification(script, config) {
	  // build status of null means successful
	  def buildStatus = config.status ?: 'SUCCESSFUL'
	  def subject = "${buildStatus} -> ${config.stage}: Job '${script.env.JOB_NAME} [${script.env.BUILD_NUMBER}]'"  
	  def details = """<p>Hi Team,</p>
	  <p><b><u>Jenkins Build Pipeline Information</u></b></p>
	  <p>Job '${script.env.JOB_NAME} [${script.env.BUILD_NUMBER}]':</p>
	  <p>Stage: ${config.stage} -> Status: ${buildStatus}</p>
	  <p>Check console output at "<a href="${script.env.BUILD_URL}">${script.env.JOB_NAME} [${script.env.BUILD_NUMBER}]</a>"</p>
	  <p>Regards,</p>
	  <p>Jenkins</p>"""
	  script.emailext (
	    mimeType: 'text/html',
	    to: config.recipients,
	    subject: subject,
	    body: details,
	    recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	  )
	}
	
	static def promotionEmailNotification(script, config) {
		def subject = "Promotion -> ${config.deployment}: Job '${script.env.JOB_NAME} [${script.env.BUILD_NUMBER}]'"
		def details = """<p>Hi Team,</p>
		<p><b><u>Jenkins Build Pipeline Stage Promotion</u></b></p>
		<p>Job '${script.env.JOB_NAME} [${script.env.BUILD_NUMBER}]':</p>
		<p>Promotion: ${config.deployment}, Started by ${script.env.BUILD_USER}</p>
		<p>Check console output at "<a href="${script.env.BUILD_URL}">${script.env.JOB_NAME} [${script.env.BUILD_NUMBER}]</a>"</p>
		<p><a href='${script.env.BUILD_URL}input'>Click Here</a> for build promotion.</p>
		<p>Regards,</p>
		<p>Jenkins</p"""
		script.emailext (
		  mimeType: 'text/html',
		  to: config.recipients,
		  subject: subject,
		  body: details,
		  recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	  )
	}
	
	static def promotion(script, config) {
		try {
			script.stage("Promotion: ${config.deployment}") {

				promotionEmailNotification(script, [
					deployment: config.deployment,
					recipients: config.recipients
				]);

				script.timeout(time: 1, unit: 'DAYS') {
					script.input message: "Promote to ${config.deployment}?",
					ok: 'Promote',
					parameters: [script.string(defaultValue: '', description: 'Approver Comments', name: 'COMMENT', trim: false)],
					submitter: "${config.submitters}"
				}

			}
		} catch(err) {
			if(err != null) {
				script.echo err.getMessage();
				throw err	
			}
		}
	}

	static def email(script, config, Closure closure) {
		try {
			callClosure(config, closure)
		} catch(err) {
			if(err != null) {
				script.echo err.getMessage();
				throw err	
			}
		} finally {
			emailNotification(script, [
				status: config.status,
				stage: config.name,
				recipients: config.recipients
			]);
		}
	}

	static def rollingDeployments(script, config, Closure closure) {

		config.environments.each { deploymentEnvironment ->

			promotion(script, [
				deployment: deploymentEnvironment,
				submitters: config.promotion.submitters,
				recipients: config.promotion.recipients
			])

			callClosure([closureParams: [environment: deploymentEnvironment]], closure)

		}

	}

	static def dynamicAgentPodTemplate(script, config, Closure closure) {
		script.podTemplate([
			label: config.label,
			yaml: config.yaml
		]) {
			script.node(config.label) {
				callClosure(config, closure)
			}
		}
	}

	static def stages(script, config, Closure closure) {
		script.stages {
			script.agent {
				label "${config.agent.label}"
			}
			config.iterations.each { itr ->
				callClosure([closureParams: itr], closure)
			}
		}
	}

}