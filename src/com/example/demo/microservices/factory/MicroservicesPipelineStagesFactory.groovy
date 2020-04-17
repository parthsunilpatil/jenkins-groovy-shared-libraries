#!/usr/bin/env groovy
package com.example.demo.microservices.factory

import com.example.demo.microservices.CIMicroservicesPipelineStages
import com.example.demo.microservices.AIMicroservicesPipelineStages

class MicroservicesPipelineStagesFactory {

	static def get(type, script) {

		switch(type) {
			case "ai":
				return new CIMicroservicesPipelineStages(script)
			break
			case "ci":
				return new AIMicroservicesPipelineStages(script)
			break
			default:
				throw new ClassNotFoundException("No class associated with type: ${type}")
			break
		}

	}

}