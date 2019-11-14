#!/usr/bin/env groovy
package com.example.demo
import com.example.demo.PipelineStagesFactory

class PipelineStages {
	
	static def addStages(script, stagesConfig) {
        if(stagesConfig.containsKey('parallel')) {
            def parallelStages = [:]
            stagesConfig.parallel.each { stage ->
                parallelStages.put(stage.stageName, addStage(script, config))
            }
            parallel(parallelStages)
        } else {
            stagesConfig.each { config ->
                addStage(script, config)
            }
        }
    }


    static def addStage(script, config) {
        return {
            script.stage(config.stageName) {
                PipelineStagesFactory.getStage(script, config)
            }
        }
    }

    static def stages(script, config) {

        if(config.containsKey('podTemplate')) {
            script.podTemplate(label: config.podTemplate.label, yaml: config.podTemplate.yaml) {
                script.node(config.podTemplate.label) {
                    addStages(script, config.stages)
                }
            }
        } else {
            addStages(script, config.stages)
        }

    }

}