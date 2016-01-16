require 'fileutils'
require 'buildr/jacoco'

require_relative './build_scripts/utils'
require_relative './build_scripts/licenser'
require_relative './build_scripts/build_cli_demo'

PROJECT_NAME = 'bunkr'
PROJECT_GROUP = "org.#{PROJECT_NAME}"
PROJECT_VERSION = '0.9.0'
COMPATIBLE_PROJECT_VERSION = '0.9.0'

# where to pull maven dependencies from
repositories.remote << 'https://repo.maven.apache.org/maven2'
repositories.remote << 'https://repo1.maven.org/maven2'

# dependencies
JAR_JUNIT = 'junit:junit:jar:4.12'
JAR_BC = 'org.bouncycastle:bcprov-jdk15on:jar:1.53'
JAR_JSON_SIMPLE = 'com.googlecode.json-simple:json-simple:jar:1.1.1'
JAR_ARGPARSE = 'net.sourceforge.argparse4j:argparse4j:jar:0.6.0'
JAR_MARKDOWN = 'org.commonjava.googlecode.markdown4j:markdown4j:jar:2.2-cj-1.0'
JAR_PROGUARD = 'net.sf.proguard:proguard-base:jar:5.2.1'

layout = Layout.new
layout[:source, :main, :java] = 'src'
layout[:source, :test, :java] = 'tests'

CLI_MAIN_CLASS = 'org.bunkr.cli.CLI'
GUI_MAIN_CLASS = 'org.bunkr.gui.GuiEntryPoint'

# define main project
define PROJECT_NAME do

    project.version = PROJECT_VERSION
    project.group = PROJECT_GROUP

    # ----------------------------------------------------------------------------------------

    define 'bunkr-core', layout: layout do
        test.with JAR_JUNIT
        jacoco.generate_html = true
        compile.with JAR_BC, JAR_JSON_SIMPLE
        compile.using(source: '1.8', target: '1.8', lint: 'all')
        package(:jar, id: 'bunkr-core').merge(compile.dependencies).exclude('META-INF/BCKEY.*')
        package(:jar, id: 'bunkr-core')

        build do
            write_version_file_for_project('bunkr-core')
        end
    end

    define 'bunkr-cli', layout: layout do
        test.with JAR_JUNIT, project('bunkr-core').test.compile.target
        jacoco.generate_html = true
        compile.with JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE, project('bunkr-core')
        compile.using(source: '1.8', target: '1.8', lint: 'all')
        package(:jar, id: 'bunkr-cli').merge(compile.dependencies).exclude('META-INF/BCKEY.*')
        package(:jar, id: 'bunkr-cli').with(manifest: {'Main-Class' => CLI_MAIN_CLASS})
        run.using main: CLI_MAIN_CLASS

        build do
            write_version_file_for_project('bunkr-cli')
        end

        task generate_demo: ['bunkr-cli:package'] do
            target_file = project.path_to('..', 'CLI_DEMO.md')
            jarfile = project('bunkr-cli').path_to('target', "bunkr-cli-#{project.version}.jar")
            puts "Regenerating #{target_file}"
            CLIDemoBuilder.run jarfile, target_file
        end

        task build_release: [:package] do
            proguard_wrap('bunkr-cli')
        end
    end

    define 'bunkr-gui', layout: layout do
        test.with JAR_JUNIT, project('bunkr-core').test.compile.target
        jacoco.generate_html = true
        compile.with JAR_BC, JAR_ARGPARSE, JAR_MARKDOWN, JAR_JSON_SIMPLE, project('bunkr-core')
        compile.using(source: '1.8', target: '1.8', lint: 'all')
        package(:jar, id: 'bunkr-gui').merge(compile.dependencies).exclude('META-INF/BCKEY.*')
        package(:jar, id: 'bunkr-gui').with(manifest: {'Main-Class' => GUI_MAIN_CLASS})
        run.using main: [GUI_MAIN_CLASS, '--logging']

        build do
            write_version_file_for_project('bunkr-gui')
            write_resources_for_project('bunkr-gui')
        end

        task build_release: [:package] do
            proguard_wrap('bunkr-gui')
        end
    end

    # ----------------------------------------------------------------------------------------

    # Task to copy dependency jars from maven source to lib folder so that IntelliJ can index them.
    task pull_libs: [:artifacts] do
        puts 'Copying dependency artifacts to lib directory..'
        unless Dir.exists? project.path_to('lib')
            puts 'Creating lib directory'
            Dir.mkdir project.path_to('lib')
        end
        lib_copy 'bunkr-core'
        lib_copy 'bunkr-cli'
        lib_copy 'bunkr-gui'
        puts '-----'
        puts Dir.entries(project.path_to('lib')).select {|f| !File.directory? f}
        puts '-----'
        puts 'Done'
    end

    # Task to apply license text to all source files
    task :apply_license do
        puts 'Applying license to core source files...'
        fix_license_in project.path_to('bunkr-core')
        puts 'Applying license to cli source files...'
        fix_license_in project.path_to('bunkr-cli')
        puts 'Applying license to gui source files...'
        fix_license_in project.path_to('bunkr-gui')
        puts 'Done'
    end
end
