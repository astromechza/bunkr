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
JAR_JSON_SIMPLE = "com.googlecode.json-simple:json-simple:jar:1.1.1"
JAR_ARGPARSE = "net.sourceforge.argparse4j:argparse4j:jar:0.6.0"

# entry point
MAIN_CLASS = "#{PROJECT_GROUP}.cli.CLI"

# define main project
define PROJECT_NAME, layout: layout do
    project.version = PROJECT_VERSION
    project.group = PROJECT_GROUP

    test.with JAR_JUNIT
    compile.with JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE
    package(:jar).merge(compile.dependencies).exclude('META-INF/BCKEY.*')
    package(:jar).with(manifest: {'Main-Class' => MAIN_CLASS})
    run.using main: MAIN_CLASS

    clean do
        if Dir.exists? project.path_to('lib')
            puts "Deleting 'lib' directory"
            FileUtils.rm_r project.path_to('lib')
        end
    end

    build do
        version_dat_file = _('target/main/classes/version.dat')
        puts 'Writing ' + version_dat_file
        GIT_HASH = `git rev-parse HEAD`
        GIT_DATE = `git --no-pager log -n 1 --date=iso-strict --format="%cd"`
        File.open(version_dat_file, 'w') do |f|
            f.puts PROJECT_VERSION
            f.puts GIT_DATE
            f.puts GIT_HASH
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
