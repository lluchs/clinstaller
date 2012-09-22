(ns clinstaller.cfg)

(def gh "lluchs/Clinfinity")

(def repos {:url (str "https://github.com/" gh)
            :paths ["Clinfinity.c4d" "Clinfinity.c4f"]})

(def api {:branches (str "https://api.github.com/repos/" gh "/branches")})
