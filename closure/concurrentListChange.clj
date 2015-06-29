(defn add-item [wishlist item]
    (dosync (alter wishlist conj item)))
    
(def family-wishlist (ref '("iPad")))
(def original-wishlist @family-wishlist)
(println "Original wish list is" original-wishlist)

(future (add-item family-wishlist "MBP"))
(future (add-item family-wishlist "Bike"))

(. Thread sleep 1000)

(println "Original wish list is" original-wishlist)
(println "Updated wish list is" @family-wishlist)