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

# define main project
define PROJECT_NAME do

    project.version = PROJECT_VERSION
    project.group = PROJECT_GROUP

    define 'bunkr-core', layout: layout do
        test.with JAR_JUNIT
        compile.with JAR_BC, JAR_JSON_SIMPLE
        compile.using(source: '1.8', target: '1.8', lint: 'all')
        package(:jar).merge(compile.dependencies).exclude('META-INF/BCKEY.*')
        package(:jar)
    end

    define 'bunkr-cli', layout: layout do
        test.with JAR_JUNIT, project('bunkr-core').test.compile.target
        compile.with JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE, project('bunkr-core')
        compile.using(source: '1.8', target: '1.8', lint: 'all')
    end

    define 'bunkr-gui', layout: layout do
        test.with JAR_JUNIT, project('bunkr-core').test.compile.target
        compile.with JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE, project('bunkr-core')
        compile.using(source: '1.8', target: '1.8', lint: 'all')
    end

    # ----------------------------------------------------------------------------------------

    def lib_copy(project_name)
        project(project_name).compile.dependencies.each do |t|
            if t.to_s.match(/\.jar$/) then
                unless File.exists? File.join(project.path_to('lib'), File.basename(t.to_s))
                    puts "Copying #{t} to 'lib' directory"
                    cp t.to_s, project.path_to('lib')
                end
            end
        end
        project(project_name).test.dependencies.each do |t|
            if t.to_s.match(/\.jar$/) then
                unless File.exists? File.join(project.path_to('lib'), File.basename(t.to_s))
                    puts "Copying #{t} to 'lib' directory"
                    cp t.to_s, project.path_to('lib')
                end
            end
        end
    end

    task :pulllibs do
        unless Dir.exists? project.path_to('lib')
            puts "Creating 'lib' directory"
            Dir.mkdir project.path_to('lib')
        end
        lib_copy('bunkr-core')
        lib_copy('bunkr-cli')
        lib_copy('bunkr-gui')
    end
end
