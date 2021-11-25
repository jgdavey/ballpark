require "rake"
require "rake/clean"

def prompt(*args)
  print(*args)
  STDIN.gets.chomp
end

OUTPUT_JAR = "ballpark.jar"

task :default => :test

file OUTPUT_JAR do |f|
  version = ENV["VERSION"] || prompt("Version (e.g. 1.2.3) > ")
  sh "clojure", "-Xjar", ":version", "\"#{version}\""
end

CLOBBER << "cljs-test-runner-out"

CLEAN << OUTPUT_JAR

desc "Build the whole thing to a jar"
task :jar => OUTPUT_JAR

task :deploy => :jar do
  ENV["CLOJARS_USERNAME"] ||= prompt("Clojars username > ")
  ENV["CLOJARS_PASSWORD"] ||= prompt("Clojars password > ")
  sh "clojure", "-Xdeploy"
end

namespace :deps do
  desc "Check for outdated dependencies"
  task :outdated do
    sh "clojure", "-Sdeps", "{:deps {olical/depot {:mvn/version \"RELEASE\"}}}",
      "-M", "-m", "depot.outdated.main", "--every"
  end
end

namespace :test do
  desc "Run cljs tests"
  task :cljs do
    sh "clojure", "-M:test:test-cljs"
  end

  desc "Run clj tests"
  task :clj do
    sh "clojure", "-M:test:test-clj"
  end

  desc "Run all tests"
  task :all => [:cljs, :clj]
end

task :test => "test:all"
