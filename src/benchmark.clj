(ns benchmark
	(:require 
		[store]
		[cache]))

; ----- ;
; Setup ;
; ----- ;

(defn create-store 
	"
	Create a store with length. With index as key. 
	We don't use arrays to have benchmarks more similar to 'real' use.
	"
	[length latency]
	(store/create 
		(into {} (for [i (range length) :let [r (rand-int 100)]] {i r})) 
		latency))

(defn setup
	"Create a cache with the given parameters for a benchmark run."
	[store-size latency cache-size]
	(let [
		s (create-store store-size latency)
		c (cache/create s cache-size)
		]
		c))

(defn init-cache 
	"Fetch some random data to fill up the cache"
	[cache cache-size store-size]
	(doseq [
		i (range cache-size) 
		:let [r (rand-int store-size)]] 
		(cache/retrieve cache r)))

(defn create-threads
	"Create a thread that performs func for every client"
	[clients func]
	(repeatedly clients (fn [] (Thread. func))))

; ------------------- ;
; Benchmark Execution ;
; ------------------- ;

(defn run 
	"Run the benchmark, return the time."
	[threads]
	(time (do
		(doseq [t threads] (.start t))
		(doseq [t threads] (.join t)))))

(defn setup-and-run
	"
	Setup a benchmark and run it.
	fun should accept a store and cache size 
	and the cache. Using these arguments,
	fun should return a function.
	"
	[store-size latency cache-size clients func]
	(let [
			c (setup store-size latency cache-size)
			t (create-threads clients (func c cache-size store-size))
		]
	;	(init-cache c cache-size store-size)
		(run t)))

(defn run-multiple
	[store-size latency cache-size clients func]
	(doseq [i (range 10)] (setup-and-run store-size latency cache-size clients func)))

; ---------- ;
; Benchmarks ;
; ---------- ;

(defn single-item-fetch
	"Always fetch the same item, optimal scenario"
	[store-size latency cache-size clients]
	(defn func [cache _c _s] (fn [] (doseq [i (range 100)] (cache/retrieve cache 1))))
	(run-multiple store-size latency cache-size clients func))

(defn random-item-fetch
	"Always fetch a random item, worst-case scenario"
	[store-size latency cache-size clients]
	(defn func [c cs ss] (fn [] (doseq [i (range 100)] (cache/retrieve c (rand-int ss)))))
	(run-multiple store-size latency cache-size clients func))

(defn cache-size-fetch
	"Fetch from a group of items that fits in the cache"
	[store-size latency cache-size clients]
	(defn func [c cs ss] (fn [] (doseq [i (range 100)] (cache/retrieve c (rand-int cs)))))
	(run-multiple store-size latency cache-size clients func))

(defn fetch-five
	"Fetch one of a small group of items"
	[store-size latency cache-size clients]
	(defn func [c cs ss] (fn [] (doseq [i (range 100)]  (cache/retrieve c (rand-int 5)))))
	(run-multiple store-size latency cache-size clients func))


(defn run-single-test
	[func name store-size latency cache-size]
	(println "Running test: " name)
	(println "===================")

	(doseq [i [1 2 3 4 10]]
		(println "store: " store-size "latency: " latency "cache: " cache-size "clients: " i)
		(func store-size latency cache-size i)))


(run-single-test single-item-fetch "single item fetch" 100 1000 25)

(run-single-test random-item-fetch "random item fetch" 100 100 25)
(run-single-test random-item-fetch "random item fetch" 100 10  25)
(run-single-test random-item-fetch "random item fetch" 100 10  50)
(run-single-test random-item-fetch "random item fetch" 100 100 25)

(run-single-test cache-size-fetch "cache size fetch" 100 100 25)
(run-single-test cache-size-fetch "cache size fetch" 100 100 50)
(run-single-test cache-size-fetch "cache size fetch" 100 10  25)
(run-single-test cache-size-fetch "cache size fetch" 200 10  25)

(run-single-test fetch-five "fetch five" 100 100 25)
(run-single-test fetch-five "fetch five" 100 100 50)
(run-single-test fetch-five "fetch five" 200 100 25)
(run-single-test fetch-five "fetch five" 200 10  25)