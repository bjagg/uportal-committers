(ns uportal-committers
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [clojure.set :refer :all]
            [clojure.pprint :refer :all]))

(def orgs ["uPortal-project" "uPortal-contrib" "uPortal-attic"])
(def team-slug "uportal-committers")
(def api-uri  "https://api.github.com")
(def api-token (System/getProperty "github-api-token"))
(def auth-str (str "token " api-token))
(def common-headers {:headers {:accept
                               "application/vnd.github+json"
                               :authorization
                               auth-str
                               :X-GitHub-Api-Version
                               "2022-11-28"}})

(defn process-paged-responses
  "Apply a function to response for a URL and parameters, handling paging hrefs"
  [init-url url-params collect-fn]
  (loop [url init-url
         collection []]
    (if-not url
      collection
      (let [response (client/get url url-params)
            json (cheshire/parse-string (:body response) true)
            resp-collection (collect-fn json)]
        (recur (get-in response [:links :next :href])
               (concat collection resp-collection))))))

(defn get-name [json] (map :name json))

(defn get-login [json] (map :login json))

(defn get-repos [org]
  (let [url (str api-uri "/orgs/" org "/repos")
        repos (process-paged-responses url common-headers get-name)]
    (map (juxt (constantly org) identity) repos)))

(defn get-committers []
  (let [url (str api-uri "/orgs/" "uPortal-project" "/teams/" team-slug "/members")]
    (process-paged-responses url common-headers get-login)))

(defn no-recent-commits? [[org repo] login]
  (let [url (str api-uri "/repos/" org "/" repo "/commits")
        since "2022-02-01T00:00:00Z"
        q {:query-params {:author login :since since}}
        url-params (merge common-headers q)
        response (client/get url url-params)
        json (cheshire/parse-string (:body response) true)]
    #_(printf "Checking commits for %s in %s" login repo)
    (empty? json)))

(defn inactive-repo-committers
  "Given a list of members, remove any that are active in this repo"
  [members org-repo]
  (let [no-commits? (partial no-recent-commits? org-repo)]
    (filter no-commits? members)))

(defn inactive-committers [orgs]
  (let [members (get-committers)
        org-repos (mapcat get-repos orgs)]
    (pprint members)
    #_(pprint org-repos)
    (reduce inactive-repo-committers members org-repos)))

(defn run [opts]
  (println (do (inactive-committers orgs))))
