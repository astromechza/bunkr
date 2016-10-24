require 'fileutils'

def make_mac_app_xml(working_dir)
    project_name = 'bunkr-gui'
    main_class = 'org.bunkr.gui.GuiEntryPoint'
    release_jar = project(project_name).path_to('target', "#{project_name}-#{project.version}-release.jar")

    unless File.exist? release_jar then
        puts "Error: release jar #{release_jar} does not exist!"
        exit! 1
    end

    system("cp #{release_jar} #{working_dir}/dist")

    java_home = ENV['JAVA_HOME']

    xml_template = <<-EOF
<?xml version="1.0" encoding="UTF-8"?>
<project name="Bunkr"
         default="default"
         basedir="."
         xmlns:fx="javafx:com.sun.javafx.tools.ant">
<target name="default">
    <taskdef resource="com/sun/javafx/tools/ant/antlib.xml"
                uri="javafx:com.sun.javafx.tools.ant"
                classpath="#{File.join(java_home, "lib", "ant-javafx.jar")}"/>
    <fx:deploy nativeBundles="dmg" outdir="dist" outfile="thing" verbose="true">
            <fx:info title="Bunkr"
                  vendor="AstromechZA"/>
            <fx:application name="Bunkr" mainClass="#{main_class}"/>
            <fx:resources>
               <fx:fileset dir="dist" includes="#{project_name}-#{project.version}-release.jar">
               </fx:fileset>
            </fx:resources>
      </fx:deploy>
</target>
</project>
EOF
    xml_template
end
