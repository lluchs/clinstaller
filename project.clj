(defproject clinstaller "0.1.0-SNAPSHOT"
  :description "Downloads and extracts Clinfinity"
  :url "https://github.com/lluchs/Clinfinity"
  :repositories {"tmatesoft.com" "http://maven.tmatesoft.com/content/repositories/releases/"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [seesaw "1.4.2"]
                 [org.tmatesoft.svnkit/svnkit "1.7.5-v1"]]
  :main clinstaller.core)
