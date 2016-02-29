(ns github.core
  (:require [clojure.pprint :refer [pprint]]
            [tentacles.repos :as repos]
            [tentacles.users :as users]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def sites [{:user "brocade" :repo "brocade"}
            {:user "BRCDcomm" :repo "BRCDcomm"}])

(defn edn->json
  "Convert EDN to JSON"
  [coll]
  (map #(json/pprint %) coll)
  )

(defn get-repos
  "Return collection of repos based on user/org"
  [user token]
  (pmap #(select-keys % [:name :html_url :forks :description])
        (repos/user-repos user {:oauth-token token}))
  )

(defn get-lastcommits
  "Return a collection containing the sha, date, name and url of lastcommit for user and a collection of repos"
  [user repo token]
  (let [lastcommit (first (repos/commits user repo {:oauth-token token}))
        sha (:sha lastcommit)
        date (get-in lastcommit [:commit :author :date])
        author (get-in lastcommit [:commit :author :name])
        email (get-in lastcommit [:commit :author :email])
        ]
    {:lastcommit sha :date date :author author :email email}
    ))

(defn get-repo-contributors
  "Return a list of repo contributors"
  [user repo token]
  (map #(:login %) (repos/contributors user repo {:oauth-token token})
       ))

(defn get-contributors-email
  "Take a list of users and return a map of user names and emails"
  [users token]
  (reduce (fn [output user]
            (let [record (users/user user {:oauth-token token})
                  email (:email record)
                  company (:company record)
                  ]
              (conj output {:email email :name user :company company})
              )
            )
          []
          users
          ))

(defn get-members
  "Return a list of collaborators"
  [user repos token]
  (map #(repos/collaborators user (:name %) {:oauth-token token}) repos)
  )

(defn assemble-report
  [repo contribs commits]
  (let [a (assoc {} :commiters contribs)
        b (conj repo a commits)
        r (assoc {} :repo b)]
    r
    )
  )

(def repo-filter (vec ["pybvc-archive-DO-NOT-USE", "brocade-github", "brocade.github.io"]))

(defn re-remove
  [f-coll coll]
  (if (empty? f-coll)
    coll
    (let [lazy-res (remove #(= (:name %) (first f-coll)) coll)]
      (recur (rest f-coll) lazy-res)))
  )

(defn filter-repos
  [repo token]
  (let [repos (into [] (get-repos repo token))
        filter-repos (re-remove repo-filter repos)

        ]
    filter-repos
    ))

(defn report
  "Return a collection of repo commits and contributors"
  [site token]
  ;(let [repos (get-repos site token)
  (let [repos (filter-repos site token)
        report
        (map (fn [repo]
               (let [name (:name repo)
                     c (get-repo-contributors site name token)
                     contribs (get-contributors-email c token)
                     commits (get-lastcommits site name token)

                     ]

                 (assemble-report repo contribs commits))
               )
             repos
             )]
    ; (assoc {} :app/repo (into [] report))
    (into [] report)

    ))

(defn -main [site]
  (let [token (env :gittoken)]
    (if (nil? token)
      (do
        (println "Please set env variable gittoken")
        (System/exit 0)))
    (spit "resources/public/app/app.edn" (report site token))))



