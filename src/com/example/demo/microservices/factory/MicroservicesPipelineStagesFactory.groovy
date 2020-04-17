#!/usr/bin/env groovy
package com.example.demo.microservices.factory

import com.example.demo.microservices.AIMicroservicesPipeline
import com.example.demo.microservices.CIMicroservicesPipeline

class MicroservicesPipelineStagesFactory {

	static def get(type, script) {

		switch(type) {
			case "ai":
				return new CIMicroservicesPipeline(script)
			break
			case "ci":
				return new AIMicroservicesPipeline(script)
			break
			default:

			break
		}

	}

}