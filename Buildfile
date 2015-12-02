require 'fileutils'
require 'buildr/jacoco'

PROJECT_NAME = 'bunkr_beta'
PROJECT_GROUP = "com.#{PROJECT_NAME}"
PROJECT_VERSION = '0.1'

# where to pull maven dependencies from
repositories.remote << 'https://repo.maven.apache.org/maven2'
repositories.remote << 'https://repo1.maven.org/maven2'

# directory structure
layout = Layout.new
layout[:source, :main, :java] = 'src'
layout[:source, :test, :java] = 'tests'

# dependencies
JAR_JUNIT = "junit:junit:jar:4.12"
JAR_BC = "org.bouncycastle:bcprov-jdk15on:jar:1.53"
JAR_JSON_CORE = "com.fasterxml.jackson.core:jackson-core:jar:2.6.3"
JAR_JSON_ANNOT = "com.fasterxml.jackson.core:jackson-annotations:jar:2.6.3"
JAR_JSON_DB = "com.fasterxml.jackson.core:jackson-databind:jar:2.6.3"
JAR_ARGPARSE = "net.sourceforge.argparse4j:argparse4j:jar:0.6.0"

# entry point
MAIN_CLASS = "#{PROJECT_GROUP}.cli.CLI"

# define main project
define PROJECT_NAME, layout: layout do
    project.version = PROJECT_VERSION
    project.group = PROJECT_GROUP

    test.with JAR_JUNIT
    compile.with JAR_BC, JAR_JSON_CORE, JAR_JSON_ANNOT, JAR_JSON_DB, JAR_ARGPARSE
    package(:jar).merge(compile.dependencies).exclude('META-INF/BCKEY.*')
    package(:jar).with(manifest: {'Main-Class' => MAIN_CLASS})
    run.using main: MAIN_CLASS

    clean do
        if Dir.exists? project.path_to('lib')
            puts "Deleting 'lib' directory"
            FileUtils.rm_r project.path_to('lib')
        end
    end

    task :pulllibs do
        unless Dir.exists? project.path_to('lib')
            puts "Creating 'lib' directory"
            Dir.mkdir project.path_to('lib')
        end
        project.compile.dependencies.each do |t|
            if t.to_s.match(/\.jar$/) then
                unless File.exists? File.join(project.path_to('lib'), File.basename(t.to_s))
                    puts "Copying #{t} to 'lib' directory"
                    cp t.to_s, project.path_to('lib')
                end
            end
        end
        project.test.dependencies.each do |t|
            if t.to_s.match(/\.jar$/) then
                unless File.exists? File.join(project.path_to('lib'), File.basename(t.to_s))
                    puts "Copying #{t} to 'lib' directory"
                    cp t.to_s, project.path_to('lib')
                end
            end
        end
    end
end
