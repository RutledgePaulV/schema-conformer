(defproject org.clojars.rutledgepaulv/schema-conformer "0.1.3"

  :description
  "A library for configurable conforming of data according to prismatic schemas."

  :url
  "https://github.com/rutledgepaulv/schema-conformer"

  :license
  {:name "MIT License" :url "http://opensource.org/licenses/MIT" :year 2020 :key "mit"}

  :scm
  {:name "git" :url "https://github.com/rutledgepaulv/schema-conformer"}

  :pom-addition
  [:developers
   [:developer
    [:name "Paul Rutledge"]
    [:url "https://github.com/rutledgepaulv"]
    [:email "rutledgepaulv@gmail.com"]
    [:timezone "-5"]]]

  :deploy-repositories
  [["releases" :clojars]
   ["snapshots" :clojars]]

  :dependencies
  [[org.clojure/clojure "1.10.1"]
   [prismatic/schema "1.1.12"]]

  :plugins
  [[lein-cloverage "1.1.2"]]

  :repl-options
  {:init-ns schema-conformer.core}

  :profiles
  {:test {:dependencies [[clj-time "0.15.2" :scope "test"]]}})
