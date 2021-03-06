(ns schema-conformer.core-test
  (:require [clojure.test :refer :all]
            [schema-conformer.core :refer :all]
            [clj-time.core :as time]
            [schema.core :as s]
            [schema-conformer.transforms :as t]
            [schema.experimental.abstract-map :as sam])
  (:import (java.time Instant)
           (org.joda.time DateTime)))

(defn opts [& ks]
  (let [originals (into {} (map vector (keys t/DEFAULTS) (repeat false)))
        overrides (into {} (map vector ks (repeat true)))]
    (merge originals overrides)))

(defmacro verify [schema expected provided]
  `(is (= ~expected (conform ~schema ~provided))))

(defmacro verify-option [option & triples]
  `(testing ~(name option)
     (let [opt# (opts ~option)]
       (doseq [[schema# expected# provided#] [~@(map vec (partition 3 triples))]]
         (is (= expected# (conform schema# opt# provided#)))))))

(s/defschema Animal
  (sam/abstract-map-schema
    :type
    {:friends #{s/Keyword}}))

(sam/extend-schema Giraffe Animal
  [:giraffe]
  {:numberOfLegs (default s/Int 4)})

(deftest conform-test
  (verify-option :align-map-keys
                 {:required (s/maybe s/Str)} {:required nil} {}
                 {(s/optional-key :optional) (s/maybe s/Str)} {} {:optional nil})

  (testing "constrained->nested"
    (verify (s/constrained {:test s/Bool} not-empty) {:test true} {:test "true"}))

  (testing "enums"
    (verify (s/enum true false) false "false"))

  (testing "abstract schemas"
    (verify Giraffe {:friends #{} :numberOfLegs 4 :type :giraffe} {:type :giraffe}))

  (verify-option :datetime->string s/Str "1970-01-01T00:00:00.000Z" (time/epoch))
  (verify-option :instant->string s/Str "1970-01-01T00:00:00Z" (Instant/EPOCH))
  (verify-option :integer->datetime DateTime (DateTime. (.toEpochMilli (Instant/EPOCH))) (.toEpochMilli (Instant/EPOCH)))
  (verify-option :integer->instant Instant (Instant/EPOCH) (.toEpochMilli (Instant/EPOCH)))
  (verify-option :keyword->string s/Str "testing" :testing)
  (verify-option :keyword->symbol s/Symbol 'testing :testing)
  (verify-option :list->set #{s/Str} #{"stuff"} '("stuff"))
  (verify-option :list->vector [s/Str] ["stuff"] '("stuff"))
  (verify-option :nil->map {} {} nil)
  (verify-option :nil->set #{s/Str} #{} nil)
  (verify-option :nil->vector [s/Str] [] nil)
  (verify-option :number->boolean s/Bool false 0))

(deftest map-key-coercions
  (let [data   {"test" 1}
        schema {(s/optional-key :test) s/Int}]
    (is (= {:test 1} (conform schema data))))
  (let [data   {:test 1}
        schema {(s/optional-key "test") s/Int}]
    (is (= {"test" 1} (conform schema data))))
  (let [data   {:test 1}
        schema {"test" s/Int}]
    (is (= {"test" 1} (conform schema data))))
  (let [data   {}
        schema {"test" (s/maybe s/Int)}]
    (is (= {"test" nil} (conform schema data))))
  (let [data   {}
        schema {(s/required-key "test") (s/maybe s/Int)}]
    (is (= {"test" nil} (conform schema data))))
  (let [data   {:test 1}
        schema {(s/required-key "test") (s/maybe s/Int)}]
    (is (= {"test" 1} (conform schema data))))
  (let [data   {"test" 1}
        schema {"cats" [s/Str] s/Keyword s/Int}]
    (is (= {"cats" [], :test 1} (conform schema data)))))


(deftest deep-merge-test
  (let [a      (s/constrained {:a s/Str} #(= (:a %) "test") "a-equals-test?")
        b      (s/constrained {:b s/Str} #(= (:b %) "tset") "b-equals-tset?")
        merged (deep-merge a b)]
    (is (not (conforms? merged {:a "rawr" :b "cat"})))
    (is (not (conforms? merged {:a "test" :b "cat"})))
    (is (not (conforms? merged {:a "rawr" :b "tset"})))
    (is (conforms? merged {:a "test" :b "tset"}))))

(deftest default-test
  (let [schema {:a (default s/Keyword :bingo)}]
    (is (= {:a :bingo} (conform schema {})))))
