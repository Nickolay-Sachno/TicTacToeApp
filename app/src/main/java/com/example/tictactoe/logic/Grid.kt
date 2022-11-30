import kotlin.random.Random

/** Control the grid of TicTacToe. The default dimension is 3, but can be modified */
class Grid(
    private val dim : Int,
    var matrix: Array<Array<Cell>> = kotlin.run {
        Array(dim) { row ->
            Array(dim) { col ->
                Cell(row, col)
            }
        }
    }
) {

    override fun toString(): String {
        var temp = ""
        for( i in matrix.indices){
            for(j in matrix[0].indices){
                temp += "${matrix[i][j].toString()} "
            }
            temp += "\n"
        }
        return temp
    }

    fun deepCopy() : Grid{
        val grid = Grid(this.dim)
        grid.matrix.forEachIndexed { row, arrayOfCells ->
            arrayOfCells.forEachIndexed { column, _ ->
                grid.matrix[row][column] = this.matrix[row][column].deepCopy()
            }
        }
        return grid
    }

    fun isMovesLeft() : Boolean {
        return matrix.any { row ->
            row.any { cell ->
                cell.content == CellType.EMPTY
            }
        }
    }

    fun fillRandomGrid() : Grid{
        val listOfMoves = listOf<CellType>(CellType.CIRCLE, CellType.CROSS)
        val grid = this.deepCopy()
        for(i in 0 until dim){
            for(j in 0 until dim){
                var temp = Random.nextInt(listOfMoves.size)
                grid.matrix[i][j].content = listOfMoves[temp]
            }
        }
        return grid
    }

    fun fillRandomGridWithSpaces() : Grid{
        val listOfMoves = listOf<CellType>(CellType.CIRCLE, CellType.CROSS, CellType.EMPTY)
        val grid = this.deepCopy()
        for(i in 0 until dim){
            for(j in 0 until dim){
                val temp = Random.nextInt(listOfMoves.size)
                grid.matrix[i][j].content = listOfMoves[temp]
            }
        }
        return grid
    }

    fun gridToCharMatrix() : Array<CharArray>{
        val charArr = Array(dim) { CharArray(dim) }
        for(i in 0 until dim){
            for(j in 0 until dim){
                charArr[i][j] = this.matrix[i][j].content.chr.lowercaseChar()
            }
        }
        return charArr
    }

    fun setNewCell(cell: Cell) {
        matrix[cell.row][cell.col] = cell.deepCopy()
    }

    fun getSize() : Int{
        return dim
    }

    fun getCell(row:Int, col:Int) : Cell{
        return matrix[row][col]
    }

    fun isEmpty(): Boolean {
       return matrix.all {
           row -> row.all {
               cell -> cell.content == CellType.EMPTY
           }
       }
    }
}