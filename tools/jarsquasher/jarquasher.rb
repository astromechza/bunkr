#!/usr/bin/env ruby

require 'tempfile'

# first get target jar argument
target_jar = ARGV[0]
if target_jar.nil? or target_jar.strip == ''
  puts 'Target jar must be specified as first argument'
  exit! 1
end

# check if it exists.. generally important
unless File.exists? target_jar
  puts 'Target jar does not exist'
  exit! 1
end

# second arg should be a path to proguard script
proguard_target_script = ARGV[1]
if proguard_target_script.nil? or proguard_target_script.strip == ''
  puts 'Second argument must be the Proguard script'
  exit! 1
end

# check if it exists.. generally important
unless File.exists? proguard_target_script
  puts "Proguard script {proguard_target_script} does not exist"
  exit! 1
end

# build final jar name
original_name = File.basename target_jar
target_directory = File.dirname target_jar
final_path = File.join target_directory, original_name.sub('.jar', '-release.jar')

# remove old jar
if File.exists? final_path
  puts "Deleting existing file #{final_path}"
  File.delete final_path
end

# build proguard file
proguard_file = Tempfile.new 'bunkr'
file_content = <<-EOF
-injars       #{File.absolute_path(target_jar)}
-outjars      #{File.absolute_path(final_path)}
-libraryjars  <java.home>/lib/rt.jar

-keep public class org.bunkr.cli.CLI {
    public static void main(java.lang.String[]);
}

-dontwarn javax.crypto.**
-dontwarn org.bouncycastle.jcajce.provider.asymmetric.util.BaseCipherSpi
-keepattributes *Annotation*
-keepattributes Signature

-dontoptimize
-dontobfuscate
EOF

File.open(proguard_file.path, 'w') do |f|
  f.write(file_content)
end

# useful method
def to_filesize(value)
{
  'B'  => 1024,
  'KB' => 1024 * 1024,
  'MB' => 1024 * 1024 * 1024,
}.each_pair { |e, s| return "#{(value.to_f / (s / 1024)).round(2)}#{e}" if value < s }
end

# run it!
puts 'Building squashed jar...'
IO.popen([proguard_target_script, "@#{proguard_file.path}"], 'r', err: [:child, :out]) do |io|
  puts io.read
  code = ($?.to_i >> 8)
  if code != 0
    puts "FAILED!!! code: #{code}"
    exit! code
  else
    size_before = File.size File.absolute_path(target_jar)
    size_after = File.size File.absolute_path(final_path)
    puts "SUCCESS. Jar created: #{final_path}. Final size: #{to_filesize size_after} Ratio: #{(size_after.to_f / size_before).round(2)}%"
  end
end
