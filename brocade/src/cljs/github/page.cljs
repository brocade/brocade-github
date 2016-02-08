(ns github.page
  (:require
    [sablono.core :as sab :include-macros true]
    [goog.dom :as gdom]
    [om.dom :as dom]
    [om.next :as om :refer-macros [defui]]
    [ajax.core :refer [GET] :as ajax]
    [github.state]
    [cljs.core.async :refer [put! chan <! >! close!]]
    )
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(declare git-cards)
(declare footer-body)
(declare footer-head)

;; (def repo-uri "https://raw.githubusercontent.com/gaberger/brocade-github/master/brocade/resources/public/app/app.edn")

;; (defn async-get
;;   [url]
;;   (let [ch (chan)]
;;     (GET url {:handler (fn [resp]
;;                          (put! ch resp))})
;;     ch))


;; Initialize Application State

(defonce app-state
  (atom
    github.state/page-state
    ))

;; (defn send-func
;;   [uri]
;;   {:value  {:app/repo "foo"}})


(defmulti read om/dispatch)

(defmethod read :default
           [{:keys [state] :as env} key params]
           (let [st @state]
                (if-let [[_ value] (find st key)]
                        {:value value}
                        {:value :not-found})))

(defmethod read :app/footer
           [{:keys [state] :as env} key params]
           {:value (:app/footer @state)})

(defmethod read :app/repo
           [{:keys [state ast] :as env} key params]
           {:value (:app/repo @state)})
;;   (go
;;     (let [ch (async-get repo-uri)
;;           response (<! ch)
;;           respvec (cljs.reader/read-string response)]
;;           {:value {:app/repo (:app/repo respvec)}}
;;         )
;;       )


(defn header-template
      [title]
      (sab/html [:header.mdl-layout__header.mdl-layout__header--scroll.mdl-color--primary-dark
                 [:div.brocade-logo.mdl-layout__header-row
                  [:div.mdl-layout-spacer]
                  [:span.mdl-layout-title title]]
                  [:div.mdl-layout__header-row]
                 ])
      )

(defn main-template
      [repos]
      (sab/html
        [:main.mdl-layout__content
         [:div.mdl-layout__tab-panel.is-active {:id "overview"}]
         (git-cards repos)
         ])
      )


(defn repo-template
      [title desc main link]
      (sab/html
        [:div.mdl-cell.mdl-cell--4-col
         [:div.card-square.mdl-card.mdl-shadow--2dp
          ;[:div.mdl-card__title.mdl-card--expand
          [:div.mdl-card__title
            [:h1.mdl-card__title-text 
              [:a {:href link} title]]]
            [:div.mdl-card__subtitle-text 
            desc
            ]
          ]
         ]
        ))


;;  <div class="mdl-card__actions mdl-card--border">
;;     <a class="mdl-button mdl-button--colored mdl-js-button mdl-js-ripple-effect">
;;       Add to Calendar
;;     </a>
;;     <div class="mdl-layout-spacer"></div>
;;     <i class="material-icons">event</i>
;;   </div>



(defn git-cards
      [repos]
      (let [c (count repos)]
           (sab/html [:section.section--center
                      [:div.git-cards.mdl-grid
                       (map #(repo-template
                                 (get-in % [:repo :name])
                                 (get-in % [:repo :description])
                                 (get-in % [:repo :author])
                                 (get-in % [:repo :html_url])
                               ) repos)

                       ]]))
      )


(defn footer-template
      [coll]
      (sab/html
        [:footer.mdl-mega-footer
         [:div.mdl-mega-footer__middle-section
          (footer-head coll)
          ]
         ]
        )
      )


(defn footer-head
      [coll]
      (map (fn [{:keys [heading items checked?]}]
               [:div.mdl-mega-footer__drop-down-section
                [:input.mdl-mega-footer__heading-checkbox {:type "checkbox"}]
                [:h1.mdl-mega-footer__heading heading]
                (footer-body items)])
           coll)
      )

(defn footer-body
      [items]
      (sab/html [:ul.mdl-mega-footer__link-list
                 (map
                   (fn [{:keys [title href]}]
                       [:li [:a {href href} title]])
                   items)]))


(defui ^:once Page
  static om/IQuery
  (query [this]
         [:app/title :app/footer :app/repo]
    ;{:github/root (om/get-query Repo)}
         )

    ;[:app/title :app/footer :github/root])
       Object
       (render [this]
               (let [{:keys [app/title app/footer app/repo]} (om/props this)]
                    (sab/html
                      [:div.mdl-layout.mdl-js-layout.mdl-layout--fixed-header
                       (header-template title)
                       (main-template repo)
                       (footer-template footer)]
                      ))))

(def parser (om/parser {:read read}))


;; (print (parser {:state app-state} [:app/repo]))
;; (parser {:state app-state} (om/get-query Repo))
;; (parser {:state app-state} (om/get-query Page))



;; (defui Repo
;;   static om/IQuery
;;   (query [_]
;;     '[:app/repo])
;;   Object
;;   (render [this]
;;     (let [{:keys [app/repo]} (om/props this)]
;;       (sab/html [:div
;;                  [:h2 "test"]
;;                  [:p repo]])
;;         )))


(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (om/parser {:read read})
;;      :send (async-get repo-uri)
;;      :remotes [:remote :repo]
     }))


(defn init []
  (if (nil? @app-state)
    (let [target (js/document.getElementById "app")]
      (om/add-root! reconciler Page target)
      (reset! app-state RootComponent))
    (om/force-root-render! reconciler)))

(init)



