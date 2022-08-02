import kotlin.random.Random

class User(
    override val cellType: CellType
) : Player {

    override fun getNextMove(gameState: GameState): Pair<Int, Int> {
        val (row,col) = getInputFromUser(gameState)
        Display.toConsole("Player's $cellType move: ($row,$col)")
        return Pair(row,col)
    }

    private fun getInputFromUser(gameState: GameState): Pair<Int, Int> {
        while (true){
            Display.toConsole("Please enter a number for row: ", delay = 0)
            val row = readLine()
            Display.toConsole("Please enter a number for column: ", delay = 0)
            val col = readLine()
            if(!isValidMove(gameState,row,col)){
                Display.toConsole("Wrong input")
                return getRandomNextMove(gameState)
            } else{
                if(row != null && col != null)
                    return Pair(row.toInt(), col.toInt())
            }
        } // end get input loop
    }

    private fun getRandomNextMove(gameState: GameState): Pair<Int, Int> {
        while (true){
            val row = Random.nextInt(gameState.getGridSize())
            val col = Random.nextInt(gameState.getGridSize())
            if(gameState.getCell(row,col).content == gameState.notVisitedCell.content)
                return Pair(row,col)
        }
    }

    private fun isValidMove(gameState: GameState, row: String?, col: String?): Boolean {
        if (row != null && col != null) {
            when{
                (row.isEmpty() || col.isEmpty() ||
                        !row.all { char -> char.isDigit()} || !col.all { char -> char.isDigit() } ||
                        row.toInt() < 0 || col.toInt() < 0 ||
                        row.toInt() > gameState.getGridSize() - 1 || col.toInt() > gameState.getGridSize() - 1 ||
                        gameState.getCell(row.toInt(),col.toInt()) != gameState.notVisitedCell
                        ) -> return false
            }
            return true
        }
        return false
    }
}