interface Player {
    val cellType : CellType

    /** Return a coordinates (i,j) based on the game state */
    fun getNextMove(gameState: GameState) : Pair<Int,Int>
}