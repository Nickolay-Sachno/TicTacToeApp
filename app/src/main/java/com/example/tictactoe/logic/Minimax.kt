import kotlin.jvm.JvmStatic // Java program to find the

// next optimal move for a player
internal object GFG1 {
    var player = 'o'
    var opponent = 'x'

    // This function returns true if there are moves
    // remaining on the board. It returns false if
    // there are no moves left to play.
    fun isMovesLeft(board: Array<CharArray>): Boolean {
        for (i in 0..2) for (j in 0..2) if (board[i][j] == '-') return true
        return false
    }

    // This is the evaluation function as discussed
    // in the previous article ( http://goo.gl/sJgv68 )
    fun evaluate(b: Array<CharArray>): Int {
        // Checking for Rows for X or O victory.
        for (row in 0..2) {
            if (b[row][0] == b[row][1] &&
                b[row][1] == b[row][2]
            ) {
                if (b[row][0] == player) return +10 else if (b[row][0] == opponent) return -10
            }
        }

        // Checking for Columns for X or O victory.
        for (col in 0..2) {
            if (b[0][col] == b[1][col] &&
                b[1][col] == b[2][col]
            ) {
                if (b[0][col] == player) return +10 else if (b[0][col] == opponent) return -10
            }
        }

        // Checking for Diagonals for X or O victory.
        if (b[0][0] == b[1][1] && b[1][1] == b[2][2]) {
            if (b[0][0] == player) return +10 else if (b[0][0] == opponent) return -10
        }
        if (b[0][2] == b[1][1] && b[1][1] == b[2][0]) {
            if (b[0][2] == player) return +10 else if (b[0][2] == opponent) return -10
        }

        // Else if none of them have won then return 0
        return 0
    }

    // This is the minimax function. It considers all
    // the possible ways the game can go and returns
    // the value of the board
    fun minimax(
        board: Array<CharArray>,
        depth: Int, isMax: Boolean
    ): Int {
        val score = evaluate(board)

        // If Maximizer has won the game
        // return his/her evaluated score
        if (score == 10) return score

        // If Minimizer has won the game
        // return his/her evaluated score
        if (score == -10) return score

        // If there are no more moves and
        // no winner then it is a tie
        if (isMovesLeft(board) == false) return 0

        // If this maximizer's move
        return if (isMax) {
            var best = -1000

            // Traverse all cells
            for (i in 0..2) {
                for (j in 0..2) {
                    // Check if cell is empty
                    if (board[i][j] == '-') {
                        // Make the move
                        board[i][j] = player

                        // Call minimax recursively and choose
                        // the maximum value
                        best = Math.max(
                            best, minimax(
                                board,
                                depth + 1, !isMax
                            )
                        )

                        // Undo the move
                        board[i][j] = '-'
                    }
                }
            }
            best
        } else {
            var best = 1000

            // Traverse all cells
            for (i in 0..2) {
                for (j in 0..2) {
                    // Check if cell is empty
                    if (board[i][j] == '-') {
                        // Make the move
                        board[i][j] = opponent

                        // Call minimax recursively and choose
                        // the minimum value
                        best = Math.min(
                            best, minimax(
                                board,
                                depth + 1, !isMax
                            )
                        )

                        // Undo the move
                        board[i][j] = '-'
                    }
                }
            }
            best
        }
    }

    // This will return the best possible
    // move for the player
    fun findBestMove(board: Array<CharArray>): Move {
        var bestVal = -1000
        val bestMove = Move()
        bestMove.row = -1
        bestMove.col = -1

        // Traverse all cells, evaluate minimax function
        // for all empty cells. And return the cell
        // with optimal value.
        for (i in 0..2) {
            for (j in 0..2) {
                // Check if cell is empty
                if (board[i][j] == '-') {
                    // Make the move
                    board[i][j] = player

                    // compute evaluation function for this
                    // move.
                    val moveVal = minimax(board, 0, false)

                    // Undo the move
                    board[i][j] = '_'

                    // If the value of the current move is
                    // more than the best value, then update
                    // best/
                    if (moveVal > bestVal) {
                        bestMove.row = i
                        bestMove.col = j
                        bestVal = moveVal
                    }
                }
            }
        }
        return bestMove
    }

    // Driver code
    @JvmStatic
    fun main(args: Array<String>) {
        val board = arrayOf(
            charArrayOf('x', 'o', 'x'),
            charArrayOf('o', 'o', 'x'),
            charArrayOf('-', '-', '-'))
        val bestMove = findBestMove(board)
        System.out.printf("The Optimal Move is :\n")
        System.out.printf(
            "ROW: %d COL: %d\n\n",
            bestMove.row, bestMove.col
        )
    }

    internal class Move {
        var row = 0
        var col = 0
    }
}