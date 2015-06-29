(def balance (ref 0))
(println "Balance is" @balance)
(ref-set balance 100)
(println "Balance is now" @balance)