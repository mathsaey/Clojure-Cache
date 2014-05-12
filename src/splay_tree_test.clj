(ns splay-tree-test
	(:require [clojure.test :refer :all]
						[splay-tree :as st]))

(defn ordered?
	"Check whether a tree is correctly ordered."
	[tree]
	(if (nil? tree)
		true
		(let [l (:left tree)
					r (:right tree)]
			(and
				(if (nil? l)
					true
					(and (not= (compare (:key l) (:key tree)) 1) (ordered? l)))
				(if (nil? r)
					true
					(and (not= (compare (:key r) (:key tree)) -1) (ordered? r)))))))

; List of keys to find in given trees, and expected new tree
(def find-tests
	[; zig
	 {:tree {:key 4 :left {:key 2 :left {:key 1} :right {:key 3}} :right {:key 5}}
		:key 2
		:before "(4 (2 (1 _ _) (3 _ _)) (5 _ _))"
		:after  "(2 (1 _ _) (4 (3 _ _) (5 _ _)))"}
	 ; zig-zig
	 {:tree {:key 6 :left {:key 4 :left {:key 2 :left {:key 1} :right {:key 3}} :right {:key 5}} :right {:key 7}}
		:key 2
		:before "(6 (4 (2 (1 _ _) (3 _ _)) (5 _ _)) (7 _ _))"
		:after  "(2 (1 _ _) (4 (3 _ _) (6 (5 _ _) (7 _ _))))"}
	 ; zig-zag
	 {:tree {:key 6 :left {:key 2 :left {:key 1} :right {:key 4 :left {:key 3} :right {:key 5}}} :right {:key 7}}
		:key 4
		:before "(6 (2 (1 _ _) (4 (3 _ _) (5 _ _))) (7 _ _))"
		:after  "(4 (2 (1 _ _) (3 _ _)) (6 (5 _ _) (7 _ _)))"}
	 ; zig-zag + zig
	 {:tree {:key 8 :left {:key 6 :left {:key 2 :left {:key 1} :right {:key 4 :left {:key 3} :right {:key 5}}} :right {:key 7}} :right {:key 9}}
		:key 4
		:before "(8 (6 (2 (1 _ _) (4 (3 _ _) (5 _ _))) (7 _ _)) (9 _ _))"
		:after  "(4 (2 (1 _ _) (3 _ _)) (8 (6 (5 _ _) (7 _ _)) (9 _ _)))"}
	 ; zig-zig + zig
	 {:tree {:key 8 :left {:key 6 :left {:key 4 :left {:key 2 :left {:key 1} :right {:key 3}} :right {:key 5}} :right {:key 7}} :right {:key 9}}
		:key 2
		:before "(8 (6 (4 (2 (1 _ _) (3 _ _)) (5 _ _)) (7 _ _)) (9 _ _))"
		:after  "(2 (1 _ _) (8 (4 (3 _ _) (6 (5 _ _) (7 _ _))) (9 _ _)))"}
	 ; other examples: simple zig-zig but right instead of left
	 {:tree {:key 1 :right {:key 4 :right {:key 9}}}
		:key 9
		:before "(1 _ (4 _ (9 _ _)))"
		:after  "(9 (4 (1 _ _) _) _)"}
	 ; other examples: from worst case to balanced
	 {:tree {:key 7 :left {:key 6 :left {:key 5 :left {:key 4 :left {:key 3 :left {:key 2 :left {:key 1}}}}}}}
		:key 1
		:before "(7 (6 (5 (4 (3 (2 (1 _ _) _) _) _) _) _) _)"
		:after  "(1 _ (6 (4 (2 _ (3 _ _)) (5 _ _)) (7 _ _)))"}
	 ; other examples: zig-zag, zig-zig, zig
	 {:tree {:key 10 :left {:key 8 :left {:key 6 :left {:key 2 :left {:key 1} :right {:key 4 :left {:key 3} :right {:key 5}}} :right {:key 7}} :right {:key 9}} :right {:key 11}}
		:key 3
		:before "(10 (8 (6 (2 (1 _ _) (4 (3 _ _) (5 _ _))) (7 _ _)) (9 _ _)) (11 _ _))"
		:after  "(3 (2 (1 _ _) _) (10 (6 (4 _ (5 _ _)) (8 (7 _ _) (9 _ _))) (11 _ _)))"}])

(deftest find-test
	(testing "Finding and splaying in example trees."
		(dorun
			(for [t find-tests]
				(let [before (:tree t)
							after  (st/find before (:key t))]
					(is (= (:before t) (st/to-string before)))
					(is (ordered? before))
					(is (= (:after t) (st/to-string after)))
					(is (ordered? after)))))))

; List of key and values to insert, and the expected tree at that moment.
(def insert-test-tree1
	[{:key 4 :value "d" :expected "(4 _ _)"}
	 {:key 1 :value "a" :expected "(1 _ (4 _ _))"}
	 {:key 9 :value "i" :expected "(9 (4 (1 _ _) _) _)"}
	 {:key 8 :value "h" :expected "(8 (4 (1 _ _) _) (9 _ _))"}
	 {:key 6 :value "f" :expected "(6 (4 (1 _ _) _) (8 _ (9 _ _)))"}
	 {:key 0 :value "z" :expected "(0 _ (6 (1 _ (4 _ _)) (8 _ (9 _ _))))"}
	 {:key 7 :value "g" :expected "(7 (0 _ (6 (1 _ (4 _ _)) _)) (8 _ (9 _ _)))"}
	 {:key 5 :value "e" :expected "(5 (0 _ (4 (1 _ _) _)) (7 (6 _ _) (8 _ (9 _ _))))"}
	 {:key 2 :value "b" :expected "(2 (0 _ (1 _ _)) (5 (4 _ _) (7 (6 _ _) (8 _ (9 _ _)))))"}
	 {:key 3 :value "c" :expected "(3 (2 (0 _ (1 _ _)) _) (4 _ (5 _ (7 (6 _ _) (8 _ (9 _ _))))))"}])

