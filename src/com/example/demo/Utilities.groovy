#!/usr/bin/env groovy
package com.example.demo

class Utilities {

	static def notifyBuild(script, config, env) {
	  // build status of null means successful
	  def buildStatus = config.status ?: 'SUCCESSFUL'
	  def subject = "${buildStatus} -> ${config.stage}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"  
	  def details = """<p>Hi Team,</p>
	  <p><b><u>Jenkins Build Pipeline Information</u></b></p>
	  <p>Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	  <p>Stage: ${config.stage} -> Status: ${buildStatus}</p>
	  <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>
	  <p>Regards,</p>
	  <p>Jenkins</p>"""
	  emailext (
	    mimeType: 'text/html',
	    to: config.recipients,
	    subject: subject,
	    body: details,
	    recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	  )
	}

}