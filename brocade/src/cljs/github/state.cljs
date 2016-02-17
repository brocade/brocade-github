(ns github.state)

(def page-state
  {:app/title "OSS"
   :app/header
              [{:title "Projects" :href "#"}]
   :app/footer
              [{:heading "Projects" :items [{:title "Repositories" :href "#"}
                                            {:title "OpenDaylight" :href "#"}
                                            {:title "OpenStack" :href "#"}]}
               {:heading "Technology" :items [{:title "Software Networking" :href "#"}]}
               {:heading "FAQ" :items [{:title "Getting Started" :href "#"}
                                       {:title "Support" :href "#"}
                                       ]}]
   }
  )

(def fail-state
[{:repo
             {:name "Brocade",
              :html_url "https://github.com/brocade",
              :forks 1,
              :description "Brocade GitHub Repository",
              :commiters [],
              :lastcommit "4123372a06aa2d26889f2987303b0634198f5807",
              :date "2013-06-03T18:39:46Z",
              :author "Brocade Communications",
              :email "noreply@brocade.com"}}]
  )
