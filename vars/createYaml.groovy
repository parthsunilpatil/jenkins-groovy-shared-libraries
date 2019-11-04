#!/usr/env/bin groovy

@Grab(group = 'org.yaml', module='snakeyaml', version = "1.18")
import org.yaml.snakeyaml.Yaml

def call(Map content) {
	Yaml yaml = new Yaml()
	echo yaml.dump(content)
}