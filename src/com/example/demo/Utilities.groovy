#!/usr/bin/env groovy
package com.example.demo

class Utilities {

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

}