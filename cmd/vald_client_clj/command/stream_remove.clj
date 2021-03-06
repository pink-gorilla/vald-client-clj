(ns vald-client-clj.command.stream-remove
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "read as json"
    :id :json?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] stream-remove [SUBOPTIONS] IDs"
        ""
        "Remove multiple IDs."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json? elapsed-time?]} options
        read-string (if json?
                      util/read-json
                      edn/read-string)]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [ids (-> (or (first arguments)
                        (util/read-from-stdin))
                    (read-string))
            f (fn []
                (-> client
                    (vald/stream-remove println ids)
                    (deref)))
            res (if elapsed-time?
                  (time (f))
                  (f))]
        (if (:error res)
          (throw (:error res))
          (->> res
               (:count)
               (str "removed: ")
               (println)))))))