; In decreasing order: worst-case scenario
(def insert-test-tree2
	[{:key 9 :value "i" :expected "(9 _ _)"}
	 {:key 8 :value "h" :expected "(8 _ (9 _ _))"}
	 {:key 7 :value "g" :expected "(7 _ (8 _ (9 _ _)))"}
	 {:key 6 :value "f" :expected "(6 _ (7 _ (8 _ (9 _ _))))"}
	 {:key 5 :value "e" :expected "(5 _ (6 _ (7 _ (8 _ (9 _ _)))))"}
	 {:key 4 :value "d" :expected "(4 _ (5 _ (6 _ (7 _ (8 _ (9 _ _))))))"}
	 {:key 3 :value "c" :expected "(3 _ (4 _ (5 _ (6 _ (7 _ (8 _ (9 _ _)))))))"}
	 {:key 2 :value "b" :expected "(2 _ (3 _ (4 _ (5 _ (6 _ (7 _ (8 _ (9 _ _))))))))"}
	 {:key 1 :value "a" :expected "(1 _ (2 _ (3 _ (4 _ (5 _ (6 _ (7 _ (8 _ (9 _ _)))))))))"}
	 {:key 0 :value "z" :expected "(0 _ (1 _ (2 _ (3 _ (4 _ (5 _ (6 _ (7 _ (8 _ (9 _ _))))))))))"}])

(defn- test-insertion
	"Test whether a sequence of insertions creates the expected results."
	[test-tree]
	(loop [i 0
				 tree nil]
		(when (< i (count test-tree))
			(let [operation (nth test-tree i)
						new-tree (st/insert tree (:key operation) (:value operation))]
				(is (ordered? new-tree))
				(is (= (:expected operation) (st/to-string new-tree)))
				(recur (inc i) new-tree)))))

(deftest insert-test
	(testing "Create a tree with a some elements, to test insert."
		(testing "Simple example"
			(test-insertion insert-test-tree1))
		(testing "Example in decreasing order: worst-case"
			(test-insertion insert-test-tree2))))

(deftest create-insert-find-test
	(testing "Create a tree, insert, and find elements."
		(let [tree (st/insert (st/create "key1" "value1") "key2" "value2")]
			(is (= '("key1" "key2") (st/all-elements tree)))
			(is (ordered? tree))
			(is (= "value1" (:value (st/find tree "key1"))))
			(is (= "value2" (:value (st/find tree "key2")))))))

(deftest insert-find-test
	(testing "Create a tree, insert, and find elements."
		(let [tree (-> nil
									 (st/insert 4 "d")
									 (st/insert 1 "a")
									 (st/insert 9 "i")
									 (st/insert 8 "h")
									 (st/insert 6 "f")
									 (st/insert 0 "z")
									 (st/insert 7 "g")
									 (st/insert 5 "e")
									 (st/insert 2 "b")
									 (st/insert 3 "c"))]
			(is (= '(0 1 2 3 4 5 6 7 8 9) (st/all-elements tree)))
			(is (= "(3 (2 (0 _ (1 _ _)) _) (4 _ (5 _ (7 (6 _ _) (8 _ (9 _ _))))))"
						 (st/to-string tree)))
			(is (ordered? tree))
			(is (= nil (:value (st/find tree 10))))
			(is (= "z" (:value (st/find tree 0))))
			(is (= "a" (:value (st/find tree 1))))
			(is (= "b" (:value (st/find tree 2))))
			(is (= "c" (:value (st/find tree 3))))
			(is (= "d" (:value (st/find tree 4))))
			(is (= "e" (:value (st/find tree 5))))
			(is (= "f" (:value (st/find tree 6))))
			(is (= "g" (:value (st/find tree 7))))
			(is (= "h" (:value (st/find tree 8))))
			(is (= "i" (:value (st/find tree 9)))))))

(deftest remove-leaf-test
	(testing "Remove leaf in tree with only one leaf."
		(let [tree {:key 7 :left {:key 6 :left {:key 5 :left {:key 4 :left {:key 3 :left {:key 2 :left {:key 1}}}}}}}]
			(is (= '(1 2 3 4 5 6 7) (st/all-elements tree)))
			(is (= "(7 (6 (5 (4 (3 (2 (1 _ _) _) _) _) _) _) _)" (st/to-string tree)))
			(is (= "(7 (6 (5 (4 (3 (2 _ _) _) _) _) _) _)"
				(st/to-string (st/remove-leaf tree)))))))

(deftest remove-all-leaves-test
	(testing "Remove all leaves in tree."
		(let [tree (-> nil
									 (st/insert 4 "d")
									 (st/insert 1 "a")
									 (st/insert 9 "i")
									 (st/insert 8 "h")
									 (st/insert 6 "f")
									 (st/insert 0 "z")
									 (st/insert 7 "g")
									 (st/insert 5 "e")
									 (st/insert 2 "b")
									 (st/insert 3 "c"))]
			(is (= '(0 1 2 3 4 5 6 7 8 9) (st/all-elements tree)))
			(is (= "(3 (2 (0 _ (1 _ _)) _) (4 _ (5 _ (7 (6 _ _) (8 _ (9 _ _))))))"
						 (st/to-string tree)))
			(loop [n 10
						 t tree]
		(is (= n (count (st/all-elements t))))
				(if (= n 0)
					(is (nil? t))
					(do
						(is (not (nil? t)))
						(recur (dec n) (st/remove-leaf t))))))))

(run-tests 'splay-tree-test)
