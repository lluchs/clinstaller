(ns clinstaller.svn
  (:import [org.tmatesoft.svn.core.wc SVNClientManager
                                      SVNRevision]
           [org.tmatesoft.svn.core SVNURL
                                   SVNDepth
                                   SVNException])
  (:use [clojure.java.io]))


; Client manager creates all necessary clients
(def cm (SVNClientManager/newInstance))
; Update client: checkout, switch, update
(def upc (.getUpdateClient cm))
; Status client: revision, url
(def sc (.getStatusClient cm))

(defn branch
  "Extracts the git branch from the given URL."
  [svnurl]
  (let [url (str svnurl)
        tail (last (re-find #"github\.com/\w+/[\w-]+/(\S+)" url))]
    (cond
      (not tail) nil
      (.startsWith tail "trunk") "master"
      (.startsWith tail "branches") (second (.split #"/" tail))
      :else nil)))


(defn status
  "Returns a hash containing status information for the given repos, or nil if
  the given path does not contain a repository."
  [repos]
  (try
    (let [st (.doStatus sc repos false)
          url (.getURL st)]
      {:date (.getCommittedDate st)
       :url url
       :branch (branch url)})
    (catch RuntimeException e nil)))

(defn- trail-slash
  "Appends a trailing slash to the given path if necessary."
  [url]
  (if (= (last url) \/)
    url
    (str url "/")))

(defn svn-url
  "Returns the SVN URL for the given base url and branch, optinally appending
  a local path."
  ([base branch] (svn-url base branch ""))
  ([base branch path]
    (SVNURL/parseURIEncoded
      (str (trail-slash base)
           (if (= branch "master")
             "trunk/"
             (str "branches/" branch "/"))
           path))))

(defn checkout
  "Checks out the given base url/branch/subdirectory combination into a new
  'subdirectory'."
  [base branch subdir]
  (.doCheckout upc
               (svn-url base branch subdir)
               (file subdir)
               SVNRevision/UNDEFINED
               SVNRevision/HEAD
               true))

(defn update
  "Updates the given repository to the latest revision."
  [dirname]
  (.doUpdate upc (file dirname) SVNRevision/HEAD true true))

(defn switch
  "Switches the repository in 'subdir' to a different branch."
  [base branch subdir]
  (.doSwitch upc
             (file subdir)
             (svn-url base branch subdir)
             SVNRevision/HEAD
             true))

(defn- rename
  "Renames the given directory to get it out of the way."
  ([dirname] (rename dirname (file dirname)))
  ([dirname original]
    (let [new-name (str dirname ".bak")
          dir (file new-name)]
      (if (.exists dir)
        ; Still exists -> continue
        (recur new-name original)
        ; Perform renaming
        (.renameTo original dir)))))

(defn download
  "Downloads the latest version for the given url/branch/subdirectory
  combination into 'subdirectory', invoking checkout, update or switch
  depending on the current state of the repository."
  [base branch subdir]
  (let [dir (file subdir)]
    (if (.exists dir)
      ; Directory exists, check current status
      (if-let [s (status dir)]
        ; There is already a repos
        (if (= branch (:branch s))
          ; Same branch => perform update
          (update subdir)
          ; Other branch => perform switch
          (switch base branch subdir))
        ; Other folder => rename and checkout
        (do
          (rename subdir)
          (checkout base branch subdir)))
      ; New directory, we can safely check out
      (checkout base branch subdir))))
