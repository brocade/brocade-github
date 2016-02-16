(ns github.page
(:require [reagent.core :as reagent]
          ;[reframetest.config]
          [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
          [ajax.core :refer [GET] :as ajax]
          [github.state]
          [devtools.core :as devtools]
          [cljs.core.async :refer [put! chan <! >! close!]]
          [clairvoyant.core :refer-macros [trace-forms]]
          [re-frame-tracer.core :refer [tracer]]
          )
 (:require-macros [reagent.ratom :refer [reaction]]
 									[cljs.core.async.macros :refer [go]]))

; (devtools/enable-feature! :sanity-hints :dirac)
; (devtools/install!)

(enable-console-print!)

(declare git-cards)
(declare header-items)
(declare footer-body)
(declare footer-head)

(def repo-uri "https://raw.githubusercontent.com/gaberger/brocade-github/master/brocade/resources/public/app/app.edn")

(register-handler
  :get-repo             ;; <-- the button dispatched this id
  (fn
    [db _]
    (GET
      repo-uri
      {:handler       #(dispatch [:process-response %1])   ;; further dispatch !!
       :error-handler #(dispatch [:bad-response %1])}) 
       db))


(register-handler               ;; when the GET succeeds 
  :process-response             ;; the GET callback dispatched this event  
  (fn
    [db [_ response]]           ;; extract the response from the dispatch event vector
    (-> db
        (assoc :app/repo (js->clj response)))))  ;; fairly lame processing

(register-handler              
  :bad-response             
  (fn
    [db [_ response]]
    (-> db
        (assoc :app/repo github.state/fail-state)))) 


(register-sub
  :repo
  (fn [db]
  (reaction (:app/repo @db))))


(register-handler                 ;; setup initial state
  :initialize                     ;; usage:  (submit [:initialize])
  (fn
    [db _]
    (merge db nil)))




(defn header-template
      [title items]
        [:nav.brocade-red {:role "navigation"}
                  [:div.nav-wrapper.container
                   [:a.brand-logo {:href "" :id "logo-container"} [:h1.brocade-logo] ]
                   [header-items items]
                   [:a.button-collapse {:data-activates "nav-mobile"} [:i.material-icons "menu"]]
                  ]
                ])

(defn header-items
      [items]
      (fn []
      [:ul.right.hide-on-med-and-down
                 (map
                   (fn [{:keys [title href]}]
                       [:li [:a {:href href} title]])
                   items)
                ]
                [:ul.side-nav {:id "nav-mobile"}
                 (map
                   (fn [{:keys [title href]}]
                       [:li [:a {:href href} title]])
                   items)
                ]))


(defn main-template
      [repos]
        [:div.container
         [:div.section
           [:div.row
             [:div.col.s12
               (git-cards repos)
             ]
           ]
         ]
        ])


(defn repo-template
      [title desc main link]
          [:div.col.s12.m6
            [:div.card.small
             [:div.card-content
              [:span.card-title title]
              [:p desc]
             ]
             [:div.card-action
               [:a {:href link} title]
             ]
            ]
          ]
        )



(defn git-cards
      [repos]
      (let [c (count repos)
            _ (print repos)]
            [:div.row
                       (map #(repo-template
                                 (get-in % [:repo :name])
                                 (get-in % [:repo :description])
                                 (get-in % [:repo :author])
                                 (get-in % [:repo :html_url])
                               ) repos)
                       ])
      )


(defn footer-template
      [coll]
        [:footer.page-footer.grey.darken-1
         [:div.container
          [:div.row
           (footer-head coll)
          ]
         ]
         [:div.footer-copyright
          [:div.container]
         ]
        ]
    )


(defn footer-head
      [coll]
      (map (fn [{:keys [heading items checked?]}]
               [:div.col.l4.s12
                [:h5.white-text heading]
                (footer-body items)])
           coll)
      )

(defn footer-body
      [items]
        [:ul
                 (map
                   (fn [{:keys [title href]}]
                       [:li [:a.white-text {:href href} title]])
                   items)])

(defn Page
  []
  (let [{:keys [app/title app/header app/footer]} github.state/page-state
         repo (subscribe [:repo])]
         [:div
          (header-template title header)
          (main-template @repo)
          (footer-template footer)
          ]))

(defn ^:export init
  []
  (dispatch-sync [:initialize])
  (dispatch-sync [:get-repo])
  (reagent/render [Page]
                  (js/document.getElementById "app")))


