class Manager {
    companion object {
        fun play(gameState: GameState) {
            Display.toConsole("Welcome to TicTacToe !")
            var currentGameState = gameState.deepCopy()
            while (true) {
                Display.toConsole("$currentGameState")
                val currentPlayer = getCurrentPlayerAngMakeMove(currentGameState)
                val currentPlayerCellType = currentPlayer.cellType
                if (currentGameState.isWinState(Cell(content = currentPlayerCellType))
                    || currentGameState.isStandOff()){
                    when{
                        currentGameState.isWinState(Cell(content = currentPlayerCellType)) ->
                            Display.toConsole("Player $currentPlayerCellType WINS!!!!!")
                        currentGameState.isStandOff() ->
                            Display.toConsole("It's a standoff -_-")
                    }
                    Display.toConsole("$currentGameState")
                    return
                }
                currentGameState = currentGameState.updateGameState()
            } // end game loop
        }

        private fun getCurrentPlayerAngMakeMove(gameState: GameState)  : Player{
            val currentPlayer = gameState.getNextPlayer()
            val (row, col) = currentPlayer.getNextMove(gameState)
            gameState.setCell(Cell(
                row = row,
                col = col,
                content = currentPlayer.cellType
            ))
            gameState.listOfMoves.add(Pair(row,col))
            return currentPlayer
        }
    }
}