(ns mp4-to-mp3
  (:require [babashka.tasks :as tasks :refer [shell]]
            [babashka.fs :as fs]
            [clojure.string :as string]))

(defn -main
  "<in> <out>"
  [& [in out]]
  (let [out (or out "mp4-to-mp3.mp3")
        out (if-not (string/ends-with? out ".mp3") (str out ".mp3") out)
        cmd (format "ffmpeg -i %s -f mp3 -vn %s" in out)]
    (prn {:in in :out out})
    (assert (seq (fs/which "ffmpeg")) "not found bin 'ffmpeg'")
    (assert (and in out))
    (println :cmd cmd)
    (shell {:out *out*} cmd)))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
