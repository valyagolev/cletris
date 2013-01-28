(defproject cletris "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [noir "1.3.0-beta3"]
                           [jayq "2.0.0"]
                           [hiccups "0.2.0"]
                           [org.clojure/math.combinatorics "0.0.3"]]
					  :plugins [[lein-cljsbuild "0.3.0"]] ; cljsbuild plugin
            :cljsbuild {
             :builds [{
             		:source-paths ["src-cljs"]
		            :compiler
    	         		{
              			:output-to "resources/public/js/cljs.js"
			              ;;:optimizations :simple
      			        :pretty-print true
            		  }
             	}]}
            :main cletris.server)

