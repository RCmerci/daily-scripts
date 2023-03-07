(ns mp4-to-mp3
  (:require [babashka.tasks :as tasks :refer [shell]]
            [babashka.fs :as fs]
            [clojure.string :as string]
            [babashka.cli :as cli]))

(def spec
  {:in {:alias :i
        :require true}
   :out {:alias :o
         :default "mp4-to-mp3.mp3"}
   :help {:alias :h}})

(defn -main
  [& _]
  (let [{{:keys [in out help]} :opts :as args}
        (cli/parse-args *command-line-args* {:spec spec
                                             :error-fn
                                             (fn [{:keys [msg]}]
                                               (println (cli/format-opts {:spec spec}))
                                               (println msg)
                                               (System/exit 0))})
        _ (prn :out out)
        out (if-not (string/ends-with? out ".mp3") (str out ".mp3") out)
        cmd (format "ffmpeg -i %s -f mp3 -vn %s" in out)]
    (when help
      (println (cli/format-opts {:spec spec}))
      (System/exit 0))
    (prn args)
    (assert (seq (fs/which "ffmpeg")) "not found bin 'ffmpeg'")
    (assert (and in out))
    (println :cmd cmd)
    (shell {:out *out*} cmd)))
