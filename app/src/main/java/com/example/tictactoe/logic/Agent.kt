import kotlin.random.Random

abstract class Agent(
    val cellType: CellType,
    val diff : AgentDifficulties
){
    companion object{
        fun createAgent(cellType: CellType, diff: AgentDifficulties) = when(diff) {
            AgentDifficulties.EASY -> RandomAgent(cellType,diff)
            AgentDifficulties.MEDIUM -> MinimaxAgent(cellType, diff)
        }
    }
}

class RandomAgent(
    cellType: CellType,
    diff: AgentDifficulties
) : Agent(cellType, diff), Player{
    override fun getNextMove(gameState: GameState) : Pair<Int, Int>{
        while (true){
            val row = Random.nextInt(gameState.getGridSize())
            val col = Random.nextInt(gameState.getGridSize())
            if(gameState.getCell(row,col).content == gameState.notVisitedCell.content) {
                Display.toConsole("Player's $cellType move: ($row,$col)")
                return Pair(row, col)
            }
        }
    }
}

class MinimaxAgent(
    cellType: CellType,
    diff: AgentDifficulties
) : Agent(cellType,diff), Player{
    override fun getNextMove(gameState: GameState) : Pair<Int, Int>{
        val bestMove = GFG1.findBestMove(gameState.grid.gridToCharMatrix())
        Display.toConsole("Player's $cellType move: (${bestMove.row}, ${bestMove.col})")
        return Pair(bestMove.row, bestMove.col)
    }
}


