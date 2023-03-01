(ns aws-log
  (:require [babashka.fs :as fs]
            [babashka.tasks :as tasks :refer [shell]]
            [babashka.cli :as cli]
            [clojure.string :as string]
            [common]))

(def spec
  {:log-stream-names {:alias :n
                      :coerce []
                      :default []}
   :start-time {:alias :s
                :desc "RFC3339"}
   :end-time {:alias :e
              :desc "RFC3339"}
   :filter-pattern {:alias :f}
   :env {:default "default"
         :desc "value: default or prod"
         :validate #{"default" "prod"}}
   :help {:alias :h}})

(defn -main
  [& _]
  (assert (fs/which "aws") "Need aws cli installed")
  (let [{:keys [args opts] :as cmd-args} (cli/parse-args *command-line-args* {:spec spec})
        [log-group-name]    args]
    (prn cmd-args)
    (cond
      (:help opts)
      (println (cli/format-opts {:spec spec}))

      (empty? log-group-name)
      (println "required argument: log-group-name")

      :else
      (let [cmd
            (string/join
             " "
             (cond-> ["aws logs filter-log-events --log-group-name" log-group-name]
               (:log-stream-names opts) (concat ["--log-stream-names"] (:log-stream-names opts))
               (:start-time opts)       (concat ["--start-time" (common/date-string->ms (:start-time opts))])
               (:end-time opts)         (concat ["--end-time" (common/date-string->ms (:end-time opts))])
               (:filter-pattern opts)   (concat ["--filter-pattern" (str "\"" (:filter-pattern opts) "\"")])
               true                     (concat ["--no-paginate" "--no-cli-pager"])))]
        (println :cmd cmd)
        (shell {:env {"AWS_PROFILE" (:env opts)}} cmd)))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
