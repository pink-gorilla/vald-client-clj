(ns vald-client-clj.command.stream-get-object
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "read and write as json"
    :id :json?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] stream-get-object [SUBOPTIONS] IDs"
        ""
        "Get object info of multiple IDs."
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
                      edn/read-string)
        writer (if json?
                 (comp println util/->json)
                 (comp println util/->edn))]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [ids (-> (or (first arguments)
                        (util/read-from-stdin))
                    (read-string))
            f (fn []
                (-> client
                    (vald/stream-get-object writer ids)
                    (deref)))
            res (if elapsed-time?
                  (time (f))
                  (f))]
        (when (:error res)
          (throw (:error res)))))))
