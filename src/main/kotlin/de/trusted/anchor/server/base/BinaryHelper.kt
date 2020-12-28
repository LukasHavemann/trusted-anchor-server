package de.trusted.anchor.server.base

class BinaryHelper {
    companion object {
        fun findBiggestBinaryTree(subtree: Int, treeWidth: Int): Int {
            if (subtree < 0) {
                return -1
            }

            if (isPowerOfTwo(subtree)) {
                return subtree
            }

            return findBiggestBinaryTree(subtree - (treeWidth / 2), treeWidth / 2)
        }

        fun isPowerOfTwo(x: Int): Boolean {
            return x and (x - 1) == 0
        }
    }
}