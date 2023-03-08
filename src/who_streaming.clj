(ns who-streaming
  (:require [etaoin.api :as e]
            [clojure.core.async :as a]
            [babashka.tasks :refer [shell]]
            [babashka.cli :as cli]))

(def spec
  {:go {:coerce :keyword}
   :help {:alias :h}})

(def live-stream-list {:sccc {:url "https://www.huya.com/123888" :platform :huya}
                       :maybe {:url "https://www.huya.com/211888" :platform :huya}
                       :ame {:url "https://live.bilibili.com/25836285" :platform :bilibili}
                       :azi {:url "https://live.bilibili.com/510" :platform :bilibili}})

(defn huya
  [who]
  (let [title (e/with-chrome-headless driver
                (e/go driver (get-in live-stream-list [who :url]))
                (e/wait-visible driver {:class :host-title})
                (e/get-element-text driver {:class :host-title}))]
    title))

(defn bilibili
  [who]
  (let [[status title] (e/with-chrome-headless driver
                         (e/go driver (get-in live-stream-list [who :url]))
                         (try (e/wait-exists driver [:player-ctnr {:tag :iframe}])
                              (e/switch-frame-first driver)
                              (catch Exception _))
                         (e/wait-visible driver {:fn/has-class :live-skin-main-text})
                         [(e/get-element-text driver {:fn/has-class :live-status})
                          (e/get-element-text driver {:fn/has-class :live-skin-main-text})])]
    (str "[" status "]" title)))

(defmacro no-log
  [& body]
  `(binding [*out* (java.io.StringWriter.)]
     ~@body))

(defn go-to
  [who]
  (shell (format "open -n -a \"Google Chrome\" --args  \"%s\"" (get-in live-stream-list [who :url]))))

(defn -main
  [& _]
  (let [{:keys [go help]} (cli/parse-opts *command-line-args* {:spec spec})]
    (cond
      (or help
          (and (some? go)
               (not (contains? (set (keys live-stream-list)) go))))
      (do (println (cli/format-opts {:spec spec}))
          (println live-stream-list))

      go
      (go-to go)

      :else
      (let [result-ch (a/chan 100)
            *count    (atom (count live-stream-list))
            done-ch (a/chan)]
        (doseq [[who {:keys [platform]}] live-stream-list]
          (a/go
            (let [title (case platform
                          :huya     (no-log (huya who))
                          :bilibili (no-log (bilibili who)))]
              (a/>! result-ch {who title})
              (swap! *count dec)
              (when (zero? @*count)
                (a/close! done-ch)))))
        (a/go-loop []
          (println (a/<! result-ch))
          (recur))
        (a/<!! done-ch)))))
