require 'fileutils'
require 'buildr/jacoco'

PROJECT_NAME = 'bunkr'
PROJECT_GROUP = "org.#{PROJECT_NAME}"
PROJECT_VERSION = '0.3.0'
COMPATIBLE_PROJECT_VERSION = '0.3.0'

# where to pull maven dependencies from
repositories.remote << 'https://repo.maven.apache.org/maven2'
repositories.remote << 'https://repo1.maven.org/maven2'

# dependencies
JAR_JUNIT = "junit:junit:jar:4.12"
JAR_BC = "org.bouncycastle:bcprov-jdk15on:jar:1.53"
JAR_JSON_SIMPLE = "com.googlecode.json-simple:json-simple:jar:1.1.1"
JAR_ARGPARSE = "net.sourceforge.argparse4j:argparse4j:jar:0.6.0"

layout = Layout.new
layout[:source, :main, :java] = 'src'
layout[:source, :test, :java] = 'tests'
layout[:target, :generated] = '../target'
layout[:target, :main, :classes] = '../target/classes'

# define main project
define PROJECT_NAME do

    project.version = PROJECT_VERSION
    project.group = PROJECT_GROUP

    define 'bunkr-core', layout: layout do
        test.with JAR_JUNIT
        compile.with JAR_BC, JAR_JSON_SIMPLE
        compile.using(source: '1.8', target: '1.8', lint: 'all')
    end

    define 'bunkr-cli', layout: layout do
        test.with JAR_JUNIT
        compile.with projects('bunkr-core'), JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE
        compile.using(source: '1.8', target: '1.8', lint: 'all')
    end

    define 'bunkr-gui', layout: layout do
        test.with JAR_JUNIT
        compile.with JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE
        compile.using(source: '1.8', target: '1.8', lint: 'all')
    end

end
