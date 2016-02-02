(ns github.page
  (:require
    [sablono.core :as sab :include-macros true]
    [goog.dom :as gdom]
    [om.dom :as dom]
    [om.next :as om :refer-macros [defui]]
    [github.state]
    )
  )

(enable-console-print!)

;; Initialize Application State

(def app-state
  (atom
    github.state/page-state))

(defmulti read (fn [env key params] key))

(defmethod read :default
           [{:keys [state] :as env} key params]
           (let [st @state]
                (if-let [[_ value] (find st key)]
                        {:value value}
                        {:value :not-found})))

(defmethod read :app/footer
           [{:keys [state] :as env} key {:keys [start end]}]
           {:value (subvec (:app/footer @state) start end)})

(defmethod read :git/repos
           [{:keys [state] :as env} key {:keys [start end]}]
           {:value (subvec (:git/repos @state) start end)})


(declare git-cards)
(declare footer-body)
(declare footer-head)


(defn header-template
      [title]
      (sab/html [:header.mdl-layout__header.mdl-layout__header--scroll.mdl-color--primary-dark
                 [:div.brocade-logo.mdl-layout__header-row
                  [:div.mdl-layout-spacer]
                  [:span.mdl-layout-title title]]])
      )

(defn main-template
      [repos]
      (sab/html
        [:main.mdl-layout__content
         [:div.mdl-layout__tab-panel.is-active {:id "overview"}]
         (git-cards repos)])
      )


(defn repo-template
      [title desc main]
      (sab/html
        [:div.mdl-cell.mdl-cell--4-col
         [:div.card-square.mdl-card.mdl-shadow--2dp
          [:div.mdl-card__title.mdl-card--expand
           [:h2.mdl-card__title-text title]]

          [:div.mdl-card__supporting-text
           desc]
          [:div.mdl-card__actions.mdl-card--border
           [:a.mdl-button.mdl-button--colored.mdl-js-button.mdl-js-ripple-effect
            main]]]]
        ))


(defn git-cards
      [repos]
      (let [c (count repos)]
           (sab/html [:section.section--center
                      [:div.git-cards.mdl-grid
                       (map #(repo-template (:title %) (:description %) (:maintainer %)) repos)
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

(defui Page
       Object
       (render [this]
               (let [{:keys [app/title app/footer git/repos]} (om/props this)]
                    (sab/html
                      [:div.mdl-layout.mdl-js-layout.mdl-layout--fixed-header
                       (header-template title)
                       (main-template repos)
                       (footer-template footer)]
                      ))))


(def reconciler
  (om/reconciler
    {:state  app-state
     :parser (om/parser {:read read})}))


(om/add-root! reconciler Page (gdom/getElement "app"))

