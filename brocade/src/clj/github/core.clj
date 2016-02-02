(ns github.core
    (:require [clojure.pprint :refer [pprint]]
      [tentacles.repos :as repos]
      [tentacles.users :as users]
      [clj-time.format :as f]
      [clj-time.core :as t]
      [clojure.data.json :as json]
      [environ.core :refer [env]]
      [immuconf.config]))

(def token (immuconf.config/get (immuconf.config/load "resources/config/config.edn") :git-token))

(def sites [{:user "brocade" :repo "brocade"}
            {:user "BRCDcomm" :repo "BRCDcomm"}])

(defn edn->json
      "Convert EDN to JSON"
      [coll]
      (map #(json/pprint %) coll)
      )


(defn get-repos
      "Return collection of repos based on user/org"
      [user]
      (pmap #(select-keys % [:name :html_url :forks :description])
            (repos/user-repos user {:oauth-token token}))
      )


(defn get-lastcommits
      "Return a collection containing the sha, date, name and url of lastcommit for user and a collection of repos"
      [user repo]
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
      [user repo]
      (map #(:login %) (repos/contributors user repo {:oauth-token token})
           ))


(defn get-contributors-email
      "Take a list of users and return a map of user names and emails"
      [users]
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
      [user repos]
      (map #(repos/collaborators user (:name %) {:oauth-token token}) repos))


(defn assemble-report
      [repo contribs commits]
      (let [root (assoc {} :github/root {:repo repo})
            contribs (assoc root :contributors contribs)
            report (assoc contribs :last-commit commits)]
           report
           )
      )

(defn report
      "Return a collection of last-commits over a list of defined repositories"
      [site]
      (let [repos (get-repos site)]
           (map (fn [repo]
                    (let [name (:name repo)
                          contribs (get-repo-contributors site name)
                          contrib-emails (get-contributors-email contribs)
                          last-commits (get-lastcommits site name)
                          ]
                         (assemble-report repo contrib-emails last-commits)
                         )
                    )
                repos
                )
           )
      )
(defn write-func
  []
  (let [opening '(ns github.repo)
        mydef '(def repo-state)]

        )
  )


(defn -main [site]
  (let [git-state (into [] (report site))]
      (spit "resources/public/app/app.edn" git-state)))


