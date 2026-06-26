package com.example.presentation.games

import java.util.LinkedList
import java.util.Queue

object GameEngine {

    // ==========================================
    // 1. LIGHTS OUT SOLVER (Gaussian Elimination over GF(2))
    // ==========================================
    fun solveLightsOut(grid: BooleanArray): IntArray {
        // We have a 3x3 grid (9 lights).
        // A toggle matrix A of size 9x9 where A[i][j] = 1 if toggling j affects i.
        // We want to solve A * x = b over GF(2), where b is the current grid state (1 for ON, 0 for OFF).
        // Since we want to turn ALL lights OFF, the initial state of the grid needs to be reduced to 0.
        // Thus, toggling x should satisfy: current_state + A * x = 0  => A * x = current_state.
        val n = 9
        val matrix = Array(n) { IntArray(n + 1) }

        // Populate toggle matrix
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val i = r * 3 + c
                matrix[i][i] = 1 // toggling itself
                if (r > 0) matrix[i][(r - 1) * 3 + c] = 1 // top neighbor
                if (r < 2) matrix[i][(r + 1) * 3 + c] = 1 // bottom neighbor
                if (c > 0) matrix[i][r * 3 + (c - 1)] = 1 // left neighbor
                if (c < 2) matrix[i][r * 3 + (c + 1)] = 1 // right neighbor
                matrix[i][n] = if (grid[i]) 1 else 0 // b vector (current state)
            }
        }

        // Gaussian Elimination over GF(2)
        var lead = 0
        for (col in 0 until n) {
            var pivotRow = lead
            while (pivotRow < n && matrix[pivotRow][col] == 0) {
                pivotRow++
            }
            if (pivotRow == n) continue // No pivot in this column

            // Swap rows
            val temp = matrix[lead]
            matrix[lead] = matrix[pivotRow]
            matrix[pivotRow] = temp

            // Eliminate
            for (row in 0 until n) {
                if (row != lead && matrix[row][col] == 1) {
                    for (c in col..n) {
                        matrix[row][c] = matrix[row][c] xor matrix[lead][c]
                    }
                }
            }
            lead++
        }

        // Reconstruct steps
        val result = IntArray(n)
        for (i in 0 until n) {
            result[i] = matrix[i][n]
        }
        return result
    }

    // ==========================================
    // 2. NIM GAME UNBEATABLE AI
    // ==========================================
    data class NimMove(val heapIndex: Int, val amountToRemove: Int)

    fun calculateNimMove(heaps: List<Int>): NimMove {
        val nimSum = heaps.reduce { acc, i -> acc xor i }

        if (nimSum != 0) {
            // Unbeatable strategy: find a heap where heap XOR nimSum < heap
            for (i in heaps.indices) {
                val target = heaps[i] xor nimSum
                if (target < heaps[i]) {
                    return NimMove(i, heaps[i] - target)
                }
            }
        }

        // If nimSum is 0, we are in a losing position if the opponent plays perfectly.
        // Make any valid random move and hope for the best!
        for (i in heaps.indices) {
            if (heaps[i] > 0) {
                return NimMove(i, 1)
            }
        }
        return NimMove(0, 0)
    }

    // ==========================================
    // 3. RIVER CROSSING BFS SOLVER
    // ==========================================
    // State represents: (Farmer, Wolf, Goat, Cabbage) where true = Left bank, false = Right bank
    data class RiverState(
        val farmer: Boolean,
        val wolf: Boolean,
        val goat: Boolean,
        val cabbage: Boolean
    ) {
        fun isValid(): Boolean {
            // If Goat and Cabbage are together and Farmer is away, Goat eats Cabbage!
            if (goat == cabbage && farmer != goat) return false
            // If Wolf and Goat are together and Farmer is away, Wolf eats Goat!
            if (wolf == goat && farmer != wolf) return false
            return true
        }

        fun isGoal(): Boolean = !farmer && !wolf && !goat && !cabbage
    }

    fun solveRiverCrossing(start: RiverState): List<RiverState>? {
        val queue: Queue<List<RiverState>> = LinkedList()
        val visited = mutableSetOf<RiverState>()

        queue.add(listOf(start))
        visited.add(start)

        while (queue.isNotEmpty()) {
            val path = queue.poll() ?: continue
            val current = path.last()

            if (current.isGoal()) return path

            // Possible moves: Farmer crosses alone, or with Wolf, Goat, or Cabbage
            val nextStates = mutableListOf<RiverState>()
            // Farmer alone
            nextStates.add(current.copy(farmer = !current.farmer))
            
            // Farmer with Wolf (if they are on the same side)
            if (current.farmer == current.wolf) {
                nextStates.add(current.copy(farmer = !current.farmer, wolf = !current.wolf))
            }
            // Farmer with Goat
            if (current.farmer == current.goat) {
                nextStates.add(current.copy(farmer = !current.farmer, goat = !current.goat))
            }
            // Farmer with Cabbage
            if (current.farmer == current.cabbage) {
                nextStates.add(current.copy(farmer = !current.farmer, cabbage = !current.cabbage))
            }

            for (state in nextStates) {
                if (state.isValid() && state !in visited) {
                    visited.add(state)
                    val newPath = path + state
                    queue.add(newPath)
                }
            }
        }
        return null
    }

    // ==========================================
    // 4. 15-PUZZLE SOLVABILITY VERIFIER & BOARD GENERATOR
    // ==========================================
    fun isFifteenPuzzleSolvable(board: List<Int>): Boolean {
        // board contains numbers 1..15 and 0 for blank
        var inversions = 0
        for (i in board.indices) {
            for (j in i + 1 until board.size) {
                if (board[i] != 0 && board[j] != 0 && board[i] > board[j]) {
                    inversions++
                }
            }
        }
        val blankIndex = board.indexOf(0)
        val blankRowFromBottom = 4 - (blankIndex / 4) // 1 to 4

        return if (blankRowFromBottom % 2 == 0) {
            inversions % 2 != 0
        } else {
            inversions % 2 == 0
        }
    }

    fun generateSolvableFifteenPuzzle(): List<Int> {
        val numbers = (0..15).toMutableList()
        do {
            numbers.shuffle()
        } while (!isFifteenPuzzleSolvable(numbers) || numbers == (1..15).toList() + 0)
        return numbers
    }

    // ==========================================
    // 5. SECRET ANIMAL GUESS (Binary Decision Tree)
    // ==========================================
    sealed class DecisionNode {
        data class Question(val text: String, var yesNode: DecisionNode, var noNode: DecisionNode) : DecisionNode()
        data class Guess(val animalName: String) : DecisionNode()
    }

    fun getInitialAnimalTree(): DecisionNode {
        return DecisionNode.Question(
            "Does it live in water?",
            DecisionNode.Question(
                "Is it a mammal?",
                DecisionNode.Guess("Dolphin"),
                DecisionNode.Guess("Goldfish")
            ),
            DecisionNode.Question(
                "Can it fly?",
                DecisionNode.Guess("Eagle"),
                DecisionNode.Guess("Lion")
            )
        )
    }
}
