require 'fileutils'
require 'buildr/jacoco'

require_relative './build_scripts/utils'
require_relative './build_scripts/licenser'
require_relative './build_scripts/build_cli_demo'
require_relative './build_scripts/examplebuilder'
require_relative './build_scripts/mac_app'

PROJECT_NAME = 'bunkr'
PROJECT_GROUP = "org.#{PROJECT_NAME}"
PROJECT_VERSION = '0.11.0'
COMPATIBLE_PROJECT_VERSION = '0.10.0'

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
JAR_OKIO = 'com.squareup.okio:okio:jar:1.6.0'
JAR_OKHTTP = 'com.squareup.okhttp3:okhttp:jar:3.0.1'

JAR_RICHTEXT = 'org.fxmisc.richtext:richtextfx:jar:0.6.10'
JAR_REACTFX = 'org.reactfx:reactfx:jar:2.0-M4u1'
JAR_UNDOFX = 'org.fxmisc.undo:undofx:jar:1.2'
JAR_FLOWLESS = 'org.fxmisc.flowless:flowless:jar:0.4.5'
JAR_WELLBEHAVED = 'org.fxmisc.wellbehaved:wellbehavedfx:jar:0.1.1'

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
            puts "Cleaning old classes folder.."
            system("rm -rfv '#{project('bunkr-cli').path_to('target', 'main')}'")
            proguard_wrap('bunkr-cli')
        end
    end

    define 'bunkr-gui', layout: layout do
        test.with JAR_JUNIT, project('bunkr-core').test.compile.target
        jacoco.generate_html = true
        compile.with JAR_BC, JAR_ARGPARSE, JAR_MARKDOWN, JAR_JSON_SIMPLE, JAR_OKIO, JAR_OKHTTP,
                     JAR_UNDOFX, JAR_FLOWLESS, JAR_REACTFX, JAR_RICHTEXT, JAR_WELLBEHAVED, project('bunkr-core')
        compile.using(source: '1.8', target: '1.8', lint: 'all')
        package(:jar, id: 'bunkr-gui').merge(compile.dependencies).exclude('META-INF/BCKEY.*')
        package(:jar, id: 'bunkr-gui').with(manifest: {'Main-Class' => GUI_MAIN_CLASS})
        run.using main: [GUI_MAIN_CLASS, '--logging']

        build do
            write_version_file_for_project('bunkr-gui')
            write_resources_for_project('bunkr-gui')
        end

        task build_release: [:package] do
            puts "Cleaning old classes folder.."
            system("rm -rfv '#{project('bunkr-gui').path_to('target', 'main')}'")
            proguard_wrap('bunkr-gui')
        end

        # ant-based task to generate Mac OSX app
        task :build_mac_app do

            output_dir = project.path_to('target', 'app')
            puts "Removing #{output_dir}"
            system("rm -rfv #{output_dir}")
            puts "Creating #{output_dir}/dist"
            system("mkdir -pv #{output_dir}/dist")

            puts "Writing build.xml"
            File.open("#{output_dir}/build.xml", 'w') do |f|
                f.puts make_mac_app_xml(output_dir)
            end

            if system("cd #{output_dir}; ant")
                puts "Built #{output_dir}/bunkr-#{project.version}.app"
                puts "Size #{system("ls -lh #{output_dir}")}"
            else
                puts "Error while building app!"
                exit! 1
            end
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

    # Task to build a nice demo archive
    task generate_example_archive: ['bunkr-cli:package'] do
        target_archive = project.path_to('example.bunkr')
        jarfile = project('bunkr-cli').path_to('target', "bunkr-cli-#{project.version}.jar")
        puts "Generating example archive for testing with.."

        ExampleBuilder.run jarfile, target_archive
    end
end
