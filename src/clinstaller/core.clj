(ns clinstaller.core
  (:gen-class)
  (:require [clinstaller.cfg :as cfg]
            [clinstaller.svn :as svn]
            [clj-http.client :as client])
  (:use seesaw.core
        clojure.java.io))

(native!)

; Extract a list of branches
(def branches (map :name (:body (client/get (cfg/api :branches) {:as :json}))))

(def pb (progress-bar :indeterminate? true
                      :visible? false))

(def status-label (label))

(def branch-group (button-group))

(def action-panel
  (horizontal-panel :items
                    [(button :id :download
                             :text "Download")
                     (radio :group branch-group
                            :id :master
                            :text "master")
                     (radio :group branch-group
                            :id :other-branch)
                     (combobox :model branches
                               :editable? true
                               :id :branch)]))

(def f (frame
         :title "Clinstaller v2"
         :on-close :exit
         :size [300 :by 200]
         :resizable? false
         :content
         (border-panel :hgap 5 :vgap 5 :border 5
                       :north pb
                       :center status-label
                       :south action-panel)))

(defn branch-selection
  "Gets the user's current branch selection."
  []
  (if (= (-> branch-group selection id-of) :master)
    "master"
    (value (select f [:#branch]))))

(defn update-status
  "Updates the status-label with information about the repository."
  []
  (let [[status-message branch]
        (if-let [s (svn/status (-> cfg/repos :paths first file))]
          ; Construct status string
          [(str "<html>Currently on branch <b>" (:branch s) "</b><br>"
               "Latest change: " (:date s))
           (:branch s)]
          ["No valid repository found, click Download below!"
           "master"])]
    (config! status-label :text status-message)
    (config! (select f [(if (= branch "master")
                          :#master
                          :#other-branch)])
             :selected? true)
    (when-not (= branch "master")
      (config! (select f [:#branch]) :text branch))))

(defn do-download
  "Starts the download, calling the given function on completion."
  [func]
  (let [branch (branch-selection)]
    (.start (Thread.
      (fn []
        (try
          (doseq [subdir (cfg/repos :paths)]
            (svn/download (cfg/repos :url) branch subdir))
          (catch Exception e
            (invoke-now (alert f (.getMessage e)))))
        (invoke-later (func)))))))

(defn set-downloading
  "Sets widgets into downloading state or resets them."
  [on]
  (config! (config action-panel :items) :enabled? (not on))
  (config! pb :visible? on))

; Events
(listen (select f [:#branch]) :focus-gained
        (fn [e]
          (config! (select f [:#other-branch]) :selected? true)))

(listen (select f [:#download]) :action
        (fn [e]
          (set-downloading true)
          (config! status-label :text
                   (str "<html>Downloading branch <b>" (branch-selection)
                        "</b>;<br>Please wait..."))
          (do-download (fn []
                         (set-downloading false)
                         (update-status)))))

(defn -main [& args]
  (invoke-later
    (update-status)
    (show! f)))
