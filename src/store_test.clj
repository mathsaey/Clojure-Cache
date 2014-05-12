(ns store-test
  (:require [clojure.test :refer :all]
			[store]))

(deftest store-test
  (testing "Create and get from simple store."
	(let [r (store/create {:b 2 :c 3} 100)]
	  (is (= nil (store/retrieve r :a)))
	  (is (= 2   (store/retrieve r :b)))
	  (is (= 3   (store/retrieve r :c)))
	  (is (= nil (store/retrieve r :a))))))

(run-tests 'store-test)
