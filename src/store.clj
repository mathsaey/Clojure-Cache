(ns store)

(defn create
	"Create a new store with latency access-time (in milliseconds)
	The store is implemented as a key-value map."
	[map latency]
	{:latency latency :map map})

(defn retrieve
	"Retrieve key from store.
	Returns corresponding value, or nil if key is not present in the store.
	Will block the calling thread for at least (:latency store) milliseconds,
	this method is considered to perform I/O, so cannot be called from within
	a transaction."
	[store key]
	(io! "fetching from store"
		(Thread/sleep (:latency store))
		(get (:map store) key)))
