(ns cache-test
	(:require [clojure.test :refer :all]
						[store]
						[cache]))

(deftest small-cache-test
	(testing "Create and get from a small store through cache."
		(let [r (store/create {:b 2 :c 3} 100)
					c (cache/create r 10)]
			; Cache empty
			(is (= '()      (cache/elements c)))
			; Get :a, not in cache or store
			(is (= nil      (cache/retrieve c :a)))
			(is (= '()      (cache/elements c)))
			; Get :b, in store, cache grows
			(is (= 2        (cache/retrieve c :b)))
			(is (= '(:b)    (cache/elements c)))
			; Get :c
			(is (= 3        (cache/retrieve c :c)))
			(is (= '(:b :c) (cache/elements c)))
			; Get :c again
			(is (= 3        (cache/retrieve c :c)))
			(is (= '(:b :c) (cache/elements c)))
			; Get :a again
			(is (= nil      (cache/retrieve c :a)))
			(is (= '(:b :c) (cache/elements c))))))

(def store-data
	{:a 1
	 :b 2
	 :c 3
	 :d 4
	 :e 5
	 :f 6
	 :g 7
	 :h 8
	 :i 9
	 :j 10
	 :k 11
	 :l 12
	 :m 13
	 :n 14
	 :o 15
	 :p 16
	 :q 17
	 :r 18
	 :s 19
	 :t 20
	 :u 21
	 :v 22
	 :w 23
	 :x 24
	 :y 25
	 :z 26})

(deftest larger-cache-test
	(testing "Use cache for 'larger' store."
		(let [r (store/create store-data 100)
					c (cache/create r 5)]
			; Start with empty cache
			(is (= '()               (cache/elements c)))
			; Get :invalid, not found
			(is (= nil               (cache/retrieve c :invalid)))
			(is (= '()               (cache/elements c)))
			; Get :a, :b, :c, :d, :e: cache grows
			(is (= 1                 (cache/retrieve c :a)))
			(is (= '(:a)             (cache/elements c)))
			(is (= 2                 (cache/retrieve c :b)))
			(is (= '(:a :b)          (cache/elements c)))
			(is (= 3                 (cache/retrieve c :c)))
			(is (= '(:a :b :c)       (cache/elements c)))
			(is (= 4                 (cache/retrieve c :d)))
			(is (= '(:a :b :c :d)    (cache/elements c)))
			(is (= 5                 (cache/retrieve c :e)))
			(is (= '(:a :b :c :d :e) (cache/elements c)))
			; Get :f, cache full so something should be evicted from cache
			; :a will be at the bottom of the splay tree, so should be removed
			(is (= 6                 (cache/retrieve c :f)))
			(is (= '(:b :c :d :e :f) (cache/elements c)))
			; Get :b, it will move to the top of the splay tree
			(is (= 2                 (cache/retrieve c :b)))
			(is (= '(:b :c :d :e :f) (cache/elements c)))
			; Get :g, something will need to be evicted
			(is (= 7                 (cache/retrieve c :g)))
			(is (= 5                 (count (cache/elements c))))
			; Get :h, something else will need to be evicted
			(is (= 8                 (cache/retrieve c :h)))
			(is (= 5                 (count (cache/elements c)))))))

(deftest multithreaded-cache-test
	(testing "Three threads access store through cache."
		(let [r (store/create store-data 100)
					c (cache/create r 5)
					body (fn []
								 (is (= 1 (cache/retrieve c :a)))
								 (is (= 2 (cache/retrieve c :b)))
								 (is (= 3 (cache/retrieve c :c)))
								 (is (= 4 (cache/retrieve c :d)))
								 (is (= 5 (cache/retrieve c :e)))
								 (is (= 6 (cache/retrieve c :f)))
								 (is (= 7 (cache/retrieve c :g)))
								 (is (= 7 (cache/retrieve c :g)))
								 (is (= 7 (cache/retrieve c :g)))
								 (is (= 7 (cache/retrieve c :g)))
								 (is (= 1 (cache/retrieve c :a)))
								 (is (= 6 (cache/retrieve c :f))))
					threads (repeatedly 30 (fn [] (Thread. body)))]
			(doseq [t threads] (.start t))
			(doseq [t threads] (.join t)))))

(deftest test-multiple-inserts
	(testing "See if the cache can handle multiple inserts of the same data"
		(let [
				s (store/create {:a 1} 10)
				c (cache/create s 5)
				body (fn [] (do 
					(cache/retrieve c :a)
					(is (= 1 (count (cache/elements c))))))
				threads (repeatedly 30 (fn [] (Thread. body)))
			]
			(doseq [t threads] (.start t))
			(doseq [t threads] (.join t)))))

(run-tests 'cache-test)
