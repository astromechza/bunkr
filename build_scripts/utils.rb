require 'fileutils'

def to_filesize(value)
    {
        'B'  => 1024,
        'KB' => 1024 * 1024,
        'MB' => 1024 * 1024 * 1024,
    }.each_pair { |e, s| return "#{(value.to_f / (s / 1024)).round(2)}#{e}" if value < s }
end

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

def lib_copy(project_name)
    project(project_name).compile.dependencies.each do |t|
        if t.to_s.include? 'bunkr-core-'
            puts "Ignoring #{t}"
            next
        end
        if t.to_s.match(/\.jar$/)
            unless File.exists? File.join(project.path_to('lib'), File.basename(t.to_s))
                puts "Copying #{t} to 'lib' directory"
                cp t.to_s, project.path_to('lib')
            end
        end
    end
    project(project_name).test.dependencies.each do |t|
        if t.to_s.include? 'bunkr-core-'
            puts "Ignoring #{t}"
            next
        end
        if t.to_s.match(/\.jar$/)
            unless File.exists? File.join(project.path_to('lib'), File.basename(t.to_s))
                puts "Copying #{t} to 'lib' directory"
                cp t.to_s, project.path_to('lib')
            end
        end
    end
end

def proguard_wrap(project_name)
    # get proguard jar file
    proguard_jar = artifact(JAR_PROGUARD)

    # download it if needed
    proguard_jar.invoke

    # get config file
    config = project(project_name).path_to('release.proguard')

    # calculate jars
    infile = project(project_name).path_to('target', "#{project_name}-#{project.version}.jar")
    outfile = project(project_name).path_to('target', "#{project_name}-#{project.version}-release.jar")

    unless system("java -jar #{proguard_jar} '@#{config}' -injars '#{infile}' -outjars '#{outfile}'")
        exit! 1
    end

    size_before = File.size File.absolute_path(infile)
    size_after = File.size File.absolute_path(outfile)
    puts "SUCCESS. Jar created: #{outfile}. Final size: #{to_filesize size_after} Ratio: #{(size_after.to_f / size_before).round(2)}%"
end
