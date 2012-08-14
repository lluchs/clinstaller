(ns clinstaller.svn-test
  (:use clojure.test
        clinstaller.svn))

(deftest branch-function
  (are [url br] (= (branch url) br)
       "https://github.com/lluchs/Clinfinity/trunk/Clinfinity.c4d" "master"
       "https://github.com/lluchs/Clinfinity/branches/steam/Clinfinity.c4d" "steam"))

(deftest svn-url-function
  (testing "With two arguments"
    (is (= (str (svn-url "https://github.com/lluchs/Clinfinity/" "master"))
           "https://github.com/lluchs/Clinfinity/trunk")))
  (testing "With three arguments"
    (are [base branch path result] (= (str (svn-url base branch path)) result)
         "https://github.com/lluchs/Clinfinity/" "master" "Clinfinity.c4d"
           "https://github.com/lluchs/Clinfinity/trunk/Clinfinity.c4d"
         "https://github.com/lluchs/Clinfinity" "master" "Clinfinity.c4d"
           "https://github.com/lluchs/Clinfinity/trunk/Clinfinity.c4d"
         "https://github.com/lluchs/Clinfinity" "steam" "Clinfinity.c4d"
           "https://github.com/lluchs/Clinfinity/branches/steam/Clinfinity.c4d")))
