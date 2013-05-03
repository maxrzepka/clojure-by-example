[
 {:id "many" :title "Many" :grammar (many letter) :input "asdf"}
 {:id "many1" :title "Many1" :grammar (many1 digit) :input "23243"}
 {:id "sequence" :title "Sequence" :grammar (<*> letter letter digit) :input "os7"}
 {:id "property" :title "Property"
  :grammar (bind [f (<?> identifier "property")
                   _ (sym \=) v
                   (comma-sep1 (field ",\n"))]
                     (return [f v]))
  :input "myproperty = hello, world"}
 {:id "line" :title "Parse Line"
  :description "Parse a simple line"
  :grammar (bind [s (word "Q")
                  line (field "\n")]
                 (return {:question line}))
  :input "Q Do you like Bach ?"}

 ]
