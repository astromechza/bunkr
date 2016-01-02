require 'fileutils'
require 'buildr/jacoco'

PROJECT_NAME = 'bunkr'
PROJECT_GROUP = "org.#{PROJECT_NAME}"
PROJECT_VERSION = '0.5.0'
COMPATIBLE_PROJECT_VERSION = '0.3.0'

# where to pull maven dependencies from
repositories.remote << 'https://repo.maven.apache.org/maven2'
repositories.remote << 'https://repo1.maven.org/maven2'

# dependencies
JAR_JUNIT = "junit:junit:jar:4.12"
JAR_BC = "org.bouncycastle:bcprov-jdk15on:jar:1.53"
JAR_JSON_SIMPLE = "com.googlecode.json-simple:json-simple:jar:1.1.1"
JAR_ARGPARSE = "net.sourceforge.argparse4j:argparse4j:jar:0.6.0"
JAR_MARKDOWN = "org.commonjava.googlecode.markdown4j:markdown4j:jar:2.2-cj-1.0"

layout = Layout.new
layout[:source, :main, :java] = 'src'
layout[:source, :test, :java] = 'tests'

CLI_MAIN_CLASS = "org.bunkr.cli.CLI"
GUI_MAIN_CLASS = "org.bunkr.gui.GuiEntryPoint"

def write_version_file_for_project(project_name)
    version_dat_file = project(project_name)._('target/main/classes/version.dat')
    puts 'Writing ' + version_dat_file
    git_hash = `git rev-parse HEAD`
    git_date = `git --no-pager log -n 1 --date=iso-strict --format="%cd"`
    File.open(version_dat_file, 'w') do |f|
        f.puts PROJECT_VERSION
        f.puts COMPATIBLE_PROJECT_VERSION
        f.puts git_date
        f.puts git_hash
    end
end

def write_resources_for_project(project_name)
    puts "Copying GUI resources to target for #{project_name}..."
    FileUtils.cp_r project(project_name)._('resources/.'), project(project_name)._('target/main/classes'), verbose: true
end

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
    end

    define 'bunkr-gui', layout: layout do
        test.with JAR_JUNIT, project('bunkr-core').test.compile.target
        jacoco.generate_html = true
        compile.with JAR_BC, JAR_ARGPARSE, JAR_MARKDOWN, JAR_JSON_SIMPLE, project('bunkr-core')
        compile.using(source: '1.8', target: '1.8', lint: 'all')
        package(:jar, id: 'bunkr-gui').merge(compile.dependencies).exclude('META-INF/BCKEY.*')
        package(:jar, id: 'bunkr-gui').with(manifest: {'Main-Class' => GUI_MAIN_CLASS})
        run.using main: GUI_MAIN_CLASS

        build do
            write_version_file_for_project('bunkr-gui')
            write_resources_for_project('bunkr-gui')
        end
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
