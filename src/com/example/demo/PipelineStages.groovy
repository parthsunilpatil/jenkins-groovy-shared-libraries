#!/usr/bin/env groovy
package com.example.demo
import com.example.demo.PipelineStagesFactory

class PipelineStages {
	
	static def addStages(script, stagesConfig) {
        stagesConfig.each { stageConfig ->
            if(stageConfig.containsKey('parallel')) {
                def parallelStages = [:]
                stageConfig.parallel.each { parallelStageConfig -> 
                    parallelStages.put(parallelStageConfig.stageName, addStage(script, parallelStageConfig))
                }
                parallel(parallelStages)
            } else {
                addStage(script, stageConfig)
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