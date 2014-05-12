# About

This is the repository for project of the Multicore Programming course of Mathijs Saey at the Vrije Universiteit Brussels. The goal of this project is to create a correct, thread-safe cache in Clojure.

# Assignment

## A Thread-safe Cache Using Splay Trees

The purpose of a cache is to speed up the retrieval of data, typically from a slow medium such a disk or a network. In this project, we will concentrate on the essential details by simply considering the store as a key-value mapping where retrieving a key has a latency of l milliseconds.

The goal of this project is to implement a cache, which sits in between the store and a number of clients, as depicted in figure 1. The cache supports a single (retrieve cache key) operation that looks up key in the cache. If the key is not found, the cache retrieves the key’s value from the underlying store and saves it in the cache for later, faster, access. If the key is found in the cache, access to the store is not necessary and the value should be returned to the client immediately. If the key is neither found in the cache nor in the store, the cache returns nil to the client, and the cache does not change.

The N clients can concurrently call retrieve on the same cache. Hence, the internal data structures of the cache must be thread-safe. Use Clojure’s built-in support for mutable state to ensure thread-safety. It is not necessary to make use of Java collections or locks.
The cache has a maximum size. When the cache is full (i.e. stores size key-value pairs) and a new item is fetched from the store, an existing item must be evicted (deleted) from the cache.
Assume for simplicity that the store is immutable: there is no need to implement a put operation on the store.

### Splay Trees

The cache will be implemented using a splay tree. A splay tree is a self-adjusting binary search tree: on each access, it will be modified so that the accessed element moves to the root. As such, over time the most used elements will move to the top of the tree. This makes a splay tree suitable for use in a cache. Splay trees provide insertion, look-up, and removal in O(log n) amortized time.
Splay trees rely on the concept of splaying: after an element has been found in the tree, a number of tree rotations will be performed to move the element to the root. This is illustrated in figure 2: when retrieving the element with key 5 from the tree, that element becomes the new root.
You can find more information on splay trees at:

* [Description and some visualizations of splay trees on Wikipedia.](https://en.wikipedia.org/wiki/Splay_tree)
* [Another description of splay trees.](http://www.cs.cornell.edu/courses/cs312/2008sp/lectures/lec25.html)
* [This website allows you to interactively visualize a splay tree, showing how inserts, finds, and deletes modify the tree.](http://www.cs.usfca.edu/~galles/visualization/SplayTree.html)
* [Another visualization of splay trees.](http://www.link.cs.cmu.edu/cgi-bin/splay/splay-cgi.pl)

## Requirements

### Thread-safe Cache

For this project, you are given a working implementation of splay trees. It is your task to implement the cache in a thread-safe way, i.e. when many clients use the cache concurrently, no race conditions should be possible.
You need to implement the (retrieve cache key) operation. This operation will mutate the cache, by moving the found element to the top of the tree, and possibly evicting an old cache entry. You must therefore make sure that concurrent calls to retrieve do not corrupt the cache’s data structures.
You can make use of any Clojure primitive to support thread-safe mutable state (agents, refs, atoms). It is up to you to select the most appropriate mechanism. Accessing the store, a slow operation, is considered to be a form of I/O and is thus not directly allowed within a running software transaction.

This assignment is accompanied by a number of source code files:

* `store.clj` implements the store. For simplicity, the store is represented as a simple hash map. Upon creation (create), a latency is specified, which is applied when retrieving (retrieve) an element from the store.
* `splay_tree.clj` provides a working implementation of a splay tree. It provides the necessary functions create, find, insert, and remove-leaf, as well as to-string and all-elements for debugging.
* `cache.clj` should implement your cache. The functions create and retrieve are specified in this file, you should implement them.

Furthermore, some unit tests are given. The unit tests for the store and splay tree succeed: they test whether the given implementation is correct. The unit tests for the cache fail, with a correct implementation they should pass. You are encouraged to extend the test suite to test alternative scenarios, especially to test concurrent client access. If your implementation requires a different interface, you are allowed to modify the tests, but please motivate the necessary changes in your report.

### Evaluation

Your evaluation should focus on two points: validating the correctness of your implementation, and evaluating the scalability.
To validate correctness, you should create a number of tests that simulate different scenar- ios. They should confirm that no race conditions can happen, such as elements appearing multiple times in the cache, or the cache growing above its maximum size.
To evaluate the scalability, create a number of benchmarks that measure the speed-up when varying a number of parameters, such as the size of the store, the size of the cache, the number of clients, and the latency of the store.
Caches rely on patterns in the access to their elements: they store recently accessed elements expecting them to be accessed again soon. Therefore, don’t access the elements in the store randomly in your tests, but make sure there is some sort of pattern. You can vary these patterns to validate in which cases your implementation works best.
The primary goal of this project is correctness: even in the presence of multiple concurrent clients, your cache should not become invalid. As experimentation should reveal, the sketched cache implementation may not allow scalable concurrent throughput. To pass this project, it is not necessary to implement a scalable cache. Any changes you make to facilitate better scaling are optional but can contribute to bonus points.