(ns cache
	(:require [splay-tree :as st]
						[store]))

; ----------- ;
; Convenience ;
; ----------- ;

(defn find-element
	"Find a value, don't care if it's nil, but ensure the ref is not set to nil!"
	[tree key]
	(try 
		(dosync (alter tree st/find-empty key))
		(catch IllegalStateException e nil)))

(defn insert-element
	"Add an element to the cache"
	[{tree :tree size :size store :store max :max :as cache} key value]

	(dosync (commute tree st/insert-empty key value))
	cache)

(defn insert-size-check
	"See if the cache is not too large, evict items if needed"
	[{tree :tree size :size store :store max :max :as cache} key value]
		(dosync
			(if (>= (ensure size) max)
				(commute tree st/remove-leaf-empty)
				(commute size + 1)))
		(insert-element cache key value)
		cache)

(defn insert-member-check 
	"Ensure an element is valid and not already present before adding it"

	[{tree :tree size :size store :store max :max :as cache} key value]

	(dosync
		(if (and value (not (st/find-nosplay-empty (ensure tree) key)))
			(insert-size-check cache key value)
			cache)))

(defn insert
	"Add an element to the cache"
	[cache key value]
	(insert-member-check cache key value))

; --------- ;
; Interface ;
; --------- ;

(defn create
	"Create a new cache for the given store, storing at most size entries."
	[store size]

	(def tree (ref (st/create-empty)))
	(set-validator! tree #(not (nil? %)))

	{:tree tree :size (ref 0) :store store :max size})

(defn retrieve
	"Retrieve value with key from cache."
	[{tree :tree size :size store :store max :max :as cache} key]
	(let [res (find-element tree key)]
		(if res
			(get res :value)
			(let [res (store/retrieve store key)]
				(insert cache key res)
				res))))

(defn elements
	"Returns the elements currently in the cache, ordered. Used for testing."
	[{tree :tree size :size store :store max :max}]
	(st/elements-empty @tree))
